package com.Tempce.tempceLib.database.optimizer;

import com.Tempce.tempceLib.database.config.DatabaseType;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * データベースの軽量化とクリーンアップ機能を提供するクラス
 */
public class DatabaseOptimizer {
    
    private final Logger logger;
    private final DatabaseType databaseType;
    private final String tablePrefix;
    
    /**
     * コンストラクタ
     * 
     * @param databaseType データベースタイプ
     * @param tablePrefix テーブルプレフィックス
     * @param logger ロガー
     */
    public DatabaseOptimizer(DatabaseType databaseType, String tablePrefix, Logger logger) {
        this.databaseType = databaseType;
        this.tablePrefix = tablePrefix;
        this.logger = logger;
    }
    
    /**
     * データベース全体を最適化します
     * 
     * @param connection データベース接続
     * @return 最適化結果
     * @throws SQLException SQL例外
     */
    public OptimizationResult optimizeDatabase(Connection connection) throws SQLException {
        OptimizationResult result = new OptimizationResult();
        
        logger.info("Starting database optimization...");
        
        // テーブル一覧を取得
        List<String> tables = getTables(connection);
        
        for (String table : tables) {
            // インデックスの最適化
            optimizeIndexes(connection, table, result);
            
            // テーブルの最適化
            optimizeTable(connection, table, result);
        }
        
        // データベース固有の最適化
        performDatabaseSpecificOptimization(connection, result);
        
        logger.info("Database optimization completed: " + result.toString());
        
        return result;
    }
    
    /**
     * 重複データをクリーンアップします
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param keyColumns 重複チェック対象のカラム
     * @return クリーンアップされた行数
     * @throws SQLException SQL例外
     */
    public int cleanupDuplicates(Connection connection, String tableName, String[] keyColumns) throws SQLException {
        if (keyColumns == null || keyColumns.length == 0) {
            throw new IllegalArgumentException("Key columns must be specified");
        }
        
        String fullTableName = tablePrefix + tableName;
        logger.info("Cleaning up duplicates in table: " + fullTableName);
        
        // 重複データの特定と削除
        String duplicateQuery = buildDuplicateQuery(fullTableName, keyColumns);
        
        try (PreparedStatement stmt = connection.prepareStatement(duplicateQuery)) {
            int deletedRows = stmt.executeUpdate();
            logger.info("Removed " + deletedRows + " duplicate rows from " + fullTableName);
            return deletedRows;
        }
    }
    
    /**
     * 不要なインデックスを検出し削除します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @return 削除されたインデックス数
     * @throws SQLException SQL例外
     */
    public int cleanupUnusedIndexes(Connection connection, String tableName) throws SQLException {
        String fullTableName = tablePrefix + tableName;
        List<String> unusedIndexes = findUnusedIndexes(connection, fullTableName);
        
        int removedCount = 0;
        for (String indexName : unusedIndexes) {
            if (dropIndex(connection, indexName, fullTableName)) {
                removedCount++;
                logger.info("Dropped unused index: " + indexName);
            }
        }
        
        return removedCount;
    }
    
    /**
     * テーブル一覧を取得します
     * 
     * @param connection データベース接続
     * @return テーブル名のリスト
     * @throws SQLException SQL例外
     */
    private List<String> getTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet rs = metaData.getTables(null, null, tablePrefix + "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        
        return tables;
    }
    
