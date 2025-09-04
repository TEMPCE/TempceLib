package com.Tempce.tempceLib.command.data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * サブコマンドの情報を保持するクラス
 */
public class SubCommandData {
    private final String path;
    private final String permission;
    private final List<String> aliases;
    private final int timeout;
    private final String description;
    private final String usage;
    private final boolean playerOnly;
    private final Method method;
    private final Object instance;
    private final List<ArgumentData> arguments;
    private String parentCommandName; // 親コマンド名
    
    public SubCommandData(String path, String permission, List<String> aliases, 
                         int timeout, String description, String usage, 
                         boolean playerOnly, Method method, Object instance, 
                         List<ArgumentData> arguments) {
        this.path = path;
        this.permission = permission;
        this.aliases = aliases;
        this.timeout = timeout;
        this.description = description;
        this.usage = usage;
        this.playerOnly = playerOnly;
        this.method = method;
        this.instance = instance;
        this.arguments = arguments;
    }
    
    public String getPath() {
        return path;
    }
    
    /**
     * パスの最初の部分を取得（例: "test.test2" -> "test"）
     */
    public String getFirstLevelName() {
        return path.split("\\.")[0];
    }
    
    /**
     * パスの全レベルを配列として取得（例: "test.test2" -> ["test", "test2"]）
     */
    public String[] getPathLevels() {
        return path.split("\\.");
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
    
    public List<ArgumentData> getArguments() {
        return arguments;
    }
    
    /**
     * 引数が定義されているかどうかを判定
     */
    public boolean hasArguments() {
        return arguments != null && !arguments.isEmpty();
    }
    
    public String getParentCommandName() {
        return parentCommandName;
    }
    
    public void setParentCommandName(String parentCommandName) {
        this.parentCommandName = parentCommandName;
    }
}
