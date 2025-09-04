package com.Tempce.tempceLib.database.manager;

import com.Tempce.tempceLib.database.config.DatabaseConfig;
import com.Tempce.tempceLib.database.config.DatabaseType;
import com.Tempce.tempceLib.database.connection.ConnectionManager;
import com.Tempce.tempceLib.database.data.QueryResult;
import com.Tempce.tempceLib.database.optimizer.DatabaseOptimizer;
import com.Tempce.tempceLib.database.optimizer.QueryOptimizer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * データベース統合機能のメインマネージャークラス
 * 
 * 複数のDBMS（MySQL、SQLite、PostgreSQL）をサポートし、
 * 最適化されたクエリ実行とパフォーマンス管理機能を提供します。
 */
public class DatabaseManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final ExecutorService executor;
    
    private DatabaseConfig config;
    private ConnectionManager connectionManager;
    private QueryOptimizer queryOptimizer;
    private DatabaseOptimizer databaseOptimizer;
    
    private boolean initialized = false;
    
    /**
     * コンストラクタ
     * 
     * @param plugin プラグインインスタンス
     */
    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r, "TempceLib-Database-" + Thread.currentThread().getId());
            thread.setDaemon(true);
            return thread;
        });
    }
    
    /**
     * データベースマネージャーを初期化します
     * 
     * @throws SQLException 初期化に失敗した場合
     */
    public void initialize() throws SQLException {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing database manager...");
        
        // 設定ファイルを読み込み
        loadConfiguration();
        
        // 接続マネージャーを初期化
        connectionManager = new ConnectionManager(config, logger);
        connectionManager.initialize();
        
        // 最適化ツールを初期化
        queryOptimizer = new QueryOptimizer(config.getType(), logger);
        databaseOptimizer = new DatabaseOptimizer(config.getType(), config.getTablePrefix(), logger);
        
        // 接続テスト
        testConnection();
        
        // SQLiteの場合はファイルディレクトリを作成
        if (config.getType() == DatabaseType.SQLITE) {
            createSQLiteDirectory();
        }
        
        initialized = true;
        logger.info("Database manager initialized successfully with " + config.getType().getName());
    }
    
    /**
     * 設定ファイルを読み込みます
     */
    private void loadConfiguration() {
        File configFile = new File(plugin.getDataFolder(), "database.yml");
        
        // デフォルト設定ファイルを作成
        if (!configFile.exists()) {
            createDefaultConfiguration(configFile);
        }
        
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);
        config = new DatabaseConfig();
        
        // 設定値を読み込み
        String typeString = fileConfig.getString("database.type", "sqlite");
        config.setType(DatabaseType.fromString(typeString));
        config.setHost(fileConfig.getString("database.host", "localhost"));
        config.setPort(fileConfig.getInt("database.port", 3306));
        config.setDatabase(fileConfig.getString("database.name", "tempcelib"));
        config.setUsername(fileConfig.getString("database.username", "root"));
        config.setPassword(fileConfig.getString("database.password", ""));
        config.setFilePath(fileConfig.getString("database.file", "plugins/TempceLib/database.db"));
        config.setTablePrefix(fileConfig.getString("database.table-prefix", "tempce_"));
        config.setUseSSL(fileConfig.getBoolean("database.use-ssl", false));
        
        // Connection Pool設定
        config.setMaxPoolSize(fileConfig.getInt("database.pool.max-size", 10));
        config.setMinPoolSize(fileConfig.getInt("database.pool.min-size", 1));
        config.setConnectionTimeout(fileConfig.getLong("database.pool.connection-timeout", 30000));
        config.setIdleTimeout(fileConfig.getLong("database.pool.idle-timeout", 600000));
        config.setMaxLifetime(fileConfig.getLong("database.pool.max-lifetime", 1800000));
        
        // パフォーマンス設定
        boolean enableQueryCache = fileConfig.getBoolean("performance.enable-query-cache", true);
        int autoOptimizeInterval = fileConfig.getInt("performance.auto-optimize-interval", 60);
        long slowQueryThreshold = fileConfig.getLong("performance.slow-query-threshold", 5000);
        
        logger.info("Database configuration loaded: " + config.getType().getName() + 
                   " (Query Cache: " + enableQueryCache + 
                   ", Auto-optimize: " + autoOptimizeInterval + "min" +
                   ", Slow Query Threshold: " + slowQueryThreshold + "ms)");
    }
    
    /**
     * デフォルト設定ファイルを作成します
     * 
     * @param configFile 設定ファイル
     */
    private void createDefaultConfiguration(File configFile) {
        plugin.getDataFolder().mkdirs();
        
        FileConfiguration config = new YamlConfiguration();
        
        // データベース設定
        config.set("database.type", "sqlite");
        config.set("database.host", "localhost");
        config.set("database.port", 3306);
        config.set("database.name", "tempcelib");
        config.set("database.username", "root");
        config.set("database.password", "");
        config.set("database.file", "plugins/TempceLib/database.db");
        config.set("database.table-prefix", "tempce_");
        config.set("database.use-ssl", false);
        
        // Connection Pool設定
        config.set("database.pool.max-size", 10);
        config.set("database.pool.min-size", 1);
        config.set("database.pool.connection-timeout", 30000);
        config.set("database.pool.idle-timeout", 600000);
        config.set("database.pool.max-lifetime", 1800000);
        
        // コメントを追加
        config.setComments("database", Arrays.asList(
            "Database configuration for TempceLib",
            "Supported types: mysql, sqlite, postgresql"
        ));
        
        try {
            config.save(configFile);
            logger.info("Created default database configuration file");
        } catch (Exception e) {
            logger.severe("Failed to create default configuration file: " + e.getMessage());
        }
    }
    
    /**
     * SQLiteディレクトリを作成します
     */
    private void createSQLiteDirectory() {
        File dbFile = new File(config.getFilePath());
        File parentDir = dbFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            logger.info("Created SQLite database directory: " + parentDir.getAbsolutePath());
        }
    }
    
    /**
     * 接続テストを実行します
     * 
     * @throws SQLException 接続に失敗した場合
     */
    private void testConnection() throws SQLException {
        try (Connection connection = connectionManager.getConnection()) {
            if (connection.isValid(5)) {
                logger.info("Database connection test successful");
            } else {
                throw new SQLException("Database connection is not valid");
            }
        }
    }
    
    /**
     * SQLクエリを非同期で実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parameters クエリパラメータ
     * @return クエリ結果のCompletableFuture
     */
    public CompletableFuture<QueryResult> executeQueryAsync(String query, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try (Connection connection = connectionManager.getConnection()) {
                String optimizedQuery = queryOptimizer.optimizeQuery(query);
                
                try (PreparedStatement stmt = connection.prepareStatement(optimizedQuery)) {
                    setParameters(stmt, parameters);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        long executionTime = System.currentTimeMillis() - startTime;
                        QueryResult result = new QueryResult(rs, executionTime);
                        
                        // 統計を記録
                        queryOptimizer.recordQueryStats(query, executionTime, result.getRowCount());
                        
                        return result;
                    }
                }
            } catch (SQLException e) {
                logger.severe("Failed to execute query: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * SQLアップデート（INSERT、UPDATE、DELETE）を非同期で実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parameters クエリパラメータ
     * @return 影響を受けた行数のCompletableFuture
     */
    public CompletableFuture<Integer> executeUpdateAsync(String query, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try (Connection connection = connectionManager.getConnection()) {
                String optimizedQuery = queryOptimizer.optimizeQuery(query);
                
                try (PreparedStatement stmt = connection.prepareStatement(optimizedQuery)) {
                    setParameters(stmt, parameters);
                    
                    int result = stmt.executeUpdate();
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    // 統計を記録
                    queryOptimizer.recordQueryStats(query, executionTime, result);
                    
                    return result;
                }
            } catch (SQLException e) {
                logger.severe("Failed to execute update: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * バッチ処理でクエリを実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parametersList パラメータのリスト
     * @return 実行結果のCompletableFuture
     */
    public CompletableFuture<int[]> executeBatchAsync(String query, List<Object[]> parametersList) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try (Connection connection = connectionManager.getConnection()) {
                String optimizedQuery = queryOptimizer.optimizeQuery(query);
                
                try (PreparedStatement stmt = connection.prepareStatement(optimizedQuery)) {
                    for (Object[] parameters : parametersList) {
                        setParameters(stmt, parameters);
                        stmt.addBatch();
                    }
                    
                    int[] result = stmt.executeBatch();
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    // 統計を記録
                    queryOptimizer.recordQueryStats(query, executionTime, result.length);
                    
                    return result;
                }
            } catch (SQLException e) {
                logger.severe("Failed to execute batch: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * テーブルを作成します
     * 
     * @param tableName テーブル名
     * @param columns カラム定義のマップ（カラム名 -> データ型）
     * @return 作成成功のCompletableFuture
     */
    public CompletableFuture<Boolean> createTable(String tableName, Map<String, String> columns) {
        return CompletableFuture.supplyAsync(() -> {
            String fullTableName = config.getTablePrefix() + tableName;
            
            try (Connection connection = connectionManager.getConnection()) {
                String createQuery = buildCreateTableQuery(fullTableName, columns);
                
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(createQuery);
                    logger.info("Created table: " + fullTableName);
                    return true;
                }
            } catch (SQLException e) {
                logger.severe("Failed to create table " + fullTableName + ": " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * テーブルが存在するかチェックします
     * 
     * @param tableName テーブル名
     * @return 存在する場合trueのCompletableFuture
     */
    public CompletableFuture<Boolean> tableExists(String tableName) {
        return CompletableFuture.supplyAsync(() -> {
            String fullTableName = config.getTablePrefix() + tableName;
            
            try (Connection connection = connectionManager.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet rs = metaData.getTables(null, null, fullTableName, new String[]{"TABLE"})) {
                    return rs.next();
                }
            } catch (SQLException e) {
                logger.severe("Failed to check table existence: " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * データベース統計情報を取得します
     * 
     * @return 統計情報のマップ
     */
    public CompletableFuture<Map<String, Object>> getDatabaseStats() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            try (Connection connection = connectionManager.getConnection()) {
                // 基本統計
                stats.put("database_type", config.getType().getName());
                stats.put("connection_pool_stats", connectionManager.getPoolStats());
                stats.put("query_stats", queryOptimizer.getQueryStats());
                
                // テーブル統計
                DatabaseMetaData metaData = connection.getMetaData();
                List<String> tables = new ArrayList<>();
                
                try (ResultSet rs = metaData.getTables(null, null, config.getTablePrefix() + "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_NAME"));
                    }
                }
                
                stats.put("tables", tables);
                stats.put("table_count", tables.size());
                
                return stats;
            } catch (SQLException e) {
                logger.severe("Failed to get database stats: " + e.getMessage());
                Map<String, Object> errorStats = new HashMap<>();
                errorStats.put("error", e.getMessage());
                return errorStats;
            }
        }, executor);
    }
    
    /**
     * データベース最適化を実行します
     * 
     * @return 最適化結果のCompletableFuture
     */
    public CompletableFuture<Boolean> optimizeDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionManager.getConnection()) {
                DatabaseOptimizer.OptimizationResult result = databaseOptimizer.optimizeDatabase(connection);
                logger.info("Database optimization completed: " + result.toString());
                return true;
            } catch (SQLException e) {
                logger.severe("Failed to optimize database: " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * 重複データのクリーンアップを実行します
     * 
     * @param tableName テーブル名
     * @param keyColumns 重複チェック対象のカラム
     * @return クリーンアップされた行数のCompletableFuture
     */
    public CompletableFuture<Integer> cleanupDuplicates(String tableName, String... keyColumns) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionManager.getConnection()) {
                return databaseOptimizer.cleanupDuplicates(connection, tableName, keyColumns);
            } catch (SQLException e) {
                logger.severe("Failed to cleanup duplicates: " + e.getMessage());
                return 0;
            }
        }, executor);
    }
    
    /**
     * プリペアードステートメントにパラメータを設定します
     * 
     * @param stmt プリペアードステートメント
     * @param parameters パラメータ配列
     * @throws SQLException SQL例外
     */
    private void setParameters(PreparedStatement stmt, Object[] parameters) throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
        }
    }
    
    /**
     * CREATE TABLEクエリを構築します
     * 
     * @param tableName テーブル名
     * @param columns カラム定義
     * @return CREATE TABLEクエリ
     */
    private String buildCreateTableQuery(String tableName, Map<String, String> columns) {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        
        // IDカラムを自動追加
        switch (config.getType()) {
            case MYSQL:
                query.append("id INT AUTO_INCREMENT PRIMARY KEY, ");
                break;
            case POSTGRESQL:
                query.append("id SERIAL PRIMARY KEY, ");
                break;
            case SQLITE:
                query.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");
                break;
        }
        
        // カスタムカラムを追加
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            query.append(entry.getKey()).append(" ").append(entry.getValue()).append(", ");
        }
        
        // 最後のカンマを削除
        query.setLength(query.length() - 2);
        query.append(")");
        
        return query.toString();
    }
    
    /**
     * データベースマネージャーをシャットダウンします
     */
    public void shutdown() {
        if (connectionManager != null) {
            connectionManager.close();
        }
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        initialized = false;
        logger.info("Database manager shutdown completed");
    }
    
    /**
     * 初期化状態を確認します
     * 
     * @return 初期化済みの場合true
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * データベース設定を取得します
     * 
     * @return データベース設定
     */
    public DatabaseConfig getConfig() {
        return config;
    }
}
