# TempceLib

**TempceLib**は、Minecraftプラグイン開発を効率化し、高品質なプラグインの作成を支援する包括的なJavaライブラリです。データ管理から高度なGUI作成まで、プラグイン開発に必要な機能を統合的に提供します。

## 📋 目次

- [機能概要](#-機能概要)
- [インストール](#-インストール)
- [クイックスタート](#-クイックスタート)
- [主要機能](#-主要機能)
  - [データファイル操作](#データファイル操作)
  - [プレイヤーデータ管理](#プレイヤーデータ管理)
  - [データベース統合](#データベース統合)
  - [コマンドシステム](#コマンドシステム)
  - [メッセージシステム](#メッセージシステム)
  - [GUI管理システム](#gui管理システム)
- [APIドキュメント](#-apiドキュメント)
- [サンプルコード](#-サンプルコード)
- [ライセンス](#-ライセンス)

## 🌟 機能概要

TempceLibは以下の主要機能を提供します：

### ✅ データファイル操作
- **YAML・JSON統合サポート**: 設定ファイルの読み書きを簡潔なAPIで実現
- **自動型変換**: 型安全なデータアクセスとバリデーション
- **階層構造サポート**: ネストされたデータ構造の直感的な操作

### 👥 プレイヤーデータ管理
- **自動データ収集**: ログイン時間、チャット数、アクティブ時間帯を自動追跡
- **多言語対応**: プレイヤーの使用言語自動検出・設定
- **拡張可能設計**: プラグイン独自のデータフィールドを簡単に追加
- **パフォーマンス最適化**: 非同期処理による軽量なデータアクセス

### 🗄️ データベース統合
- **マルチDB対応**: MySQL、SQLite、PostgreSQL対応
- **クエリ最適化**: 自動的なクエリ結合とパフォーマンス向上
- **軽量化機能**: データ重複排除と効率的なインデックス管理
- **移行サポート**: データベース間の簡単な移行ツール

### ⌨️ コマンドシステム
- **タブ補完自動生成**: `@Command`と`@SubCommand`の`name`から自動でタブ補完を生成
- **権限管理統合**: Spigot権限システムとの連携
- **サブコマンド対応**: 階層的なコマンド構造のサポート
- **エラーハンドリング**: 分かりやすいエラーメッセージ自動生成

### 💬 リッチメッセージシステム
- **インタラクティブテキスト**: クリック・ホバーイベント対応（Spigot API使用）
- **カスタマイズ可能**: 色、フォーマット効果
- **多言語サポート**: 設定ファイルベースの翻訳システム
- **コンポーネント対応**: TextComponentを活用したリッチテキスト

### 🖥️ GUI管理システム
- **統合コマンドGUI**: ADMIN限定でTempceLibに登録された全プラグインのコマンドを実行可能なGUI自動生成
- **簡略化GUI生成**: アイテム選択、数値選択、確認ダイアログなどの汎用GUIを簡単生成
- **権限管理統合**: 権限に応じたGUI表示制御
- **ページネーション**: 大量の項目を自動でページ分割表示
- **カスタマイズ対応**: テーマやレイアウトの変更可能

## 📦 インストール

### Maven
```xml
<dependency>
    <groupId>com.Tempce</groupId>
    <artifactId>tempcelib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.Tempce:tempcelib:1.0.0'
```

### 手動インストール
1. [リリースページ](https://github.com/Tempce/TempceLib/releases)から最新版をダウンロード
2. `plugins`フォルダにJARファイルを配置
3. サーバーを再起動

## 🚀 クイックスタート

```java
public class MyPlugin extends JavaPlugin {
    private TempceLib tempceLib;
    
    @Override
    public void onEnable() {
        // TempceLibの初期化
        tempceLib = TempceLib.getInstance(this);
        
        // プレイヤーデータ管理の有効化
        tempceLib.getPlayerDataManager().enable();
        
        // 設定ファイルの読み込み
        YamlConfig config = tempceLib.getConfigManager().loadYaml("config.yml");
        
        // コマンドの登録
        tempceLib.getCommandManager().registerCommand(new MyCommand());
        
        getLogger().info("MyPlugin has been enabled with TempceLib!");
    }
}
```

## 🔧 主要機能

### データファイル操作

#### YAML操作
```java
// 設定の読み書き
YamlConfig config = tempceLib.getConfigManager().loadYaml("config.yml");
config.set("server.port", 25565);
config.set("features.pvp", true);
config.save();

// 型安全なデータ取得
int port = config.getInt("server.port", 25565);
boolean pvpEnabled = config.getBoolean("features.pvp", false);
```

#### JSON操作
```java
// JSONデータの処理
JsonConfig data = tempceLib.getConfigManager().loadJson("data.json");
data.setObject("player.stats", new PlayerStats(player));
data.save();
```

### プレイヤーデータ管理

```java
// プレイヤーデータの取得
PlayerDataManager pdm = tempceLib.getPlayerDataManager();
PlayerData data = pdm.getPlayerData(player);

// 自動収集データの参照
long loginTime = data.getLastLoginTime();
int chatCount = data.getChatCount();
String preferredLanguage = data.getPreferredLanguage();
Map<Integer, Long> activeHours = data.getActiveHours();

// カスタムデータの追加
data.setCustomData("myPlugin.level", 50);
data.setCustomData("myPlugin.experience", 12500L);
```

### データベース統合

```java
// データベース接続の設定
DatabaseManager dbManager = tempceLib.getDatabaseManager();
dbManager.configure(DatabaseType.MYSQL, "localhost", 3306, "minecraft", "user", "password");

// クエリの実行
PlayerStats stats = dbManager.queryBuilder()
    .select("player_stats")
    .where("player_uuid", player.getUniqueId())
    .executeQuery(PlayerStats.class);

// バッチ処理による最適化
dbManager.batchUpdate(updates);
```

### コマンドシステム

```java
@Command(name = "mycommand", permission = "myplugin.use")
public class MyCommand extends TempceCommand {
    
    @SubCommand(name = "reload")
    @Permission("myplugin.admin")
    public void reload(CommandSender sender, String[] args) {
        // リロード処理
        sender.sendMessage("§aConfiguration reloaded!");
    }
    
    @SubCommand(name = "status")
    @Permission("myplugin.use")
    public void status(CommandSender sender, String[] args) {
        // ステータス表示
        sender.sendMessage("§ePlugin is running!");
    }
    
    // タブ補完は自動生成される:
    // /mycommand <TAB> → ["reload", "status"]
    // name属性から自動的にタブ補完候補が作成される
}
```

### メッセージシステム

```java
// リッチメッセージの作成（Spigot API使用）
TextComponent message = new TextComponent("§6Welcome to the server!");
TextComponent helpText = new TextComponent("\n§eClick here for help");
helpText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help"));
helpText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
    new ComponentBuilder("§7Get help and information").create()));

message.addExtra(helpText);
player.spigot().sendMessage(message);

// TempceLib簡易API
MessageBuilder builder = tempceLib.getMessageManager()
    .builder("§6Welcome to the server!")
    .newLine()
    .append("§eClick here for help")
    .click("/help")
    .hover("§7Get help and information");
    
player.spigot().sendMessage(builder.build());
```

### GUI管理システム

```java
// ADMIN用統合コマンドGUI（自動生成）
// TempceLibに登録された全プラグインのコマンドを自動収集・表示
if (player.hasPermission("tempcelib.admin")) {
    AdminCommandGUI adminGUI = tempceLib.getGUIManager()
        .createAdminCommandGUI();
    
    // 登録されたすべてのプラグインのコマンドが自動で表示される
    // 例：MyPlugin → [/myplugin reload, /myplugin status]
    //     EconomyPlugin → [/economy give, /economy take]
    adminGUI.open(player);
}

// 簡略化GUI生成機能

// 1. アイテム選択GUI
ItemSelectionGUI itemGUI = tempceLib.getGUIManager()
    .createItemSelection("§8Select Item", Arrays.asList(
        Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT
    ))
    .onSelect((player, material) -> {
        player.getInventory().addItem(new ItemStack(material));
        player.sendMessage("§aSelected: " + material.name());
    })
    .build();

itemGUI.open(player);

// 2. 数値選択GUI
NumberSelectionGUI numberGUI = tempceLib.getGUIManager()
    .createNumberSelection("§8Select Amount", 1, 64)
    .onSelect((player, number) -> {
        player.sendMessage("§aSelected amount: " + number);
    })
    .build();

numberGUI.open(player);

// 3. 確認ダイアログGUI
ConfirmationGUI confirmGUI = tempceLib.getGUIManager()
    .createConfirmation("§cDelete all data?", 
        "§7This action cannot be undone!")
    .onConfirm(player -> {
        // 確認時の処理
        deleteAllData();
        player.sendMessage("§cData deleted!");
    })
    .onCancel(player -> {
        player.sendMessage("§7Cancelled.");
    })
    .build();

confirmGUI.open(player);

// 4. プレイヤー選択GUI
PlayerSelectionGUI playerGUI = tempceLib.getGUIManager()
    .createPlayerSelection("§8Select Player")
    .excludeOffline(false) // オフラインプレイヤーも表示
    .onSelect((admin, selectedPlayer) -> {
        admin.sendMessage("§aSelected: " + selectedPlayer.getName());
    })
    .build();

playerGUI.open(player);
```

## 📚 APIドキュメント

詳細なAPIドキュメントは[こちら](https://tempce.github.io/TempceLib/docs/)で確認できます。

### 主要クラス
- `TempceLib` - メインのライブラリクラス
- `PlayerDataManager` - プレイヤーデータ管理
- `ConfigManager` - 設定ファイル管理
- `DatabaseManager` - データベース操作
- `CommandManager` - コマンド管理
- `MessageManager` - メッセージ管理
- `GUIManager` - GUI管理（統合コマンドGUI、簡略化GUI生成）

## 💡 サンプルコード

完全なサンプルプロジェクトは[examples](https://github.com/Tempce/TempceLib/tree/main/examples)フォルダを参照してください。

## 🤝 コントリビューション

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add amazing feature'`)
4. ブランチをプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを作成

## 📄 ライセンス

このプロジェクトは[MIT License](LICENSE)の下で公開されています。

## 📞 サポート

- **Issue**: [GitHub Issues](https://github.com/Tempce/TempceLib/issues)
- **Discord**: [TempceLib Support Server](https://discord.gg/tempcelib)
- **Email**: support@tempce.com

---

**TempceLib** - Minecraftプラグイン開発をもっと簡単に、もっと楽しく！