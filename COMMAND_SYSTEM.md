# TempceLib コマンドシステム

TempceLibのコマンドシステムは、アノテーションベースの簡単で強力なコマンド作成機能を提供します。

## 特徴

- **アノテーションベース**: `@Command`と`@SubCommand`で簡単にコマンドを定義
- **多階層サブコマンド対応**: `config.reload`、`user.create.admin`などの多階層パス構造をサポート
- **pathベース設計**: `@SubCommand`の`path`属性でパス構造を定義
- **自動タブ補完**: コマンド名とサブコマンド名の自動補完（多階層対応）
- **部分パスヘルプ**: 未定義の部分パス入力時に下位サブコマンドのヘルプを自動表示
- **権限管理**: Spigotの権限システムと統合
- **クールダウン機能**: コマンドの再実行制限
- **エラーハンドリング**: 分かりやすいエラーメッセージ
- **エイリアス対応**: コマンドとサブコマンドの別名設定
- **自動ヘルプ生成**: helpサブコマンドの自動生成

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
    
    // 単一レベルサブコマンド
    @SubCommand(
        path = "test",            // サブコマンドのパス
        permission = "plugin.test", // 実行権限（省略可）
        alias = {"t"},            // エイリアス（省略可）
        timeout = 10,             // クールダウン秒数（省略可）
        description = "テスト",    // サブコマンドの説明
        usage = "test [arg]",     // 使用方法
        playerOnly = true         // プレイヤー限定（省略可、デフォルトfalse）
    )
    public void test(CommandSender sender, String[] args) {
        sender.sendMessage("テストコマンドが実行されました！");
    }
    
    // 多階層サブコマンド
    @SubCommand(path = "config.reload", description = "設定をリロード")
    public void configReload(CommandSender sender, String[] args) {
        sender.sendMessage("設定がリロードされました！");
    }
    
    @SubCommand(path = "config.save", description = "設定を保存")
    public void configSave(CommandSender sender, String[] args) {
        sender.sendMessage("設定が保存されました！");
    }
    
    @SubCommand(path = "user.create.admin", description = "管理者ユーザーを作成")
    public void createAdminUser(CommandSender sender, String[] args) {
        sender.sendMessage("管理者ユーザーが作成されました！");
    }
}
```

## 多階層サブコマンドシステム

### パス構造

サブコマンドは`.`で区切られたパス構造で定義できます：

- **単一レベル**: `test`, `reload`, `save`
- **2階層**: `config.reload`, `user.list`, `item.give`
- **3階層以上**: `config.advanced.debug`, `user.create.admin`

### 実行例

```bash
# 単一レベル
/mycommand test
/mycommand reload

# 多階層
/mycommand config reload
/mycommand config save
/mycommand user create admin
/mycommand config advanced debug
```

### 部分パスヘルプ

未定義の部分パスを入力すると、その下位のサブコマンドが自動表示されます：

```bash
# /mycommand config を実行
========== config のサブコマンド ==========
利用可能なサブコマンド:
  reload - 設定をリロード
  save - 設定を保存
  advanced - サブカテゴリ (1個のコマンド)
使用方法: /mycommand config <サブコマンド>
================================================
```

### タブ補完

多階層構造に対応したタブ補完が自動で機能します：

```bash
/mycommand <TAB>     # 第1レベルのサブコマンドを表示
/mycommand config <TAB>  # configの下位サブコマンドを表示
/mycommand user create <TAB>  # user createの下位サブコマンドを表示
```
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

## 実装例（TestCommand）

現在のTestCommandクラスでは以下のサブコマンドが定義されています：

```java
@Command(name = "test", description = "Test command for TempceLib")
public class TestCommand {

    @SubCommand(path = "test", description = "Executes the test command")
    public void test(CommandSender sender, String[] args) {
        sender.sendMessage("§aTest command executed with args: " + String.join(", ", args));
    }

    @SubCommand(path = "test.test2", description = "Executes the test2 sub-command")
    public void test2(CommandSender sender, String[] args) {
        sender.sendMessage("§aTest.test2 executed with args: " + String.join(", ", args));
    }
    
    @SubCommand(path = "config.reload", description = "Reload configuration")
    public void configReload(CommandSender sender, String[] args) {
        sender.sendMessage("§aConfiguration reloaded!");
    }
    
    @SubCommand(path = "config.advanced.debug", description = "Enable debug mode")
    public void configAdvancedDebug(CommandSender sender, String[] args) {
        sender.sendMessage("§aDebug mode enabled!");
    }
}
```

### 実行可能なコマンド一覧

- `/test test` - 基本テストコマンド
- `/test test test2` - 多階層テストコマンド（test.test2）
- `/test config reload` - 設定リロード
- `/test config advanced debug` - デバッグモード有効化
- `/test help` - 自動生成されるヘルプ

## システムアーキテクチャ

### 主要コンポーネント

1. **CommandManager**: コマンド登録・管理・タブ補完処理
2. **TempceCommandExecutor**: コマンド実行・パス解析・部分パスヘルプ
3. **SubCommandData**: サブコマンド情報保持・パス操作メソッド
4. **AutoHelpExecutor**: 自動ヘルプ生成・表示

### パス解析システム

サブコマンドは最長一致アルゴリズムで解析されます：

1. 入力された引数から最長のパスを構築
2. 登録されたサブコマンドと照合
3. 一致しない場合は部分パスヘルプを表示
4. エイリアスでの照合も実行

### 権限・タイムアウト

- **コマンドレベル**: メインコマンドの権限・タイムアウト
- **サブコマンドレベル**: 個別のサブコマンド権限・タイムアウト
- **継承**: サブコマンドはメインコマンドの権限を継承しない（独立）

## 権限

以下の権限が定義されています：

- `tempcelib.use` - 基本機能（デフォルト: true）

## 注意事項

1. サブコマンドメソッドのパラメータは必ず `(CommandSender, String[])` の形式にしてください
2. `playerOnly = true` を設定したサブコマンドはプレイヤーのみ実行可能です
3. クールダウンは送信者とコマンド/サブコマンドごとに個別に管理されます
4. 権限が設定されていない場合は誰でも実行可能です
5. **重要**: `@SubCommand`の`path`属性は完全なパスで登録されます（従来の`name`から変更）
6. 同一の第1レベル名を持つ異なるパス（例：`test`と`test.test2`）は共存可能です
7. 自動ヘルプコマンドは既存の`help`サブコマンドがない場合のみ追加されます

## トラブルシューティング

### よくある問題

1. **サブコマンドがhelpで上書きされる**
   - 修正済み：完全パス登録により解決

2. **多階層サブコマンドが認識されない**
   - `path`属性を正しく設定しているか確認
   - `.`区切りで正確にパスを記述

3. **タブ補完が動作しない**
   - `CommandManager`の`handleMultiLevelTabCompletion`が多階層に対応

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
