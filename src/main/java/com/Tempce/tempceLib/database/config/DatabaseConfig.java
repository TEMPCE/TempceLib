package com.Tempce.tempceLib.database.config;

/**
 * データベース接続設定を管理するクラス
 */
public class DatabaseConfig {
    
    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String filePath; // SQLite用
    private String tablePrefix;
    private boolean useSSL;
    
    // Connection Pool設定
    private int maxPoolSize;
    private int minPoolSize;
    private long connectionTimeout;
    private long idleTimeout;
    private long maxLifetime;
    
    /**
     * デフォルトコンストラクタ
     */
    public DatabaseConfig() {
        // デフォルト値設定
        this.type = DatabaseType.SQLITE;
        this.host = "localhost";
        this.port = 3306;
        this.database = "tempcelib";
        this.username = "root";
        this.password = "";
        this.filePath = "plugins/TempceLib/database.db";
        this.tablePrefix = "tempce_";
        this.useSSL = false;
        
        // Connection Pool デフォルト値
        this.maxPoolSize = 10;
        this.minPoolSize = 1;
        this.connectionTimeout = 30000;
        this.idleTimeout = 600000;
        this.maxLifetime = 1800000;
    }
    
    /**
     * JDBC URLを生成します
     * 
     * @return JDBC URL
     */
    public String buildJdbcUrl() {
        switch (type) {
            case MYSQL:
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&characterEncoding=UTF-8",
                        host, port, database, useSSL);
            case POSTGRESQL:
                return String.format("jdbc:postgresql://%s:%d/%s?ssl=%s",
                        host, port, database, useSSL);
            case SQLITE:
                return "jdbc:sqlite:" + filePath;
            default:
                throw new IllegalStateException("Unsupported database type: " + type);
        }
    }
    
    // Getters and Setters
    public DatabaseType getType() {
        return type;
    }
    
    public void setType(DatabaseType type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }
    
    public boolean isUseSSL() {
        return useSSL;
    }
    
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public int getMinPoolSize() {
        return minPoolSize;
    }
    
    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public long getIdleTimeout() {
        return idleTimeout;
    }
    
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    
    public long getMaxLifetime() {
        return maxLifetime;
    }
    
    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }
}
