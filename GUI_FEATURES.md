# TempceLib GUI機能ドキュメント

TempceLibのGUI管理システムは、Spigotプラグインで柔軟なGUIメニューを簡単に作成できる機能を提供します。

## 主要機能

### 1. 汎用GUI生成API
- **アイテム選択GUI**: プレイヤーがアイテムを選択できるGUI
- **数値選択GUI**: 指定範囲内で数値を選択できるGUI  
- **確認ダイアログGUI**: はい/いいえの確認を行うGUI
- **プレイヤー選択GUI**: オンライン/オフラインプレイヤーから選択できるGUI（スキン表示対応）
- **カスタムメニューGUI**: 独自のレイアウトでメニューを作成

### 2. コマンド自動GUI化
- TempceLibに登録されたすべてのコマンドを自動検出
- コマンド一覧をGUI上に表示し、クリックで実行可能
- サブコマンド階層もGUIで操作可能
- 権限に応じたコマンドフィルタリング

### 3. 権限管理統合
- プレイヤーの権限に応じてGUI項目を自動制御
- 権限がない項目は非表示または無効化
- LuckPerms等の権限プラグインと連携

### 4. ページネーション機能
- 大量のアイテムを複数ページに自動分割
- 前/次ページのナビゲーションボタン
- 現在ページ・総ページ数の表示

### 5. プレイヤー選択機能の詳細
- **オンラインプレイヤー選択**: 現在サーバーにログインしているプレイヤーのみ表示
- **オフライン対応選択**: サーバーに参加したことがあるすべてのプレイヤーを表示
- **権限フィルタ**: 特定の権限を持つプレイヤーのみをフィルタリング
- **スキン表示**: プレイヤーヘッドにそれぞれのスキンを表示
- **ソート機能**: オンライン → オフライン、名前順で自動ソート
- **プレイヤー情報**: オンライン状態、権限情報などを表示

## 使用方法

### プラグイン開発者向けAPI

```java
import com.Tempce.tempceLib.api.CommandAPI;
import com.Tempce.tempceLib.api.GUIAPI;

// GUI APIを取得
GUIAPI guiAPI = CommandAPI.getGUIAPI();

// アイテム選択GUIを作成
List<ItemStack> items = Arrays.asList(/* アイテムリスト */);
guiAPI.createItemSelectionGUI(player, "アイテムを選択", items, (selectedItem) -> {
    // アイテム選択時の処理
});

// コマンド自動GUI化を開く
guiAPI.openCommandAutoGUI(player);

// プレイヤー選択GUIを作成
guiAPI.createPlayerSelectionGUI(player, "プレイヤーを選択", (selectedPlayer) -> {
    // プレイヤー選択時の処理
});

// 権限フィルタ付きプレイヤー選択GUIを作成
guiAPI.createPlayerSelectionGUI(player, "管理者を選択", "admin.permission", (selectedPlayer) -> {
    // 管理者選択時の処理
});

// オフラインプレイヤーを含む全プレイヤー選択GUIを作成
guiAPI.createAllPlayerSelectionGUI(player, "全プレイヤーから選択", true, (selectedPlayer) -> {
    // プレイヤー選択時の処理（オンラインプレイヤーのみ）
});

// プレイヤー名での選択GUI（オフライン対応）
guiAPI.createPlayerNameSelectionGUI(player, "プレイヤー名を選択", true, (selectedPlayerName) -> {
    // プレイヤー名選択時の処理（オンライン/オフライン問わず）
});

// カスタムメニューを作成
List<GUIItemData> items = new ArrayList<>();
items.add(new GUIItemData(itemStack, slot, clickAction));
GUIMenuData menuData = new GUIMenuData(title, size, items);
guiAPI.createCustomMenuGUI(player, menuData);
```

### テストコマンド

プラグインには以下のテストコマンドが含まれています：

- `/tempce-gui commands` - コマンド自動GUI化を開く
- `/tempce-gui item-selection` - アイテム選択GUIのテスト
- `/tempce-gui number-selection` - 数値選択GUIのテスト  
- `/tempce-gui confirmation` - 確認ダイアログGUIのテスト
- `/tempce-gui player-selection` - プレイヤー選択GUIのテスト
- `/tempce-gui player-selection-permission` - 権限フィルタ付きプレイヤー選択GUIのテスト
- `/tempce-gui all-player-selection [offline]` - 全プレイヤー選択GUIのテスト（offlineオプション）
- `/tempce-gui player-name-selection [offline]` - プレイヤー名選択GUIのテスト（オフライン対応）
- `/tempce-gui custom` - カスタムメニューGUIのテスト
- `/tempce-gui paginated` - ページネーションGUIのテスト

### 権限
- `tempcelib.gui` - GUI機能の基本権限

## アーキテクチャ

### パッケージ構成
```
com.Tempce.tempceLib.gui/
├── api/
│   └── GUIAPI.java                 # GUI APIインターフェース
├── data/
│   ├── GUIItemData.java           # GUIアイテムデータ
│   └── GUIMenuData.java           # GUIメニューデータ
├── manager/
│   └── GUIManager.java            # GUI管理メインクラス
└── examples/
    └── TempceGUICommand.java      # テスト用コマンド
```

### 主要クラス

#### GUIAPI
GUI機能の公開インターフェース。他のプラグインはこのAPIを通じてGUI機能を利用します。

#### GUIManager
GUI機能の実装クラス。Bukkit/Spigot APIを使用してインベントリGUIを管理します。

#### GUIItemData / GUIMenuData
GUIの構成要素を表すデータクラス。権限チェック、クリックアクション等の情報を保持します。

## 今後の機能拡張予定

- チェストGUI以外のGUIタイプサポート（看板入力等）
- GUIテンプレートシステム
- アニメーション効果
- 多言語対応
- 設定ファイルによるGUIカスタマイズ

## ライセンス

このプロジェクトは[ライセンス名]の下で公開されています。
