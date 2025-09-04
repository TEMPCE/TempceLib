package com.Tempce.tempceLib.database.optimizer;

/**
 * インデックス使用統計情報を保持するクラス
 */
public class IndexUsageStats {
    
    private final String indexName;
    private long cardinality;
    private boolean nullable;
    private String indexType;
    private long scanCount;
    private long tupleReads;
    private long tupleFetches;
    private int columnCount;
    private long lastUsed;
    private double selectivity;
    
    /**
     * コンストラクタ
     * 
     * @param indexName インデックス名
     */
    public IndexUsageStats(String indexName) {
        this.indexName = indexName;
        this.lastUsed = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getIndexName() {
        return indexName;
    }
    
    public long getCardinality() {
        return cardinality;
    }
    
    public void setCardinality(long cardinality) {
        this.cardinality = cardinality;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    
    public String getIndexType() {
        return indexType;
    }
    
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }
    
    public long getScanCount() {
        return scanCount;
    }
    
    public void setScanCount(long scanCount) {
        this.scanCount = scanCount;
    }
    
    public long getTupleReads() {
        return tupleReads;
    }
    
    public void setTupleReads(long tupleReads) {
        this.tupleReads = tupleReads;
    }
    
    public long getTupleFetches() {
        return tupleFetches;
    }
    
    public void setTupleFetches(long tupleFetches) {
        this.tupleFetches = tupleFetches;
    }
    
    public int getColumnCount() {
        return columnCount;
    }
    
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
    
    public long getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public double getSelectivity() {
        return selectivity;
    }
    
    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }
    
    /**
     * インデックスが使用されているかチェック
     * 
     * @return 使用されている場合true
     */
    public boolean isUsed() {
        return scanCount > 0 || tupleReads > 0;
    }
    
    /**
     * インデックス効率の計算
     * 
     * @return 効率スコア (0.0 - 1.0)
     */
    public double getEfficiencyScore() {
        if (scanCount == 0) return 0.0;
        
        // スキャン回数に対するフェッチ効率
        double fetchEfficiency = tupleFetches > 0 ? (double) tupleFetches / tupleReads : 0.0;
        
        // カーディナリティに基づく選択性
        double cardinalityScore = cardinality > 0 ? Math.min(1.0, cardinality / 1000.0) : 0.5;
        
        return (fetchEfficiency * 0.7) + (cardinalityScore * 0.3);
    }
    
    @Override
    public String toString() {
        return "IndexUsageStats{" +
                "indexName='" + indexName + '\'' +
                ", cardinality=" + cardinality +
                ", scanCount=" + scanCount +
                ", efficiency=" + String.format("%.2f", getEfficiencyScore()) +
                '}';
    }
}
