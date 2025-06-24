package com.Tempce.tempceLib.command.data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * サブコマンドの情報を保持するクラス
 */
public class SubCommandData {
    private final String name;
    private final String permission;
    private final List<String> aliases;
    private final int timeout;
    private final String description;
    private final String usage;
    private final boolean playerOnly;
    private final Method method;
    private final Object instance;
    
    public SubCommandData(String name, String permission, List<String> aliases, 
                         int timeout, String description, String usage, 
                         boolean playerOnly, Method method, Object instance) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
        this.timeout = timeout;
        this.description = description;
        this.usage = usage;
        this.playerOnly = playerOnly;
        this.method = method;
        this.instance = instance;
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
    
    public boolean isPlayerOnly() {
        return playerOnly;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Object getInstance() {
        return instance;
    }
}
