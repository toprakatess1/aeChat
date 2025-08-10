package me.toprak.aechat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AEChat extends JavaPlugin implements Listener {

    private boolean chatEnabled = true;
    private final Set<Player> mutedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        chatEnabled = getConfig().getBoolean("chat-enabled", true);
        getLogger().info("AEChat aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("aespeak.admin") && !sender.isOp()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("sohbet")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-sohbet")));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "ac":
                    chatEnabled = true;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-opened")));
                    break;

                case "kapat":
                    chatEnabled = false;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-closed-broadcast")));
                    break;

                case "sil":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-sohbet")));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("herkes")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            clearChat(p);
                        }
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-cleared")));
                    } else {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player-not-found")));
                            return true;
                        }
                        clearChat(target);
                        sender.sendMessage(ChatColor.GREEN + target.getName() + " adlı oyuncunun sohbeti temizlendi.");
                    }
                    break;

                default:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-sohbet")));
                    break;
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("sustur")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-sustur")));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player-not-found")));
                return true;
            }
            mutedPlayers.add(target);
            sender.sendMessage(ChatColor.GREEN + target.getName() + " susturuldu.");
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.muted-message")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("susturac")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-susturac")));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player-not-found")));
                return true;
            }
            if (mutedPlayers.remove(target)) {
                sender.sendMessage(ChatColor.GREEN + target.getName() + " susturması kaldırıldı.");
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.unmuted-message")));
            } else {
                sender.sendMessage(ChatColor.YELLOW + target.getName() + " zaten susturulmamış.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("haber")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-haber")));
                return true;
            }

            String type = args[0].toLowerCase();
            String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

            switch (type) {
                case "title":
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', message), "", 10, 70, 20);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Başlık mesajı gönderildi.");
                    break;
                case "subtitle":
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("", ChatColor.translateAlternateColorCodes('&', message), 10, 70, 20);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Alt başlık mesajı gönderildi.");
                    break;
                default:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usage-haber")));
                    break;
            }
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!chatEnabled) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-closed")));
            event.setCancelled(true);
            return;
        }

        if (mutedPlayers.contains(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.muted")));
            event.setCancelled(true);
        }
    }

    private void clearChat(Player player) {
        // 100 boş mesaj göndererek sohbet ekranını temizler
        for (int i = 0; i < 100; i++) {
            player.sendMessage("");
        }
    }
}
