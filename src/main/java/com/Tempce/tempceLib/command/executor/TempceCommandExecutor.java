package com.Tempce.tempceLib.command.executor;

import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.data.SubCommandData;
import com.Tempce.tempceLib.command.manager.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TempceLibコマンドシステムのメインエグゼキューター
 */
public class TempceCommandExecutor implements CommandExecutor {
    
    private final CommandManager commandManager;
    private final Map<String, Long> cooldowns = new HashMap<>();
    
    public TempceCommandExecutor(CommandManager commandManager) {
        this.commandManager = commandManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        CommandData commandData = commandManager.getCommand(commandName);
        
        if (commandData == null) {
            sender.sendMessage(ChatColor.RED + "不明なコマンドです: " + commandName);
            return true;
        }
        
        // 権限チェック
        if (!commandData.getPermission().isEmpty() && !sender.hasPermission(commandData.getPermission())) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }
        
        // タイムアウトチェック
        if (commandData.getTimeout() > 0) {
            String cooldownKey = sender.getName() + ":" + commandName;
            Long lastUsed = cooldowns.get(cooldownKey);
            if (lastUsed != null) {
                long timeLeft = (lastUsed + commandData.getTimeout() * 1000L) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    sender.sendMessage(ChatColor.RED + "このコマンドはあと" + (timeLeft / 1000) + "秒後に再度実行できます。");
                    return true;
                }
            }
            cooldowns.put(cooldownKey, System.currentTimeMillis());
        }
        
        if (args.length == 0) {
            // サブコマンドがない場合、ヘルプを表示
            sendCommandHelp(sender, commandData);
            return true;
        }
        
        // 多階層パスを構築
        SubCommandData subCommandData = findSubCommand(commandData, args);
        
        if (subCommandData == null) {
            // サブコマンドが見つからない場合、部分パスのヘルプを表示
            if (showPartialPathHelp(sender, commandData, args)) {
                return true;
            }
            
            sender.sendMessage(ChatColor.RED + "不明なサブコマンドです: " + String.join(" ", args));
            sendCommandHelp(sender, commandData);
            return true;
        }
        
        // プレイヤー限定チェック
        if (subCommandData.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行可能です。");
            return true;
        }
        
        // 権限チェック
        if (!subCommandData.getPermission().isEmpty() && !sender.hasPermission(subCommandData.getPermission())) {
            sender.sendMessage(ChatColor.RED + "このサブコマンドを実行する権限がありません。");
            return true;
        }
        
        // サブコマンドタイムアウトチェック
        if (subCommandData.getTimeout() > 0) {
            String cooldownKey = sender.getName() + ":" + commandName + ":" + subCommandData.getPath();
            Long lastUsed = cooldowns.get(cooldownKey);
            if (lastUsed != null) {
                long timeLeft = (lastUsed + subCommandData.getTimeout() * 1000L) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    sender.sendMessage(ChatColor.RED + "このサブコマンドはあと" + (timeLeft / 1000) + "秒後に再度実行できます。");
                    return true;
                }
            }
            cooldowns.put(cooldownKey, System.currentTimeMillis());
        }
        
        // サブコマンド実行 - パスのレベル分だけ引数をスキップ
        try {
            int pathLevels = subCommandData.getPathLevels().length;
            String[] subArgs = new String[Math.max(0, args.length - pathLevels)];
            if (subArgs.length > 0) {
                System.arraycopy(args, pathLevels, subArgs, 0, subArgs.length);
            }
            
            subCommandData.getMethod().invoke(subCommandData.getInstance(), sender, subArgs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            sender.sendMessage(ChatColor.RED + "コマンド実行中にエラーが発生しました。");
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void sendCommandHelp(CommandSender sender, CommandData commandData) {
        sender.sendMessage(ChatColor.GREEN + "========== " + commandData.getName() + " ==========");
        sender.sendMessage(ChatColor.GRAY + commandData.getDescription());
        sender.sendMessage(ChatColor.YELLOW + "利用可能なサブコマンド:");
        
        for (SubCommandData subCmd : commandData.getSubCommands().values()) {
            if (subCmd.getPermission().isEmpty() || sender.hasPermission(subCmd.getPermission())) {
                String usage = subCmd.getUsage().isEmpty() ? subCmd.getFirstLevelName() : subCmd.getUsage();
                sender.sendMessage(ChatColor.AQUA + "  " + usage + " - " + subCmd.getDescription());
            }
        }
    }
    
    /**
     * 引数からサブコマンドを検索する（多階層対応）
     */
    private SubCommandData findSubCommand(CommandData commandData, String[] args) {
        // 最長マッチで検索（長いパスから順に試す）
        for (int length = args.length; length > 0; length--) {
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (i > 0) pathBuilder.append(".");
                pathBuilder.append(args[i].toLowerCase());
            }
            String path = pathBuilder.toString();
            
            // 直接パスで検索
            for (SubCommandData subCmd : commandData.getSubCommands().values()) {
                if (subCmd.getPath().equalsIgnoreCase(path)) {
                    return subCmd;
                }
            }
            
            // エイリアスで検索（第1レベルのみ）
            if (length == 1) {
                for (SubCommandData subCmd : commandData.getSubCommands().values()) {
                    if (subCmd.getAliases().contains(args[0].toLowerCase())) {
                        return subCmd;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 部分パスのヘルプを表示する
     * @param sender コマンド送信者
     * @param commandData コマンドデータ
     * @param args 引数
     * @return ヘルプを表示した場合はtrue
     */
    private boolean showPartialPathHelp(CommandSender sender, CommandData commandData, String[] args) {
        // 部分パスを構築
        StringBuilder partialPath = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) partialPath.append(".");
            partialPath.append(args[i].toLowerCase());
        }
        String targetPath = partialPath.toString();
        
        // この部分パスで始まるサブコマンドを検索
        Map<String, SubCommandData> matchingCommands = new HashMap<>();
        Set<String> nextLevels = new HashSet<>();
        
        for (SubCommandData subCmd : commandData.getSubCommands().values()) {
            if (subCmd.getPermission().isEmpty() || sender.hasPermission(subCmd.getPermission())) {
                String cmdPath = subCmd.getPath().toLowerCase();
                
                if (cmdPath.startsWith(targetPath + ".")) {
                    // 部分パスで始まるコマンドを発見
                    matchingCommands.put(subCmd.getPath(), subCmd);
                    
                    // 次のレベルを抽出
                    String remaining = cmdPath.substring(targetPath.length() + 1);
                    String nextLevel = remaining.split("\\.")[0];
                    nextLevels.add(nextLevel);
                }
            }
        }
        
        if (matchingCommands.isEmpty()) {
            return false; // 該当するサブコマンドがない
        }
        
        // ヘルプを表示
        sender.sendMessage(ChatColor.GREEN + "========== " + String.join(" ", args) + " のサブコマンド ==========");
        sender.sendMessage(ChatColor.GRAY + "利用可能なサブコマンド:");
        
        // 次のレベルのコマンドを表示
        for (String nextLevel : nextLevels.stream().sorted().collect(Collectors.toList())) {
            List<SubCommandData> levelCommands = matchingCommands.values().stream()
                .filter(cmd -> cmd.getPath().toLowerCase().startsWith(targetPath.toLowerCase() + "." + nextLevel))
                .collect(Collectors.toList());
                
            if (!levelCommands.isEmpty()) {
                String fullNextPath = targetPath + "." + nextLevel;
                
                // 完全一致するコマンドがあるかチェック
                SubCommandData exactMatch = levelCommands.stream()
                    .filter(cmd -> cmd.getPath().equalsIgnoreCase(fullNextPath))
                    .findFirst()
                    .orElse(null);
                
                if (exactMatch != null) {
                    // 完全一致するコマンドがある場合
                    sender.sendMessage(ChatColor.AQUA + "  " + nextLevel + " - " + exactMatch.getDescription());
                } else {
                    // さらに深い階層がある場合
                    long deeperCount = levelCommands.stream()
                        .filter(cmd -> cmd.getPath().split("\\.").length > args.length + 1)
                        .count();
                    
                    sender.sendMessage(ChatColor.AQUA + "  " + nextLevel + " - サブカテゴリ (" + deeperCount + "個のコマンド)");
                }
            }
        }
        
        sender.sendMessage(ChatColor.GRAY + "使用方法: /" + commandData.getName() + " " + String.join(" ", args) + " <サブコマンド>");
        sender.sendMessage(ChatColor.GREEN + "================================================");
        
        return true;
    }
}
