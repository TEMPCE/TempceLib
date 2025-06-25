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
import java.util.HashMap;
import java.util.Map;

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
        
        String subCommandName = args[0].toLowerCase();
        SubCommandData subCommandData = commandData.getSubCommands().get(subCommandName);
        
        if (subCommandData == null) {
            // エイリアスで検索
            for (SubCommandData subCmd : commandData.getSubCommands().values()) {
                if (subCmd.getAliases().contains(subCommandName)) {
                    subCommandData = subCmd;
                    break;
                }
            }
        }
        
        if (subCommandData == null) {
            sender.sendMessage(ChatColor.RED + "不明なサブコマンドです: " + subCommandName);
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
            String cooldownKey = sender.getName() + ":" + commandName + ":" + subCommandName;
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
        
        // サブコマンド実行
        try {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            
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
                String usage = subCmd.getUsage().isEmpty() ? subCmd.getName() : subCmd.getUsage();
                sender.sendMessage(ChatColor.AQUA + "  " + usage + " - " + subCmd.getDescription());
            }
        }
    }
}
