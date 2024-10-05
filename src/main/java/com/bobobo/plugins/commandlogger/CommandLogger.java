package com.bobobo.plugins.commandlogger;
import com.bobobo.plugins.commandlogger.conf.Config;
import com.bobobo.plugins.commandlogger.huy.GovnoCode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class CommandLogger extends JavaPlugin implements Listener {
    private GovnoCode govnoCode;
    private Config config;
    @Override
    public void onEnable() {
        config = new Config(this);
        govnoCode = new GovnoCode(this);

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CommandLogger Plugin Enabled!");

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            getServer().getOnlinePlayers().forEach(govnoCode::flushLogs);
        }, 200L, 200L);
    }
    @Override
    public void onDisable() {
        govnoCode.closeAllLogs();
        getLogger().info("CommandLogger Plugin Disabled!");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        govnoCode.loadPlayerLog(event.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        govnoCode.closePlayerLog(event.getPlayer());
    }
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        PlayerKickEvent.Cause cause = event.getCause();
        if (cause == PlayerKickEvent.Cause.BANNED || cause == PlayerKickEvent.Cause.IP_BANNED) {
            govnoCode.closePlayerLog(event.getPlayer());
        }
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        govnoCode.logCommand(event.getPlayer(), event.getMessage(), config.getExcludedCommands());
    }
}
