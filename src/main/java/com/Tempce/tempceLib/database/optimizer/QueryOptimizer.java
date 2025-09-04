package com.Tempce.tempceLib.database.optimizer;

import com.Tempce.tempceLib.database.config.DatabaseType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * データベースクエリの最適化を管理するクラス
 */
public class QueryOptimizer {
    
    private final Logger logger;
    private final DatabaseType databaseType;
    private final Map<String, QueryStats> queryStatsMap;
    private final Map<String, String> optimizedQueries;
    
    /**
     * コンストラクタ
     * 
     * @param databaseType データベースタイプ
     * @param logger ロガー
     */
    public QueryOptimizer(DatabaseType databaseType, Logger logger) {
        this.databaseType = databaseType;
        this.logger = logger;
        this.queryStatsMap = new ConcurrentHashMap<>();
        this.optimizedQueries = new ConcurrentHashMap<>();
    }
    
    /**
     * クエリを最適化します
     * 
     * @param originalQuery 元のクエリ
     * @return 最適化されたクエリ
     */
    public String optimizeQuery(String originalQuery) {
        String normalizedQuery = normalizeQuery(originalQuery);
        
        // 既に最適化済みの場合はキャッシュから返す
        if (optimizedQueries.containsKey(normalizedQuery)) {
            return optimizedQueries.get(normalizedQuery);
        }
        
        String optimizedQuery = performOptimization(originalQuery);
        optimizedQueries.put(normalizedQuery, optimizedQuery);
        
        return optimizedQuery;
    }
    
    /**
     * クエリの実行統計を記録します
     * 
     * @param query クエリ
     * @param executionTime 実行時間（ミリ秒）
     * @param rowCount 結果行数
     */
    public void recordQueryStats(String query, long executionTime, int rowCount) {
        String normalizedQuery = normalizeQuery(query);
        QueryStats stats = queryStatsMap.computeIfAbsent(normalizedQuery, k -> new QueryStats());
        stats.addExecution(executionTime, rowCount);
        
        // パフォーマンスのボトルネックを検出
        if (stats.getAverageExecutionTime() > 5000) { // 5秒以上
            logger.warning("Slow query detected (avg: " + stats.getAverageExecutionTime() + "ms): " + 
                    truncateQuery(query, 100));
        }
    }
    
    /**
     * クエリの最適化処理を実行します
     * 
     * @param query 元のクエリ
     * @return 最適化されたクエリ
     */
    private String performOptimization(String query) {
        String optimized = query;
        
        // 基本的な最適化
        optimized = optimizeSelect(optimized);
        optimized = optimizeJoins(optimized);
        optimized = optimizeWhere(optimized);
        optimized = addDatabaseSpecificOptimizations(optimized);
        
        return optimized;
    }
    
    /**
     * SELECT文を最適化します
     * 
     * @param query クエリ
     * @return 最適化されたクエリ
     */
    private String optimizeSelect(String query) {
        // SELECT * を避ける提案（実際の置換は行わない）
        if (query.toLowerCase().contains("select *")) {
            logger.info("Consider specifying column names instead of SELECT * for better performance");
        }
        
        return query;
    }
    
    /**
     * JOIN文を最適化します
     * 
     * @param query クエリ
     * @return 最適化されたクエリ
     */
    private String optimizeJoins(String query) {
        String lower = query.toLowerCase();
        
        // INNER JOINの明示的な使用を推奨
        if (lower.contains("join") && !lower.contains("inner join") && 
            !lower.contains("left join") && !lower.contains("right join")) {
            logger.info("Consider using explicit JOIN types (INNER JOIN, LEFT JOIN, etc.)");
        }
        
        return query;
    }
    
    /**
     * WHERE句を最適化します
     * 
     * @param query クエリ
     * @return 最適化されたクエリ
     */
    private String optimizeWhere(String query) {
        String lower = query.toLowerCase();
        
        // インデックスが効きにくいパターンの検出
        if (lower.contains("like '%")) {
            logger.info("Leading wildcard in LIKE clause may cause full table scan");
        }
        
        if (lower.contains("or")) {
            logger.info("OR conditions may prevent index usage, consider using UNION");
        }
        
        return query;
    }
    
    /**
     * データベース固有の最適化を追加します
     * 
     * @param query クエリ
     * @return 最適化されたクエリ
     */
    private String addDatabaseSpecificOptimizations(String query) {
        switch (databaseType) {
            case MYSQL:
                return optimizeForMySQL(query);
            case POSTGRESQL:
                return optimizeForPostgreSQL(query);
            case SQLITE:
                return optimizeForSQLite(query);
            default:
                return query;
        }
    }
    
    /**
     * MySQL固有の最適化
     */
    private String optimizeForMySQL(String query) {
        // LIMIT句の追加提案
        String lower = query.toLowerCase();
        if (lower.startsWith("select") && !lower.contains("limit")) {
            logger.info("Consider adding LIMIT clause for large result sets");
        }
        
        return query;
    }
    
    /**
     * PostgreSQL固有の最適化
     */
    private String optimizeForPostgreSQL(String query) {
        // ANALYZE実行の提案
        if (query.toLowerCase().startsWith("select")) {
            logger.fine("Consider running ANALYZE on tables for better query planning");
        }
        
        return query;
    }
    
    /**
     * SQLite固有の最適化
     */
    private String optimizeForSQLite(String query) {
        // PRAGMA最適化の提案
        logger.fine("SQLite performance can be improved with appropriate PRAGMA settings");
        
        return query;
    }
    
    /**
     * クエリを正規化します（統計とキャッシュのため）
     * 
     * @param query クエリ
     * @return 正規化されたクエリ
     */
    private String normalizeQuery(String query) {
        return query.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .replaceAll("'[^']*'", "'?'")  // 文字列リテラルを置換
                .replaceAll("\\d+", "?");      // 数値リテラルを置換
    }
    
    /**
     * クエリを指定された長さに切り詰めます
     * 
     * @param query クエリ
     * @param maxLength 最大長
     * @return 切り詰められたクエリ
     */
    private String truncateQuery(String query, int maxLength) {
        if (query.length() <= maxLength) {
            return query;
        }
        return query.substring(0, maxLength) + "...";
    }
    
    /**
     * クエリ統計情報を取得します
     * 
     * @return 統計情報のマップ
     */
    public Map<String, QueryStats> getQueryStats() {
        return new HashMap<>(queryStatsMap);
    }
    
    /**
     * 最適化されたクエリのキャッシュをクリアします
     */
    public void clearCache() {
        optimizedQueries.clear();
        logger.info("Query optimization cache cleared");
    }
    
    /**
     * クエリ統計情報のクラス
     */
    public static class QueryStats {
        private long totalExecutions = 0;
        private long totalExecutionTime = 0;
        private long totalRowCount = 0;
        private long minExecutionTime = Long.MAX_VALUE;
        private long maxExecutionTime = 0;
        
        public synchronized void addExecution(long executionTime, int rowCount) {
            totalExecutions++;
            totalExecutionTime += executionTime;
            totalRowCount += rowCount;
            minExecutionTime = Math.min(minExecutionTime, executionTime);
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        }
        
        public long getTotalExecutions() {
            return totalExecutions;
        }
        
        public long getAverageExecutionTime() {
            return totalExecutions > 0 ? totalExecutionTime / totalExecutions : 0;
        }
        
        public long getMinExecutionTime() {
            return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }
        
        public long getAverageRowCount() {
            return totalExecutions > 0 ? totalRowCount / totalExecutions : 0;
        }
    }
}
