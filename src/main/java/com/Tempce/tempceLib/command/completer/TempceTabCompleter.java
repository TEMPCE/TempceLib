package com.Tempce.tempceLib.command.completer;

import com.Tempce.tempceLib.command.manager.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/**
 * TempceLibコマンドシステムのタブ補完
 */
public class TempceTabCompleter implements TabCompleter {
    
    private final CommandManager commandManager;
    
    public TempceTabCompleter(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandManager.getTabCompletions(sender, command.getName(), args);
    }
}
