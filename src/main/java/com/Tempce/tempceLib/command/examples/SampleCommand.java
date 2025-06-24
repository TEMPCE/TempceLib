package com.Tempce.tempceLib.command.examples;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * テスト用のサンプルコマンド
 */
@Command(
    name = "sample",
    alias = {"smp", "example"},
    description = "TempceLibコマンドシステムのサンプル",
    usage = "/sample <subcommand>"
)
public class SampleCommand {
    
    @SubCommand(
        name = "hello",
        alias = {"hi", "greet"},
        description = "挨拶メッセージを表示",
        usage = "hello [name]"
    )
    public void hello(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "こんにちは！");
        } else {
            String name = String.join(" ", args);
            sender.sendMessage(ChatColor.GOLD + "こんにちは、" + name + "さん！");
        }
    }
    
    @SubCommand(
        name = "give",
        permission = "tempcelib.give",
        description = "アイテムを配布",
        usage = "give <item> [amount]",
        playerOnly = true
    )
    public void give(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "使用方法: /sample give <item> [amount]");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "不正なアイテム名: " + args[0]);
            return;
        }
        
        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > 64) {
                    player.sendMessage(ChatColor.RED + "数量は1-64の範囲で指定してください");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "無効な数量: " + args[1]);
                return;
            }
        }
        
        ItemStack item = new ItemStack(material, amount);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + material.name() + " x" + amount + " を配布しました！");
    }
    
    @SubCommand(
        name = "time",
        description = "現在時刻を表示",
        usage = "time"
    )
    public void time(CommandSender sender, String[] args) {
        long currentTime = System.currentTimeMillis();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = sdf.format(new java.util.Date(currentTime));
        sender.sendMessage(ChatColor.AQUA + "現在時刻: " + ChatColor.WHITE + timeString);
    }
    
    @SubCommand(
        name = "cooldown",
        description = "クールダウンテスト",
        usage = "cooldown",
        timeout = 10
    )
    public void cooldown(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "クールダウンテストが実行されました！次回実行まで10秒お待ちください。");
    }
    
    @SubCommand(
        name = "admin",
        permission = "tempcelib.admin",
        description = "管理者専用コマンド",
        usage = "admin"
    )
    public void admin(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.RED + "管理者専用機能が実行されました！");
    }
}
