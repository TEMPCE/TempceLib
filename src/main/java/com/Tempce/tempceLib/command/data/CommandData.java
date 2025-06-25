package com.Tempce.tempceLib.command.data;

import java.util.List;
import java.util.Map;

/**
 * コマンドの情報を保持するクラス
 */
public class CommandData {
    private final String name;
    private final String permission;
    private final List<String> aliases;
    private final int timeout;
    private final String description;
    private final String usage;
    private final Object instance;
    private final Map<String, SubCommandData> subCommands;
    
    public CommandData(String name, String permission, List<String> aliases, 
                      int timeout, String description, String usage, 
                      Object instance, Map<String, SubCommandData> subCommands) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
        this.timeout = timeout;
        this.description = description;
        this.usage = usage;
        this.instance = instance;
        this.subCommands = subCommands;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public List<String> getAliases() {
        return aliases;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUsage() {
        return usage;
    }
    
    public Object getInstance() {
        return instance;
    }
    
    public Map<String, SubCommandData> getSubCommands() {
        return subCommands;
    }
}
