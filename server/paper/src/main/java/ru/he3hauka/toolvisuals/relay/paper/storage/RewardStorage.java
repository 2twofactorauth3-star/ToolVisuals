package ru.he3hauka.toolvisuals.relay.paper.storage;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class RewardStorage implements AutoCloseable {
    public enum Claim { RESERVED, ALREADY_CLAIMED, REVIEW_REQUIRED, INCOMPLETE }
    private final HikariDataSource pool;
    private final String upsert;
    private final String insertClaim;

    public RewardStorage(StorageSettings settings) throws SQLException {
        var config = new HikariConfig();
        config.setPoolName("ToolVisuals-storage");
        config.setMaximumPoolSize(settings.maximumPoolSize);
        config.setMinimumIdle(settings.backend == StorageSettings.Backend.SQLITE ? 1 : 0);
        config.setConnectionTimeout(settings.connectionTimeoutMillis);
        config.setValidationTimeout(Math.min(3000, settings.connectionTimeoutMillis));
        config.setMaxLifetime(settings.maximumLifetimeMillis);
        config.setInitializationFailTimeout(1);
        config.setRegisterMbeans(false);
        if (settings.backend == StorageSettings.Backend.SQLITE) {
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + settings.sqliteFile);
            config.addDataSourceProperty("busy_timeout", Integer.toString(settings.busyTimeoutMillis));
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "FULL");
            upsert = "INSERT INTO toolvisuals_playtime(player_uuid,played_ms) VALUES(?,?) ON CONFLICT(player_uuid) DO UPDATE SET played_ms=MAX(toolvisuals_playtime.played_ms,excluded.played_ms)";
            insertClaim = "INSERT OR IGNORE INTO toolvisuals_rewards(player_uuid,reward_id,claim_id,state,created_ms,completed_commands) SELECT ?,?,?,'RESERVED',?,0 WHERE EXISTS(SELECT 1 FROM toolvisuals_playtime WHERE player_uuid=? AND played_ms>=?)";
        } else {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + settings.host + ":" + settings.port + "/" + settings.database);
            config.setUsername(settings.username);
            config.setPassword(settings.password);
            config.addDataSourceProperty("sslMode", settings.sslMode);
            config.addDataSourceProperty("connectTimeout", Long.toString(settings.connectionTimeoutMillis));
            config.addDataSourceProperty("socketTimeout", "15000");
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("allowLoadLocalInfile", "false");
            config.addDataSourceProperty("allowMultiQueries", "false");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "32");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            upsert = "INSERT INTO toolvisuals_playtime(player_uuid,played_ms) VALUES(?,?) ON DUPLICATE KEY UPDATE played_ms=GREATEST(played_ms,VALUES(played_ms))";
            insertClaim = "INSERT IGNORE INTO toolvisuals_rewards(player_uuid,reward_id,claim_id,state,created_ms,completed_commands) SELECT ?,?,?,'RESERVED',?,0 WHERE EXISTS(SELECT 1 FROM toolvisuals_playtime WHERE player_uuid=? AND played_ms>=?)";
        }
        pool = new HikariDataSource(config);
        try (var connection = pool.getConnection(); var statement = connection.createStatement()) {
            String engine = settings.backend == StorageSettings.Backend.MYSQL ? " ENGINE=InnoDB" : "";
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS toolvisuals_playtime(player_uuid VARCHAR(36) PRIMARY KEY,played_ms BIGINT NOT NULL)" + engine);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS toolvisuals_rewards(player_uuid VARCHAR(36) NOT NULL,reward_id VARCHAR(64) NOT NULL,claim_id VARCHAR(36) NOT NULL,state VARCHAR(16) NOT NULL,created_ms BIGINT NOT NULL,completed_commands INTEGER NOT NULL,PRIMARY KEY(player_uuid,reward_id))" + engine);
        } catch (SQLException | RuntimeException error) { pool.close(); throw error; }
    }

    public long load(UUID player) throws SQLException {
        try (var connection = pool.getConnection(); var query = connection.prepareStatement("SELECT played_ms FROM toolvisuals_playtime WHERE player_uuid=?")) {
            query.setString(1, player.toString());
            try (var rows = query.executeQuery()) { return rows.next() ? Math.max(0, rows.getLong(1)) : 0; }
        }
    }

    public void save(Map<UUID, Long> totals) throws SQLException {
        if (totals.isEmpty()) return;
        try (var connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try (var query = connection.prepareStatement(upsert)) {
                for (var entry : totals.entrySet()) {
                    query.setString(1, entry.getKey().toString()); query.setLong(2, entry.getValue()); query.addBatch();
                }
                query.executeBatch(); connection.commit();
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        }
    }

    public Claim reserve(UUID player, String reward, UUID claim, long totalMillis, long requiredMillis, long nowMillis) throws SQLException {
        try (var connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var save = connection.prepareStatement(upsert)) {
                    save.setString(1, player.toString()); save.setLong(2, totalMillis); save.executeUpdate();
                }
                int changed;
                try (var query = connection.prepareStatement(insertClaim)) {
                    query.setString(1, player.toString()); query.setString(2, reward); query.setString(3, claim.toString());
                    query.setLong(4, nowMillis); query.setString(5, player.toString()); query.setLong(6, requiredMillis);
                    changed = query.executeUpdate();
                }
                String previous = changed == 1 ? null : existing(connection, player, reward);
                Claim result = changed == 1 ? Claim.RESERVED : previous == null ? Claim.INCOMPLETE
                        : previous.equals("COMPLETE") ? Claim.ALREADY_CLAIMED : Claim.REVIEW_REQUIRED;
                connection.commit(); return result;
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        }
    }

    private static String existing(Connection connection, UUID player, String reward) throws SQLException {
        try (var query = connection.prepareStatement("SELECT state FROM toolvisuals_rewards WHERE player_uuid=? AND reward_id=?")) {
            query.setString(1, player.toString()); query.setString(2, reward);
            try (var rows = query.executeQuery()) { return rows.next() ? rows.getString(1) : null; }
        }
    }

    public boolean dispatching(UUID player, String reward, UUID claim) throws SQLException {
        return update("UPDATE toolvisuals_rewards SET state='DISPATCHING' WHERE player_uuid=? AND reward_id=? AND claim_id=? AND state='RESERVED'", player, reward, claim) == 1;
    }

    public void complete(UUID player, String reward, UUID claim, int commands, boolean success) throws SQLException {
        try (var connection = pool.getConnection(); var query = connection.prepareStatement("UPDATE toolvisuals_rewards SET state=?,completed_commands=? WHERE player_uuid=? AND reward_id=? AND claim_id=? AND state='DISPATCHING'")) {
            query.setString(1, success ? "COMPLETE" : "FAILED"); query.setInt(2, commands); query.setString(3, player.toString());
            query.setString(4, reward); query.setString(5, claim.toString());
            if (query.executeUpdate() != 1) throw new SQLException("Reward receipt changed before finalization");
        }
    }

    public void release(UUID player, String reward, UUID claim) throws SQLException {
        update("DELETE FROM toolvisuals_rewards WHERE player_uuid=? AND reward_id=? AND claim_id=? AND state IN ('RESERVED','DISPATCHING') AND completed_commands=0", player, reward, claim);
    }

    private int update(String sql, UUID player, String reward, UUID claim) throws SQLException {
        try (var connection = pool.getConnection(); var query = connection.prepareStatement(sql)) {
            query.setString(1, player.toString()); query.setString(2, reward); query.setString(3, claim.toString()); return query.executeUpdate();
        }
    }

    @Override public void close() { pool.close(); }
}
