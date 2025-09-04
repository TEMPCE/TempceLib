package com.Tempce.tempceLib.api;

import com.Tempce.tempceLib.database.manager.DatabaseManager;
import com.Tempce.tempceLib.database.data.QueryResult;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * データベース統合機能のAPIクラス
 * 
 * このクラスは、複数のDBMS（MySQL、SQLite、PostgreSQL）をサポートし、
 * 最適化されたクエリ実行とパフォーマンス管理機能を提供します。
 * 
 * @author TempceLib Development Team
 * @version 1.0.0
 */
public class DatabaseAPI {
    
    private static DatabaseManager databaseManager;
    
    /**
     * データベースAPIを初期化します
     * 
     * @param plugin プラグインインスタンス
     */
    public static void initialize(JavaPlugin plugin) {
        databaseManager = new DatabaseManager(plugin);
    }
    
    /**
     * データベースマネージャーを取得します
     * 
     * @return データベースマネージャーのインスタンス
     */
    public static DatabaseManager getManager() {
        if (databaseManager == null) {
            throw new IllegalStateException("DatabaseAPI is not initialized. Call DatabaseAPI.initialize() first.");
        }
        return databaseManager;
    }
    
    /**
     * SQLクエリを非同期で実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parameters クエリパラメータ
     * @return クエリ結果のCompletableFuture
     */
    public static CompletableFuture<QueryResult> executeQueryAsync(String query, Object... parameters) {
        return getManager().executeQueryAsync(query, parameters);
    }
    
    /**
     * SQLアップデート（INSERT、UPDATE、DELETE）を非同期で実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parameters クエリパラメータ
     * @return 影響を受けた行数のCompletableFuture
     */
    public static CompletableFuture<Integer> executeUpdateAsync(String query, Object... parameters) {
        return getManager().executeUpdateAsync(query, parameters);
    }
    
    /**
     * バッチ処理でクエリを実行します
     * 
     * @param query 実行するSQLクエリ
     * @param parametersList パラメータのリスト
     * @return 実行結果のCompletableFuture
     */
    public static CompletableFuture<int[]> executeBatchAsync(String query, List<Object[]> parametersList) {
        return getManager().executeBatchAsync(query, parametersList);
    }
    
    /**
     * テーブルを作成します
     * 
     * @param tableName テーブル名
     * @param columns カラム定義のマップ（カラム名 -> データ型）
     * @return 作成成功のCompletableFuture
     */
    public static CompletableFuture<Boolean> createTable(String tableName, Map<String, String> columns) {
        return getManager().createTable(tableName, columns);
    }
    
    /**
     * テーブルが存在するかチェックします
     * 
     * @param tableName テーブル名
     * @return 存在する場合trueのCompletableFuture
     */
    public static CompletableFuture<Boolean> tableExists(String tableName) {
        return getManager().tableExists(tableName);
    }
    
    /**
     * データベース統計情報を取得します
     * 
     * @return 統計情報のマップ
     */
    public static CompletableFuture<Map<String, Object>> getDatabaseStats() {
        return getManager().getDatabaseStats();
    }
    
    /**
     * データベース最適化を実行します
     * 
     * @return 最適化結果のCompletableFuture
     */
    public static CompletableFuture<Boolean> optimizeDatabase() {
        return getManager().optimizeDatabase();
    }
    
    /**
     * 重複データのクリーンアップを実行します
     * 
     * @param tableName テーブル名
     * @param keyColumns 重複チェック対象のカラム
     * @return クリーンアップされた行数のCompletableFuture
     */
    public static CompletableFuture<Integer> cleanupDuplicates(String tableName, String... keyColumns) {
        return getManager().cleanupDuplicates(tableName, keyColumns);
    }
}
