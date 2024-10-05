package com.bobobo.plugins.commandlogger.conf;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
public class Config {
    private final JavaPlugin plugin;
    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }
    public List<String> getExcludedCommands() {
        return plugin.getConfig().getStringList("excluded-commands");
    }
    public int getLogFlushCooldown() {
        return plugin.getConfig().getInt("log-flush-cooldown", 10);
    }
    public void reloadConfig() {
        plugin.reloadConfig();
    }
}
