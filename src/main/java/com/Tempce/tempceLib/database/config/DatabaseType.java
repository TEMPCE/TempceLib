package com.Tempce.tempceLib.database.config;

/**
 * データベース種別を表すEnum
 */
public enum DatabaseType {
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    SQLITE("sqlite", "org.sqlite.JDBC"),
    POSTGRESQL("postgresql", "org.postgresql.Driver");
    
    private final String name;
    private final String driverClass;
    
    DatabaseType(String name, String driverClass) {
        this.name = name;
        this.driverClass = driverClass;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDriverClass() {
        return driverClass;
    }
    
    /**
     * 文字列からDatabaseTypeを取得
     * 
     * @param name データベース種別名
     * @return DatabaseType
     * @throws IllegalArgumentException 不正な種別名の場合
     */
    public static DatabaseType fromString(String name) {
        for (DatabaseType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported database type: " + name);
    }
}
