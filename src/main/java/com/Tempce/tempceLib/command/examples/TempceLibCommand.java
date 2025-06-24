package com.Tempce.tempceLib.command.examples;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * TempceLibのメインコマンドのサンプル実装
 */
@Command(
    name = "tempcelib",
    permission = "tempcelib.use",
    alias = {"tcl", "tempce"},
    description = "TempceLibのメインコマンド",
    usage = "/tempcelib <subcommand>"
)
public class TempceLibCommand {
    
    @SubCommand(
        name = "info",
        description = "TempceLibの情報を表示",
        usage = "info"
    )
    public void info(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "========== TempceLib Information ==========");
        sender.sendMessage(ChatColor.YELLOW + "バージョン: " + ChatColor.WHITE + "1.0.0");
        sender.sendMessage(ChatColor.YELLOW + "作者: " + ChatColor.WHITE + "Tempce");
        sender.sendMessage(ChatColor.YELLOW + "説明: " + ChatColor.WHITE + "Minecraftプラグイン開発支援ライブラリ");
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    @SubCommand(
        name = "test",
        alias = {"t"},
        description = "テストコマンド",
        usage = "test [message]",
        timeout = 5
    )
    public void test(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "テストコマンドが実行されました！");
        } else {
            String message = String.join(" ", args);
            sender.sendMessage(ChatColor.AQUA + "テストメッセージ: " + ChatColor.WHITE + message);
        }
    }
    
    @SubCommand(
        name = "reload",
        permission = "tempcelib.admin",
        description = "設定をリロード",
        usage = "reload"
    )
    public void reload(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "設定をリロードしました！");
    }
    
    @SubCommand(
        name = "heal",
        permission = "tempcelib.heal",
        description = "プレイヤーを回復",
        usage = "heal",
        playerOnly = true
    )
    public void heal(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.sendMessage(ChatColor.GREEN + "体力が回復しました！");
        }
    }
      @SubCommand(
        name = "stats",
        description = "コマンド統計を表示",
        usage = "stats"
    )
    public void stats(CommandSender sender, String[] args) {
        com.Tempce.tempceLib.TempceLib plugin = com.Tempce.tempceLib.TempceLib.getInstance();
        com.Tempce.tempceLib.command.manager.CommandManager manager = plugin.getCommandManager();
        
        sender.sendMessage(ChatColor.GOLD + "========== コマンド統計 ==========");
        sender.sendMessage(ChatColor.YELLOW + "登録コマンド数: " + ChatColor.WHITE + manager.getCommandCount());
        sender.sendMessage(ChatColor.YELLOW + "登録サブコマンド数: " + ChatColor.WHITE + manager.getSubCommandCount());
        
        sender.sendMessage(ChatColor.YELLOW + "登録コマンド一覧:");
        for (com.Tempce.tempceLib.command.data.CommandData cmdData : manager.getCommands().values()) {
            if (cmdData.getPermission().isEmpty() || sender.hasPermission(cmdData.getPermission())) {
                sender.sendMessage(ChatColor.AQUA + "  - " + cmdData.getName() + 
                    " (サブコマンド: " + cmdData.getSubCommands().size() + "個)");
            }
        }
        sender.sendMessage(ChatColor.GOLD + "===============================");
    }
}
