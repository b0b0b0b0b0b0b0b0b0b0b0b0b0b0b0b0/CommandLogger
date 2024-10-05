package com.bobobo.plugins.commandlogger.huy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
public class GovnoCode {
    private final JavaPlugin plugin;
    private final ConcurrentLinkedQueue<String> logQueue;
    private final Map<UUID, BufferedWriter> playerWriters = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public GovnoCode(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logQueue = new ConcurrentLinkedQueue<>();
        File logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
    }
    public void loadPlayerLog(Player player) {
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        String logFileName = playerName + "_" + playerUUID + ".log";
        File logFile = new File(plugin.getDataFolder() + File.separator + "logs", logFileName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            playerWriters.put(playerUUID, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании файла лога для игрока: " + playerName, e);
        }
    }
    public void logCommand(Player player, String command, List<String> excludedCommands) {
        String cmd = command.split(" ")[0].toLowerCase();
        if (excludedCommands.contains(cmd)) {
            return;
        }
        String timestamp = dateFormat.format(new Date());
        String logEntry = "[" + timestamp + "] [" + player.getName() + "]: " + command;
        logQueue.add(logEntry);
    }
    public void flushLogs(Player player) {
        CompletableFuture.runAsync(() -> {
            UUID playerUUID = player.getUniqueId();
            BufferedWriter writer = playerWriters.get(playerUUID);
            if (writer == null) {
                plugin.getLogger().log(Level.WARNING, "Writer для игрока " + player.getName() + " не инициализирован.");
                return;
            }
            while (!logQueue.isEmpty()) {
                String logEntry = logQueue.poll();
                if (logEntry != null) {
                    try {
                        writer.write(logEntry + "\n");
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Ошибка при записи в лог игрока", e);
                    }
                }
            }
            try {
                writer.flush();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при сбросе буфера", e);
            }
        });
    }
    public void closePlayerLog(Player player) {
        UUID playerUUID = player.getUniqueId();
        BufferedWriter writer = playerWriters.remove(playerUUID);
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при закрытии файла лога игрока " + player.getName(), e);
            }
        }
    }
    public void closeAllLogs() {
        for (Map.Entry<UUID, BufferedWriter> entry : playerWriters.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при закрытии файла лога для игрока " + entry.getKey(), e);
            }
        }
    }
}