    /**
     * インデックスを最適化します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void optimizeIndexes(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        // 既存インデックスの分析
        analyzeExistingIndexes(connection, tableName, result);
        
        // 推奨インデックスの提案
        suggestIndexes(connection, tableName, result);
    }
    
    /**
     * 既存インデックスを分析します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void analyzeExistingIndexes(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, true)) {
            Set<String> indexes = new HashSet<>();
            Map<String, IndexUsageStats> indexStats = new HashMap<>();
            
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null && !indexName.equals("PRIMARY")) {
                    indexes.add(indexName);
                    
                    // インデックス使用統計を収集
                    IndexUsageStats stats = collectIndexUsageStats(connection, tableName, indexName);
                    indexStats.put(indexName, stats);
                    result.addIndexStats(tableName, indexName, stats);
                }
            }
            result.addAnalyzedIndexes(tableName, indexes.size());
            
            // 重複インデックスの検出
            detectDuplicateIndexes(connection, tableName, result);
        }
    }
    
    /**
     * インデックス使用統計を収集します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param indexName インデックス名
     * @return インデックス使用統計
     * @throws SQLException SQL例外
     */
    private IndexUsageStats collectIndexUsageStats(Connection connection, String tableName, String indexName) throws SQLException {
        IndexUsageStats stats = new IndexUsageStats(indexName);
        
        switch (databaseType) {
            case MYSQL:
                collectMySQLIndexStats(connection, tableName, indexName, stats);
                break;
            case POSTGRESQL:
                collectPostgreSQLIndexStats(connection, tableName, indexName, stats);
                break;
            case SQLITE:
                collectSQLiteIndexStats(connection, tableName, indexName, stats);
                break;
        }
        
        return stats;
    }
    
    /**
     * MySQL用インデックス統計収集
     */
    private void collectMySQLIndexStats(Connection connection, String tableName, String indexName, IndexUsageStats stats) throws SQLException {
        // MySQL INFORMATION_SCHEMA から統計情報を取得
        String query = "SELECT CARDINALITY, NULLABLE, INDEX_TYPE FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = ? AND INDEX_NAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableName);
            stmt.setString(2, indexName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setCardinality(rs.getLong("CARDINALITY"));
                    stats.setNullable(rs.getString("NULLABLE").equals("YES"));
                    stats.setIndexType(rs.getString("INDEX_TYPE"));
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to collect MySQL index stats for " + indexName + ": " + e.getMessage());
        }
    }
    
