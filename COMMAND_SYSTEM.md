# TempceLib コマンドシステム

TempceLibのコマンドシステムは、アノテーションベースの簡単で強力なコマンド作成機能を提供します。

## 特徴

- **アノテーションベース**: `@Command`と`@SubCommand`で簡単にコマンドを定義
- **自動タブ補完**: コマンド名とサブコマンド名の自動補完
- **権限管理**: Spigotの権限システムと統合
- **クールダウン機能**: コマンドの再実行制限
- **エラーハンドリング**: 分かりやすいエラーメッセージ
- **エイリアス対応**: コマンドとサブコマンドの別名設定

## 基本的な使い方

### 1. コマンドクラスの作成

```java
@Command(
    name = "mycommand",           // コマンド名（省略時はクラス名をlowerCaseに変換）
    permission = "plugin.use",    // 実行権限（省略可）
    alias = {"mc", "mycmd"},      // エイリアス（省略可）
    timeout = 5,                  // クールダウン秒数（省略可、0で無制限）
    description = "説明文",        // コマンドの説明
    usage = "/mycommand <sub>"    // 使用方法
)
public class MyCommand {
    
    @SubCommand(
        name = "test",            // サブコマンド名（省略時はメソッド名）
        permission = "plugin.test", // 実行権限（省略可）
        alias = {"t"},            // エイリアス（省略可）
        timeout = 10,             // クールダウン秒数（省略可）
        description = "テスト",    // サブコマンドの説明
        usage = "test [arg]",     // 使用方法
        playerOnly = true         // プレイヤー限定（省略可、デフォルトfalse）
    )
    public void test(CommandSender sender, String[] args) {
        // サブコマンドの処理
        sender.sendMessage("テストコマンドが実行されました！");
    }
}
```

### 2. コマンドの登録

```java
// TempceLibのAPI経由で登録
CommandAPI.registerCommand(MyCommand.class);

// または、インスタンスを直接登録
CommandAPI.registerCommand(new MyCommand());

// プラグインから直接登録
TempceLib.getInstance().getCommandManager().registerCommand(new MyCommand());
```

## 権限

以下の権限が定義されています：

- `tempcelib.use` - 基本機能（デフォルト: true）

## 注意事項

1. サブコマンドメソッドのパラメータは必ず `(CommandSender, String[])` の形式にしてください
2. `playerOnly = true` を設定したサブコマンドはプレイヤーのみ実行可能です
3. クールダウンは送信者とコマンド/サブコマンドごとに個別に管理されます
4. 権限が設定されていない場合は誰でも実行可能です

## API利用例

```java
// 他のプラグインからTempceLibのコマンドシステムを利用
public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // コマンドを登録
        CommandAPI.registerCommand(YourCommand.class);
        
        // 統計情報を取得
        int commandCount = CommandAPI.getCommandCount();
        getLogger().info("登録コマンド数: " + commandCount);
    }
}
```
