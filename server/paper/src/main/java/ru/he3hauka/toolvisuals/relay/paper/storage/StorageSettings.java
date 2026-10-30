package ru.he3hauka.toolvisuals.relay.paper.storage;

import java.nio.file.Path;
import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;

public final class StorageSettings {
    public enum Backend { SQLITE, MYSQL }
    public final Backend backend;
    public final Path sqliteFile;
    public final String host, database, username, password, sslMode;
    public final int port, maximumPoolSize, busyTimeoutMillis;
    public final long connectionTimeoutMillis, maximumLifetimeMillis;

    public StorageSettings(ConfigurationSection source, Path directory) {
        backend = Backend.valueOf(source.getString("type", "sqlite").toUpperCase(Locale.ROOT));
        String file = source.getString("sqlite.file", "toolvisuals.db");
        if (!file.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]{0,95}")) throw new IllegalArgumentException("sqlite.file must be a simple file name");
        sqliteFile = directory.resolve(file).toAbsolutePath().normalize();
        host = source.getString("mysql.host", "localhost");
        database = source.getString("mysql.database", "toolvisuals");
        username = source.getString("mysql.username", "toolvisuals");
        password = source.getString("mysql.password", "");
        sslMode = source.getString("mysql.ssl-mode", "VERIFY_IDENTITY").toUpperCase(Locale.ROOT);
        port = source.getInt("mysql.port", 3306);
        if (!host.matches("[a-zA-Z0-9.\\[\\]:_-]{1,253}") || !database.matches("[a-zA-Z0-9_-]{1,64}") || port < 1 || port > 65535)
            throw new IllegalArgumentException("Invalid MySQL host, database or port");
        if (!java.util.Set.of("DISABLED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY").contains(sslMode))
            throw new IllegalArgumentException("Invalid mysql.ssl-mode");
        maximumPoolSize = backend == Backend.SQLITE ? 1 : Math.clamp(source.getInt("pool.maximum-size", 4), 1, 16);
        busyTimeoutMillis = Math.clamp(source.getInt("sqlite.busy-timeout-millis", 5000), 1000, 30000);
        connectionTimeoutMillis = Math.clamp(source.getLong("pool.connection-timeout-millis", 5000), 1000, 30000);
        maximumLifetimeMillis = Math.clamp(source.getLong("pool.maximum-lifetime-millis", 1800000), 30000, 7200000);
    }
}