    /**
     * PostgreSQL用インデックス統計収集
     */
    private void collectPostgreSQLIndexStats(Connection connection, String tableName, String indexName, IndexUsageStats stats) throws SQLException {
        // PostgreSQL pg_stat_user_indexes から統計情報を取得
        String query = "SELECT idx_scan, idx_tup_read, idx_tup_fetch FROM pg_stat_user_indexes WHERE indexrelname = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, indexName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setScanCount(rs.getLong("idx_scan"));
                    stats.setTupleReads(rs.getLong("idx_tup_read"));
                    stats.setTupleFetches(rs.getLong("idx_tup_fetch"));
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to collect PostgreSQL index stats for " + indexName + ": " + e.getMessage());
        }
    }
    
    /**
     * SQLite用インデックス統計収集（簡易版）
     */
    private void collectSQLiteIndexStats(Connection connection, String tableName, String indexName, IndexUsageStats stats) throws SQLException {
        // SQLiteは詳細な統計情報が限定的なため、基本情報のみ収集
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA index_info('" + indexName + "')");
            int columnCount = 0;
            while (rs.next()) {
                columnCount++;
            }
            stats.setColumnCount(columnCount);
        } catch (SQLException e) {
            logger.warning("Failed to collect SQLite index info for " + indexName + ": " + e.getMessage());
        }
    }
    
    /**
     * 重複インデックスを検出します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void detectDuplicateIndexes(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        Map<String, List<String>> indexColumns = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        // 各インデックスのカラム構成を取得
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                
                if (indexName != null && !indexName.equals("PRIMARY") && columnName != null) {
                    indexColumns.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnName);
                }
            }
        }
        
        // 重複する組み合わせを検出
        List<String> indexNames = new ArrayList<>(indexColumns.keySet());
        for (int i = 0; i < indexNames.size(); i++) {
            for (int j = i + 1; j < indexNames.size(); j++) {
                String index1 = indexNames.get(i);
                String index2 = indexNames.get(j);
                
                List<String> columns1 = indexColumns.get(index1);
                List<String> columns2 = indexColumns.get(index2);
                
                if (isIndexDuplicate(columns1, columns2)) {
                    result.addDuplicateIndex(tableName, index1, index2);
                    logger.info("Detected duplicate indexes: " + index1 + " and " + index2);
                }
            }
        }
    }
    
    /**
     * インデックスが重複しているかチェック
     * 
     * @param columns1 インデックス1のカラム
     * @param columns2 インデックス2のカラム
     * @return 重複している場合true
     */
    private boolean isIndexDuplicate(List<String> columns1, List<String> columns2) {
        // 完全一致
        if (columns1.equals(columns2)) {
            return true;
        }
        
        // 前方一致（より短いインデックスがより長いインデックスの前方部分と一致）
        if (columns1.size() < columns2.size()) {
            return columns2.subList(0, columns1.size()).equals(columns1);
        } else if (columns2.size() < columns1.size()) {
            return columns1.subList(0, columns2.size()).equals(columns2);
        }
        
        return false;
    }
    
    /**
     * クエリパフォーマンスを分析します
     * 
     * @param connection データベース接続
     * @param query 分析対象のクエリ
     * @return パフォーマンス分析結果
     * @throws SQLException SQL例外
     */
    public QueryPerformanceAnalysis analyzeQueryPerformance(Connection connection, String query) throws SQLException {
        QueryPerformanceAnalysis analysis = new QueryPerformanceAnalysis(query);
        
        switch (databaseType) {
            case MYSQL:
                analyzeMySQLQueryPerformance(connection, query, analysis);
                break;
            case POSTGRESQL:
                analyzePostgreSQLQueryPerformance(connection, query, analysis);
                break;
            case SQLITE:
                analyzeSQLiteQueryPerformance(connection, query, analysis);
                break;
        }
        
        return analysis;
    }
    
    /**
     * MySQLクエリパフォーマンス分析
     */
    private void analyzeMySQLQueryPerformance(Connection connection, String query, QueryPerformanceAnalysis analysis) throws SQLException {
        // EXPLAIN による実行計画の取得
        try (PreparedStatement stmt = connection.prepareStatement("EXPLAIN " + query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String table = rs.getString("table");
                    String type = rs.getString("type");
                    String key = rs.getString("key");
                    Long rows = rs.getLong("rows");
                    
                    analysis.addTableScan(table, type, key, rows);
                }
            }
        }
        
        // 実行時間の測定
        long startTime = System.nanoTime();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 結果を消費（実際の処理時間を測定）
                }
            }
        }
        long endTime = System.nanoTime();
        analysis.setExecutionTime((endTime - startTime) / 1_000_000); // ミリ秒
    }
    
    /**
     * PostgreSQLクエリパフォーマンス分析
     */
    private void analyzePostgreSQLQueryPerformance(Connection connection, String query, QueryPerformanceAnalysis analysis) throws SQLException {
        // EXPLAIN ANALYZE による詳細分析
        try (PreparedStatement stmt = connection.prepareStatement("EXPLAIN ANALYZE " + query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String planInfo = rs.getString(1);
                    analysis.addPlanInfo(planInfo);
                }
            }
        }
    }
    
    /**
     * SQLiteクエリパフォーマンス分析
     */
    private void analyzeSQLiteQueryPerformance(Connection connection, String query, QueryPerformanceAnalysis analysis) throws SQLException {
        // EXPLAIN QUERY PLAN による実行計画の取得
        try (PreparedStatement stmt = connection.prepareStatement("EXPLAIN QUERY PLAN " + query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String detail = rs.getString("detail");
                    analysis.addPlanInfo(detail);
                }
            }
        }
        
        // 実行時間の測定
        long startTime = System.nanoTime();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 結果を消費
                }
            }
        }
        long endTime = System.nanoTime();
        analysis.setExecutionTime((endTime - startTime) / 1_000_000);
    }
    
    /**
     * 自動最適化スケジューラー
     * 定期的にデータベースの最適化を実行します
     */
    public static class OptimizationScheduler {
        private final DatabaseOptimizer optimizer;
        private final long intervalMillis;
        private volatile boolean running;
        private Thread schedulerThread;
        
        /**
         * コンストラクタ
         * 
         * @param optimizer データベース最適化オブジェクト
         * @param intervalHours 最適化間隔（時間）
         */
        public OptimizationScheduler(DatabaseOptimizer optimizer, int intervalHours) {
            this.optimizer = optimizer;
            this.intervalMillis = intervalHours * 60 * 60 * 1000L;
        }
        
        /**
         * スケジューラーを開始
         */
        public void start() {
            if (running) return;
            
            running = true;
            schedulerThread = new Thread(this::run, "DB-Optimizer-Scheduler");
            schedulerThread.setDaemon(true);
            schedulerThread.start();
            
            optimizer.logger.info("Database optimization scheduler started (interval: " + (intervalMillis / 3600000) + " hours)");
        }
        
        /**
         * スケジューラーを停止
         */
        public void stop() {
            running = false;
            if (schedulerThread != null) {
                schedulerThread.interrupt();
            }
            optimizer.logger.info("Database optimization scheduler stopped");
        }
        
        /**
         * スケジューラーのメインループ
         */
        private void run() {
            while (running) {
                try {
                    Thread.sleep(intervalMillis);
                    
                    if (running) {
                        performScheduledOptimization();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    optimizer.logger.warning("Error during scheduled optimization: " + e.getMessage());
                }
            }
        }
        
        /**
         * スケジュールされた最適化を実行
         */
        private void performScheduledOptimization() {
            try {
                optimizer.logger.info("Starting scheduled database optimization...");
                
                // ここで実際のデータベース接続を取得して最適化を実行
                // 注意: 実際の実装では適切な接続管理が必要
                
                optimizer.logger.info("Scheduled database optimization completed");
            } catch (Exception e) {
                optimizer.logger.severe("Failed to perform scheduled optimization: " + e.getMessage());
            }
        }
    }
    
    /**
     * インデックス推奨エンジン
     * クエリパターンを分析してインデックスを推奨します
     */
    public static class IndexRecommendationEngine {
        private final Map<String, Integer> columnAccess;
        
        public IndexRecommendationEngine() {
            this.columnAccess = new HashMap<>();
        }
        
        /**
         * クエリを分析してパターンを記録
         * 
         * @param query 実行されたクエリ
         */
        public void analyzeQuery(String query) {
            // WHERE句のカラムを抽出
            extractWhereColumns(query).forEach(column -> 
                columnAccess.merge(column, 1, Integer::sum)
            );
            
            // JOIN句のカラムを抽出
            extractJoinColumns(query).forEach(column -> 
                columnAccess.merge(column, 2, Integer::sum) // JOINは重要度が高い
            );
            
            // ORDER BY句のカラムを抽出
            extractOrderByColumns(query).forEach(column -> 
                columnAccess.merge(column, 1, Integer::sum)
            );
        }
        
        /**
         * インデックス推奨リストを生成
         * 
         * @param threshold 推奨閾値
         * @return 推奨インデックスリスト
         */
        public List<String> getRecommendations(int threshold) {
            return columnAccess.entrySet().stream()
                    .filter(entry -> entry.getValue() >= threshold)
                    .map(entry -> "CREATE INDEX idx_" + entry.getKey().replace(".", "_") + " ON " + 
                          extractTableName(entry.getKey()) + " (" + extractColumnName(entry.getKey()) + ")")
                    .collect(java.util.stream.Collectors.toList());
        }
        
        /**
         * WHERE句からカラムを抽出（簡易版）
         */
        private List<String> extractWhereColumns(String query) {
            List<String> columns = new ArrayList<>();
            String upperQuery = query.toUpperCase();
            
            int whereIndex = upperQuery.indexOf("WHERE");
            if (whereIndex > 0) {
                String whereClause = query.substring(whereIndex + 5);
                // 簡易的なカラム抽出（実際にはより複雑な解析が必要）
                String[] parts = whereClause.split("(?i)\\s+(AND|OR)\\s+");
                for (String part : parts) {
                    if (part.contains("=") || part.contains(">") || part.contains("<")) {
                        String[] sides = part.split("[=><]");
                        if (sides.length > 0) {
                            String column = sides[0].trim();
                            if (!column.matches(".*\\d.*") && !column.startsWith("'") && !column.startsWith("\"")) {
                                columns.add(column);
                            }
                        }
                    }
                }
            }
            
            return columns;
        }
        
        /**
         * JOIN句からカラムを抽出（簡易版）
         */
        private List<String> extractJoinColumns(String query) {
            List<String> columns = new ArrayList<>();
            String upperQuery = query.toUpperCase();
            
            int joinIndex = upperQuery.indexOf("JOIN");
            while (joinIndex > 0) {
                int onIndex = upperQuery.indexOf("ON", joinIndex);
                if (onIndex > 0) {
                    int nextJoinIndex = upperQuery.indexOf("JOIN", onIndex);
                    String onClause = nextJoinIndex > 0 ? 
                            query.substring(onIndex + 2, nextJoinIndex) : 
                            query.substring(onIndex + 2);
                    
                    if (onClause.contains("=")) {
                        String[] sides = onClause.split("=");
                        for (String side : sides) {
                            String column = side.trim();
                            if (column.contains(".")) {
                                columns.add(column);
                            }
                        }
                    }
                }
                joinIndex = upperQuery.indexOf("JOIN", joinIndex + 1);
            }
            
            return columns;
        }
        
        /**
         * ORDER BY句からカラムを抽出（簡易版）
         */
        private List<String> extractOrderByColumns(String query) {
            List<String> columns = new ArrayList<>();
            String upperQuery = query.toUpperCase();
            
            int orderByIndex = upperQuery.indexOf("ORDER BY");
            if (orderByIndex > 0) {
                String orderByClause = query.substring(orderByIndex + 8);
                String[] parts = orderByClause.split(",");
                for (String part : parts) {
                    String column = part.trim().split("\\s+")[0];
                    columns.add(column);
                }
            }
            
            return columns;
        }
        
        private String extractTableName(String fullColumn) {
            return fullColumn.contains(".") ? fullColumn.split("\\.")[0] : "unknown_table";
        }
        
        private String extractColumnName(String fullColumn) {
            return fullColumn.contains(".") ? fullColumn.split("\\.")[1] : fullColumn;
        }
    }
    
    /**
     * 推奨インデックスを提案します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void suggestIndexes(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        // カラム使用頻度の分析（簡略化）
        List<String> frequentColumns = analyzeColumnUsage(connection, tableName);
        
        for (String column : frequentColumns) {
            if (!hasIndexOnColumn(connection, tableName, column)) {
                result.addIndexSuggestion(tableName, column);
                logger.info("Suggested index on " + tableName + "." + column);
            }
        }
    }
    
    /**
     * カラム使用頻度を分析します（簡略版）
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @return 頻繁に使用されるカラムのリスト
     * @throws SQLException SQL例外
     */
    private List<String> analyzeColumnUsage(Connection connection, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                
                // 一般的にインデックスが有効なカラムタイプ
                if (isIndexableColumnType(dataType)) {
                    columns.add(columnName);
                }
            }
        }
        
        return columns;
    }
    
    /**
     * カラムタイプがインデックス可能かチェックします
     * 
     * @param dataType データタイプ
     * @return インデックス可能な場合true
     */
    private boolean isIndexableColumnType(String dataType) {
        String type = dataType.toLowerCase();
        return type.contains("int") || type.contains("varchar") || 
               type.contains("char") || type.contains("date") || 
               type.contains("time") || type.contains("decimal");
    }
    
    /**
     * 指定されたカラムにインデックスが存在するかチェックします
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param columnName カラム名
     * @return インデックスが存在する場合true
     * @throws SQLException SQL例外
     */
    private boolean hasIndexOnColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, true)) {
            while (rs.next()) {
                String indexedColumn = rs.getString("COLUMN_NAME");
                if (columnName.equals(indexedColumn)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * テーブルを最適化します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void optimizeTable(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        switch (databaseType) {
            case MYSQL:
                optimizeTableMySQL(connection, tableName, result);
                break;
            case POSTGRESQL:
                optimizeTablePostgreSQL(connection, tableName, result);
                break;
            case SQLITE:
                optimizeTableSQLite(connection, tableName, result);
                break;
        }
    }
    
    /**
     * MySQL用テーブル最適化
     */
    private void optimizeTableMySQL(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("OPTIMIZE TABLE " + tableName);
            result.addOptimizedTable(tableName);
        }
    }
    
    /**
     * PostgreSQL用テーブル最適化
     */
    private void optimizeTablePostgreSQL(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("VACUUM ANALYZE " + tableName);
            result.addOptimizedTable(tableName);
        }
    }
    
    /**
     * SQLite用テーブル最適化
     */
    private void optimizeTableSQLite(Connection connection, String tableName, OptimizationResult result) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("VACUUM");
            result.addOptimizedTable(tableName);
        }
    }
    
    /**
     * データベース固有の最適化を実行します
     * 
     * @param connection データベース接続
     * @param result 最適化結果
     * @throws SQLException SQL例外
     */
    private void performDatabaseSpecificOptimization(Connection connection, OptimizationResult result) throws SQLException {
        switch (databaseType) {
            case MYSQL:
                // ANALYZE TABLE for all tables
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ANALYZE TABLE " + String.join(", ", getTables(connection)));
                }
                break;
            case POSTGRESQL:
                // Update statistics
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ANALYZE");
                }
                break;
            case SQLITE:
                // Optimize pragma settings
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA optimize");
                }
                break;
        }
    }
    
    /**
     * 重複データ検出クエリを構築します
     * 
     * @param tableName テーブル名
     * @param keyColumns キーカラム
     * @return 重複削除クエリ
     */
    private String buildDuplicateQuery(String tableName, String[] keyColumns) {
        StringBuilder query = new StringBuilder();
        
        switch (databaseType) {
            case MYSQL:
                query.append("DELETE t1 FROM ").append(tableName).append(" t1 ")
                     .append("INNER JOIN ").append(tableName).append(" t2 ")
                     .append("WHERE t1.id > t2.id");
                
                for (String column : keyColumns) {
                    query.append(" AND t1.").append(column).append(" = t2.").append(column);
                }
                break;
                
            case POSTGRESQL:
                query.append("DELETE FROM ").append(tableName).append(" ")
                     .append("WHERE ctid NOT IN (")
                     .append("SELECT DISTINCT ON (").append(String.join(", ", keyColumns)).append(") ctid ")
                     .append("FROM ").append(tableName).append(" ")
                     .append("ORDER BY ").append(String.join(", ", keyColumns)).append(", ctid")
                     .append(")");
                break;
                
            case SQLITE:
                query.append("DELETE FROM ").append(tableName).append(" ")
                     .append("WHERE rowid NOT IN (")
                     .append("SELECT MIN(rowid) FROM ").append(tableName).append(" ")
                     .append("GROUP BY ").append(String.join(", ", keyColumns))
                     .append(")");
                break;
        }
        
        return query.toString();
    }
    
    /**
     * 未使用インデックスを検索します
     * 
     * @param connection データベース接続
     * @param tableName テーブル名
     * @return 未使用インデックスのリスト
     * @throws SQLException SQL例外
     */
    private List<String> findUnusedIndexes(Connection connection, String tableName) throws SQLException {
        List<String> unusedIndexes = new ArrayList<>();
        
        // 簡略化：実際のプロダクションでは使用統計を確認
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null && !indexName.equals("PRIMARY")) {
                    // 簡単なヒューリスティック：名前にtempが含まれる
                    if (indexName.toLowerCase().contains("temp")) {
                        unusedIndexes.add(indexName);
                    }
                }
            }
        }
        
        return unusedIndexes;
    }
    
    /**
     * インデックスを削除します
     * 
     * @param connection データベース接続
     * @param indexName インデックス名
     * @param tableName テーブル名
     * @return 削除成功の場合true
     */
    private boolean dropIndex(Connection connection, String indexName, String tableName) {
        try (Statement stmt = connection.createStatement()) {
            String dropQuery = buildDropIndexQuery(indexName, tableName);
            stmt.execute(dropQuery);
            return true;
        } catch (SQLException e) {
            logger.warning("Failed to drop index " + indexName + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * インデックス削除クエリを構築します
     * 
     * @param indexName インデックス名
     * @param tableName テーブル名
     * @return 削除クエリ
     */
    private String buildDropIndexQuery(String indexName, String tableName) {
        switch (databaseType) {
            case MYSQL:
                return "DROP INDEX " + indexName + " ON " + tableName;
            case POSTGRESQL:
            case SQLITE:
                return "DROP INDEX " + indexName;
            default:
                return "DROP INDEX " + indexName;
        }
    }
    
    /**
     * 最適化結果を格納するクラス
     */
    public static class OptimizationResult {
        private final Map<String, Integer> analyzedIndexes = new HashMap<>();
        private final Map<String, List<String>> indexSuggestions = new HashMap<>();
        private final List<String> optimizedTables = new ArrayList<>();
        private final Map<String, Map<String, IndexUsageStats>> indexStats = new HashMap<>();
        private final Map<String, List<String>> duplicateIndexes = new HashMap<>();
        private long executionTime;
        
        public void addAnalyzedIndexes(String tableName, int count) {
            analyzedIndexes.put(tableName, count);
        }
        
        public void addIndexSuggestion(String tableName, String column) {
            indexSuggestions.computeIfAbsent(tableName, k -> new ArrayList<>()).add(column);
        }
        
        public void addOptimizedTable(String tableName) {
            optimizedTables.add(tableName);
        }
        
        public void addIndexStats(String tableName, String indexName, IndexUsageStats stats) {
            indexStats.computeIfAbsent(tableName, k -> new HashMap<>()).put(indexName, stats);
        }
        
        public void addDuplicateIndex(String tableName, String index1, String index2) {
            duplicateIndexes.computeIfAbsent(tableName, k -> new ArrayList<>())
                           .add(index1 + " (duplicate of " + index2 + ")");
        }
        
        public void setExecutionTime(long executionTime) {
            this.executionTime = executionTime;
        }
        
        public Map<String, Integer> getAnalyzedIndexes() {
            return analyzedIndexes;
        }
        
        public Map<String, List<String>> getIndexSuggestions() {
            return indexSuggestions;
        }
        
        public List<String> getOptimizedTables() {
            return optimizedTables;
        }
        
        public Map<String, Map<String, IndexUsageStats>> getIndexStats() {
            return indexStats;
        }
        
        public Map<String, List<String>> getDuplicateIndexes() {
            return duplicateIndexes;
        }
        
        public long getExecutionTime() {
            return executionTime;
        }
        
        @Override
        public String toString() {
            return String.format("OptimizationResult{tables=%d, suggestions=%d, duplicates=%d, executionTime=%dms}",
                    optimizedTables.size(), indexSuggestions.size(), duplicateIndexes.size(), executionTime);
        }
    }
}
