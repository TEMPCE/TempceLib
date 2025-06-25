package com.Tempce.tempceLib.command.helper;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.data.SubCommandData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自動生成されるヘルプコマンドの実行クラス
 */
public class AutoHelpExecutor {
    
    private final Command commandAnnotation;
    private final Map<String, SubCommandData> subCommands;
    
    public AutoHelpExecutor(Command commandAnnotation, Map<String, SubCommandData> subCommands) {
        this.commandAnnotation = commandAnnotation;
        this.subCommands = subCommands;
    }
    
    /**
     * ヘルプコマンドを実行する
     * @param sender コマンド送信者
     * @param args 引数
     */
    public void executeHelp(CommandSender sender, String[] args) {
        if (args.length > 0 && !args[0].isEmpty()) {
            // 特定のサブコマンドのヘルプを表示
            showSubCommandHelp(sender, args[0]);
        } else {
            // 全体のヘルプを表示
            showGeneralHelp(sender);
        }
    }
    
    /**
     * 全体のヘルプを表示する
     * @param sender コマンド送信者
     */
    private void showGeneralHelp(CommandSender sender) {
        String commandName = commandAnnotation.name();
        
        sender.sendMessage(ChatColor.GOLD + "========== " + commandName.toUpperCase() + " ヘルプ ==========");
        sender.sendMessage(ChatColor.YELLOW + "説明: " + ChatColor.WHITE + commandAnnotation.description());
        
        if (!commandAnnotation.usage().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "使用方法: " + ChatColor.WHITE + commandAnnotation.usage());
        }
        
        if (commandAnnotation.alias().length > 0) {
            sender.sendMessage(ChatColor.YELLOW + "エイリアス: " + ChatColor.WHITE + 
                String.join(", ", commandAnnotation.alias()));
        }
        
        if (!commandAnnotation.permission().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "必要権限: " + ChatColor.WHITE + commandAnnotation.permission());
        }
        
        if (commandAnnotation.timeout() > 0) {
            sender.sendMessage(ChatColor.YELLOW + "クールダウン: " + ChatColor.WHITE + commandAnnotation.timeout() + "秒");
        }
        
        sender.sendMessage(ChatColor.AQUA + "利用可能なサブコマンド:");
        
        // 権限のあるサブコマンドのみを表示
        subCommands.values().stream()
            .filter(subCmd -> subCmd.getPermission().isEmpty() || sender.hasPermission(subCmd.getPermission()))
            .collect(Collectors.groupingBy(SubCommandData::getName)) // 重複を除去
            .values().stream()
            .map(list -> list.get(0)) // 各グループの最初の要素を取得
            .sorted((a, b) -> a.getName().compareTo(b.getName()))
            .forEach(subCmd -> {
                StringBuilder line = new StringBuilder();
                line.append(ChatColor.GREEN).append("  /").append(commandName)
                    .append(" ").append(subCmd.getName());
                
                if (!subCmd.getAliases().isEmpty()) {
                    line.append(ChatColor.GRAY).append(" (")
                        .append(String.join(", ", subCmd.getAliases())).append(")");
                }
                
                line.append(ChatColor.WHITE).append(" - ").append(subCmd.getDescription());
                
                sender.sendMessage(line.toString());
            });
        
        sender.sendMessage(ChatColor.GRAY + "詳細情報: /" + commandName + " help <サブコマンド>");
        sender.sendMessage(ChatColor.GOLD + "=======================================");
    }
    
    /**
     * 特定のサブコマンドのヘルプを表示する
     * @param sender コマンド送信者
     * @param subCommandName サブコマンド名
     */
    private void showSubCommandHelp(CommandSender sender, String subCommandName) {
        SubCommandData subCommand = subCommands.get(subCommandName.toLowerCase());
        
        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "サブコマンド '" + subCommandName + "' が見つかりません。");
            sender.sendMessage(ChatColor.GRAY + "/" + commandAnnotation.name() + " help で利用可能なサブコマンドを確認してください。");
            return;
        }
        
        // 権限チェック
        if (!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(ChatColor.RED + "そのサブコマンドの情報を表示する権限がありません。");
            return;
        }
        
        String commandName = commandAnnotation.name();
        
        sender.sendMessage(ChatColor.GOLD + "========== " + subCommand.getName().toUpperCase() + " ヘルプ ==========");
        sender.sendMessage(ChatColor.YELLOW + "コマンド: " + ChatColor.WHITE + "/" + commandName + " " + subCommand.getName());
        sender.sendMessage(ChatColor.YELLOW + "説明: " + ChatColor.WHITE + subCommand.getDescription());
        
        if (!subCommand.getUsage().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "使用方法: " + ChatColor.WHITE + "/" + commandName + " " + subCommand.getUsage());
        }
        
        if (!subCommand.getAliases().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "エイリアス: " + ChatColor.WHITE + 
                String.join(", ", subCommand.getAliases()));
        }
        
        if (!subCommand.getPermission().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "必要権限: " + ChatColor.WHITE + subCommand.getPermission());
        }
        
        if (subCommand.getTimeout() > 0) {
            sender.sendMessage(ChatColor.YELLOW + "クールダウン: " + ChatColor.WHITE + subCommand.getTimeout() + "秒");
        }
        
        if (subCommand.isPlayerOnly()) {
            sender.sendMessage(ChatColor.YELLOW + "制限: " + ChatColor.WHITE + "プレイヤーのみ実行可能");
        }
        
        sender.sendMessage(ChatColor.GOLD + "========================================");
    }
}
