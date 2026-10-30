package ru.he3hauka.toolvisuals.relay.paper.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import ru.he3hauka.toolvisuals.relay.paper.reward.RewardSettings;
import ru.he3hauka.toolvisuals.relay.paper.storage.StorageSettings;

public final class ServerConfiguration {
    public final RewardSettings reward;
    public final StorageSettings storage;

    public ServerConfiguration(JavaPlugin plugin) throws IOException, InvalidConfigurationException {
        plugin.saveDefaultConfig();
        var storageFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!storageFile.exists()) plugin.saveResource("storage.yml", false);
        var configFile = new File(plugin.getDataFolder(), "config.yml");
        var config = new YamlConfiguration(); config.options().parseComments(true); config.load(configFile);
        boolean migrated = ServerConfigMigrations.apply(config);
        var database = new YamlConfiguration(); database.load(storageFile);
        var messages = new HashMap<String, String>();
        var section = config.getConfigurationSection("messages");
        if (section != null) for (var entry : section.getValues(false).entrySet()) {
            if (!(entry.getValue() instanceof String value) || value.length() > 2048) throw new IllegalArgumentException("Invalid message: " + entry.getKey());
            messages.put(entry.getKey(), value);
        }
        reward = new RewardSettings(config.getBoolean("reward.enabled", true), config.getString("reward.id", "visuals-player"),
                config.getLong("reward.required-playtime-seconds", 1800), config.getStringList("reward.commands"),
                config.getInt("storage-flush-seconds", 30), config.getString("placeholders.using", "Да"), config.getString("placeholders.not-using", "Нет"), messages);
        storage = new StorageSettings(database, plugin.getDataFolder().toPath());
        if (migrated) ru.he3hauka.toolvisuals.storage.AtomicFiles.write(configFile.toPath(), config.saveToString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
