# TempceLib データベース統合機能

TempceLibのデータベース統合機能は、Minecraftプラグインでのデータベース操作を簡単かつ効率的に行うための包括的なソリューションです。

## 🗄️ サポートするデータベース

- **SQLite** - ファイルベースの軽量データベース（デフォルト）
- **MySQL** - 高性能なリレーショナルデータベース
- **PostgreSQL** - 高機能なオープンソースデータベース

## ⚙️ 設定

### database.yml の設定例

```yaml
database:
  # データベース種別 (sqlite, mysql, postgresql)
  type: "sqlite"
  
  # MySQL/PostgreSQL用設定
  host: "localhost"
  port: 3306
  name: "tempcelib"
  username: "root"
  password: ""
  
  # SQLite用設定
  file: "plugins/TempceLib/database.db"
  
  # テーブルプリフィックス
  table-prefix: "tempce_"
  
  # SSL使用設定
  use-ssl: false
  
  # コネクションプール設定
  pool:
    max-size: 10
    min-size: 1
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000

# パフォーマンス設定
performance:
  enable-query-cache: true
  auto-optimize-interval: 60
  slow-query-threshold: 5000
```

## 🔧 使用方法

### 基本的なデータベース操作

```java
import com.Tempce.tempceLib.api.DatabaseAPI;

// テーブル作成
Map<String, String> columns = new HashMap<>();
columns.put("player_name", "VARCHAR(16)");
columns.put("score", "INT");
columns.put("last_login", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

DatabaseAPI.createTable("player_scores", columns).thenAccept(success -> {
    if (success) {
        plugin.getLogger().info("テーブルが作成されました");
    }
});

// データ挿入
DatabaseAPI.executeUpdateAsync(
    "INSERT INTO tempce_player_scores (player_name, score) VALUES (?, ?)",
    "PlayerName", 1000
).thenAccept(result -> {
    plugin.getLogger().info("データが挿入されました: " + result + "行");
});

// データ取得
DatabaseAPI.executeQueryAsync(
    "SELECT * FROM tempce_player_scores WHERE player_name = ?",
    "PlayerName"
).thenAccept(result -> {
    if (!result.isEmpty()) {
        Map<String, Object> row = result.getFirstRow();
        String playerName = (String) row.get("player_name");
        Integer score = (Integer) row.get("score");
        plugin.getLogger().info("プレイヤー: " + playerName + ", スコア: " + score);
    }
});
```

### バッチ処理

```java
// 複数のデータを一度に挿入
List<Object[]> parametersList = new ArrayList<>();
parametersList.add(new Object[]{"Player1", 1000});
parametersList.add(new Object[]{"Player2", 1500});
parametersList.add(new Object[]{"Player3", 2000});

DatabaseAPI.executeBatchAsync(
    "INSERT INTO tempce_player_scores (player_name, score) VALUES (?, ?)",
    parametersList
).thenAccept(results -> {
    plugin.getLogger().info("バッチ処理完了: " + results.length + "件");
});
```

### データベース最適化

```java
// データベース全体を最適化
DatabaseAPI.optimizeDatabase().thenAccept(success -> {
    if (success) {
        plugin.getLogger().info("データベースが最適化されました");
    }
});

// 重複データのクリーンアップ
DatabaseAPI.cleanupDuplicates("player_scores", "player_name").thenAccept(deletedRows -> {
    plugin.getLogger().info("重複データ " + deletedRows + "行を削除しました");
});
```

### データベース統計情報

```java
DatabaseAPI.getDatabaseStats().thenAccept(stats -> {
    plugin.getLogger().info("データベース種別: " + stats.get("database_type"));
    plugin.getLogger().info("テーブル数: " + stats.get("table_count"));
});
```

## 🎮 コマンド

TempceLibは以下のデータベース管理コマンドを提供します：

- `/tempcelib database info` - データベース情報を表示
- `/tempcelib database test` - データベース接続をテスト
- `/tempcelib database optimize` - データベースを最適化
- `/tempcelib database cleanup <table_name> <key_columns...>` - 重複データをクリーンアップ

## 🚀 パフォーマンス最適化機能

### 自動クエリ最適化
- SQLクエリの自動分析と最適化提案
- スロークエリの検出とログ出力
- データベース固有の最適化ルール適用

### コネクションプール管理
- HikariCPによる高性能なコネクションプール
- 設定可能なプール設定
- 接続統計情報の監視

### インデックス管理
- 自動インデックス提案
- 未使用インデックスの検出
- テーブル構造の最適化

### 軽量化機能
- 重複データの自動検出・削除
- データベースサイズの最適化
- 定期的なメンテナンス実行

## 📊 統計とモニタリング

データベース統合機能は以下の統計情報を提供します：

- クエリ実行統計（回数、平均実行時間、エラー率）
- コネクションプール状態
- テーブルサイズと行数
- インデックス使用状況
- パフォーマンスメトリクス

## 🔒 セキュリティ機能

- SQLインジェクション対策（プリペアードステートメント）
- 接続暗号化（SSL/TLS）
- 権限管理とアクセス制御
- エラー情報の適切な隠蔽

## 🔧 トラブルシューティング

### よくある問題

1. **データベースに接続できない**
   - `database.yml`の設定を確認してください
   - データベースサーバーが起動しているか確認してください
   - ネットワーク接続を確認してください

2. **パフォーマンスが悪い**
   - スロークエリログを確認してください
   - インデックスが適切に設定されているか確認してください
   - コネクションプールの設定を調整してください

3. **メモリ使用量が多い**
   - コネクションプールのサイズを調整してください
   - 不要なデータをクリーンアップしてください
   - データベースの最適化を実行してください

## 📝 ライセンス

このデータベース統合機能はTempceLibの一部として提供されます。
