package com.Tempce.tempceLib.database.data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * データベースクエリの結果を格納するクラス
 */
public class QueryResult {
    
    private final List<Map<String, Object>> rows;
    private final List<String> columnNames;
    private final int rowCount;
    private final long executionTime;
    
    /**
     * コンストラクタ
     * 
     * @param resultSet ResultSet
     * @param executionTime 実行時間（ミリ秒）
     * @throws SQLException SQL例外
     */
    public QueryResult(ResultSet resultSet, long executionTime) throws SQLException {
        this.executionTime = executionTime;
        this.rows = new ArrayList<>();
        this.columnNames = new ArrayList<>();
        
        if (resultSet != null) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // カラム名を取得
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            
            // データを取得
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                rows.add(row);
            }
        }
        
        this.rowCount = rows.size();
    }
    
    /**
     * 空の結果を作成するコンストラクタ
     * 
     * @param executionTime 実行時間（ミリ秒）
     */
    public QueryResult(long executionTime) {
        this.rows = new ArrayList<>();
        this.columnNames = new ArrayList<>();
        this.rowCount = 0;
        this.executionTime = executionTime;
    }
    
    /**
     * 全ての行を取得
     * 
     * @return 行のリスト
     */
    public List<Map<String, Object>> getRows() {
        return new ArrayList<>(rows);
    }
    
    /**
     * 指定されたインデックスの行を取得
     * 
     * @param index 行インデックス
     * @return 行データ
     * @throws IndexOutOfBoundsException インデックスが範囲外の場合
     */
    public Map<String, Object> getRow(int index) {
        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + index);
        }
        return new LinkedHashMap<>(rows.get(index));
    }
    
    /**
     * 最初の行を取得
     * 
     * @return 最初の行データ、存在しない場合はnull
     */
    public Map<String, Object> getFirstRow() {
        return rows.isEmpty() ? null : getRow(0);
    }
    
    /**
     * 指定されたカラムの値を全行から取得
     * 
     * @param columnName カラム名
     * @return 値のリスト
     */
    public List<Object> getColumnValues(String columnName) {
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            values.add(row.get(columnName));
        }
        return values;
    }
    
    /**
     * 指定された行とカラムの値を取得
     * 
     * @param rowIndex 行インデックス
     * @param columnName カラム名
     * @return 値
     */
    public Object getValue(int rowIndex, String columnName) {
        return getRow(rowIndex).get(columnName);
    }
    
    /**
     * カラム名のリストを取得
     * 
     * @return カラム名のリスト
     */
    public List<String> getColumnNames() {
        return new ArrayList<>(columnNames);
    }
    
    /**
     * 行数を取得
     * 
     * @return 行数
     */
    public int getRowCount() {
        return rowCount;
    }
    
    /**
     * 実行時間を取得
     * 
     * @return 実行時間（ミリ秒）
     */
    public long getExecutionTime() {
        return executionTime;
    }
    
    /**
     * 結果が空かどうかチェック
     * 
     * @return 空の場合true
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }
    
    /**
     * 指定されたカラムが存在するかチェック
     * 
     * @param columnName カラム名
     * @return 存在する場合true
     */
    public boolean hasColumn(String columnName) {
        return columnNames.contains(columnName);
    }
    
    @Override
    public String toString() {
        return String.format("QueryResult{rows=%d, columns=%s, executionTime=%dms}", 
                rowCount, columnNames, executionTime);
    }
}
