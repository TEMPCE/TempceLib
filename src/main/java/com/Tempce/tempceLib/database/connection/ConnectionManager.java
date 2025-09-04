package com.Tempce.tempceLib.database.connection;

import com.Tempce.tempceLib.database.config.DatabaseConfig;
import com.Tempce.tempceLib.database.config.DatabaseType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * データベース接続を管理するクラス
 * HikariCPを使用してコネクションプールを管理します
 */
public class ConnectionManager {
    
    private final Logger logger;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;
    private boolean initialized = false;
    
    /**
     * コンストラクタ
     * 
     * @param config データベース設定
     * @param logger ロガー
     */
    public ConnectionManager(DatabaseConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }
    
    /**
     * 接続プールを初期化します
     * 
     * @throws SQLException 初期化に失敗した場合
     */
    public void initialize() throws SQLException {
        if (initialized) {
            return;
        }
        
        try {
            // ドライバをロード
            Class.forName(config.getType().getDriverClass());
            
            // HikariCP設定
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.buildJdbcUrl());
            
            // SQLite以外の場合は認証情報を設定
            if (config.getType() != DatabaseType.SQLITE) {
                hikariConfig.setUsername(config.getUsername());
                hikariConfig.setPassword(config.getPassword());
            }
            
            // コネクションプール設定
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getMinPoolSize());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());
            
            // データベース固有の設定
            configureDatabase(hikariConfig);
            
            dataSource = new HikariDataSource(hikariConfig);
            initialized = true;
            
            logger.info("Database connection pool initialized successfully for " + config.getType().getName());
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + config.getType().getDriverClass(), e);
        } catch (Exception e) {
            throw new SQLException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * データベース固有の設定を行います
     * 
     * @param hikariConfig HikariCP設定
     */
    private void configureDatabase(HikariConfig hikariConfig) {
        switch (config.getType()) {
            case MYSQL:
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
                break;
                
            case POSTGRESQL:
                hikariConfig.addDataSourceProperty("prepareThreshold", "1");
                hikariConfig.addDataSourceProperty("preparedStatementCacheQueries", "250");
                hikariConfig.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
                hikariConfig.addDataSourceProperty("databaseMetadataCacheFields", "65536");
                hikariConfig.addDataSourceProperty("databaseMetadataCacheFieldsMiB", "5");
                break;
                
            case SQLITE:
                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                hikariConfig.addDataSourceProperty("cache_size", "10000");
                hikariConfig.addDataSourceProperty("foreign_keys", "true");
                hikariConfig.addDataSourceProperty("busy_timeout", "30000");
                break;
        }
    }
    
    /**
     * データベース接続を取得します
     * 
     * @return データベース接続
     * @throws SQLException 接続の取得に失敗した場合
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Connection manager is not initialized");
        }
        
        return dataSource.getConnection();
    }
    
    /**
     * 接続プールをクローズします
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
        initialized = false;
    }
    
    /**
     * 接続プールの統計情報を取得します
     * 
     * @return 統計情報の文字列
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }
        
        return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
    
    /**
     * 初期化状態を確認します
     * 
     * @return 初期化済みの場合true
     */
    public boolean isInitialized() {
        return initialized;
    }
}
