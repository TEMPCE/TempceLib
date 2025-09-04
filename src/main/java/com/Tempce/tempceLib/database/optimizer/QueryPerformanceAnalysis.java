package com.Tempce.tempceLib.database.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * クエリパフォーマンス分析結果を保持するクラス
 */
public class QueryPerformanceAnalysis {
    
    private final String query;
    private long executionTime; // ミリ秒
    private final List<TableScanInfo> tableScans;
    private final List<String> planInfo;
    private final Map<String, Object> metrics;
    
    /**
     * コンストラクタ
     * 
     * @param query 分析対象クエリ
     */
    public QueryPerformanceAnalysis(String query) {
        this.query = query;
        this.tableScans = new ArrayList<>();
        this.planInfo = new ArrayList<>();
        this.metrics = new HashMap<>();
    }
    
    /**
     * テーブルスキャン情報を追加
     * 
     * @param table テーブル名
     * @param scanType スキャンタイプ
     * @param indexUsed 使用されたインデックス
     * @param rowsExamined 検査された行数
     */
    public void addTableScan(String table, String scanType, String indexUsed, Long rowsExamined) {
        tableScans.add(new TableScanInfo(table, scanType, indexUsed, rowsExamined));
    }
    
    /**
     * 実行計画情報を追加
     * 
     * @param info 実行計画情報
     */
    public void addPlanInfo(String info) {
        planInfo.add(info);
    }
    
    /**
     * メトリクスを追加
     * 
     * @param key メトリクス名
     * @param value 値
     */
    public void addMetric(String key, Object value) {
        metrics.put(key, value);
    }
    
    // Getters
    public String getQuery() {
        return query;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public List<TableScanInfo> getTableScans() {
        return tableScans;
    }
    
    public List<String> getPlanInfo() {
        return planInfo;
    }
    
    public Map<String, Object> getMetrics() {
        return metrics;
    }
    
    /**
     * パフォーマンススコアを計算
     * 
     * @return パフォーマンススコア (0.0 - 1.0, 1.0が最高)
     */
    public double getPerformanceScore() {
        double score = 1.0;
        
        // 実行時間ペナルティ
        if (executionTime > 1000) { // 1秒以上
            score *= 0.3;
        } else if (executionTime > 100) { // 100ms以上
            score *= 0.7;
        }
        
        // フルテーブルスキャンペナルティ
        for (TableScanInfo scan : tableScans) {
            if ("ALL".equals(scan.getScanType()) || "FULL".equals(scan.getScanType())) {
                score *= 0.5;
            }
        }
        
        return score;
    }
    
    /**
     * 最適化の推奨事項を取得
     * 
     * @return 推奨事項リスト
     */
    public List<String> getOptimizationSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // フルテーブルスキャンの検出
        for (TableScanInfo scan : tableScans) {
            if ("ALL".equals(scan.getScanType()) || "FULL".equals(scan.getScanType())) {
                suggestions.add("テーブル " + scan.getTable() + " でフルスキャンが発生しています。適切なインデックスの追加を検討してください。");
            }
            
            if (scan.getRowsExamined() != null && scan.getRowsExamined() > 10000) {
                suggestions.add("テーブル " + scan.getTable() + " で大量の行（" + scan.getRowsExamined() + "）が検査されています。WHERE句の最適化を検討してください。");
            }
        }
        
        // 実行時間の警告
        if (executionTime > 1000) {
            suggestions.add("クエリの実行時間が長すぎます（" + executionTime + "ms）。クエリの最適化が必要です。");
        }
        
        return suggestions;
    }
    
    @Override
    public String toString() {
        return "QueryPerformanceAnalysis{" +
                "executionTime=" + executionTime + "ms" +
                ", tableScans=" + tableScans.size() +
                ", performanceScore=" + String.format("%.2f", getPerformanceScore()) +
                '}';
    }
    
    /**
     * テーブルスキャン情報を保持するクラス
     */
    public static class TableScanInfo {
        private final String table;
        private final String scanType;
        private final String indexUsed;
        private final Long rowsExamined;
        
        public TableScanInfo(String table, String scanType, String indexUsed, Long rowsExamined) {
            this.table = table;
            this.scanType = scanType;
            this.indexUsed = indexUsed;
            this.rowsExamined = rowsExamined;
        }
        
        public String getTable() {
            return table;
        }
        
        public String getScanType() {
            return scanType;
        }
        
        public String getIndexUsed() {
            return indexUsed;
        }
        
        public Long getRowsExamined() {
            return rowsExamined;
        }
        
        @Override
        public String toString() {
            return "TableScan{" +
                    "table='" + table + '\'' +
                    ", type='" + scanType + '\'' +
                    ", index='" + indexUsed + '\'' +
                    ", rows=" + rowsExamined +
                    '}';
        }
    }
}
