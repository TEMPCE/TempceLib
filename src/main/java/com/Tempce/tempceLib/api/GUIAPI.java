package com.Tempce.tempceLib.api;

import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.MaterialCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * GUI管理システムのメインAPI
 * 汎用GUI生成、コマンド自動GUI化、権限管理統合、ページネーション機能を提供
 */
public interface GUIAPI {
    
    /**
     * シンプルなアイテム選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param items 選択肢アイテム
     * @param onSelect アイテム選択時のコールバック
     */
    void createItemSelectionGUI(Player player, String title, List<ItemStack> items, Consumer<ItemStack> onSelect);
    
    /**
     * 数値選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param min 最小値
     * @param max 最大値
     * @param defaultValue デフォルト値
     * @param onSelect 数値選択時のコールバック
     */
    void createNumberSelectionGUI(Player player, String title, int min, int max, int defaultValue, Consumer<Integer> onSelect);
    
    /**
     * 確認ダイアログGUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param message 確認メッセージ
     * @param onConfirm 確認時のコールバック
     * @param onCancel キャンセル時のコールバック
     */
    void createConfirmationGUI(Player player, String title, String message, Runnable onConfirm, Runnable onCancel);
    
    /**
     * カスタムメニューGUIを作成
     * @param player 対象プレイヤー
     * @param menuData メニューデータ
     */
    void createCustomMenuGUI(Player player, GUIMenuData menuData);
    
    /**
     * オンラインプレイヤー選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param onSelect プレイヤー選択時のコールバック
     */
    void createPlayerSelectionGUI(Player player, String title, Consumer<Player> onSelect);
    
    /**
     * 権限フィルタ付きプレイヤー選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param permission 必要な権限（nullの場合はフィルタなし）
     * @param onSelect プレイヤー選択時のコールバック
     */
    void createPlayerSelectionGUI(Player player, String title, String permission, Consumer<Player> onSelect);
    
    /**
     * オフラインプレイヤーを含む全プレイヤー選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param includeOffline オフラインプレイヤーを含むかどうか
     * @param onSelect プレイヤー選択時のコールバック
     */
    void createAllPlayerSelectionGUI(Player player, String title, boolean includeOffline, Consumer<Player> onSelect);
    
    /**
     * プレイヤー名での選択コールバック（オフライン対応）
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param includeOffline オフラインプレイヤーを含むかどうか
     * @param onSelectName プレイヤー名選択時のコールバック
     */
    void createPlayerNameSelectionGUI(Player player, String title, boolean includeOffline, Consumer<String> onSelectName);
    
    /**
     * コマンド自動GUI化メニューを開く
     * @param player 対象プレイヤー
     */
    void openCommandAutoGUI(Player player);
    
    /**
     * 特定のコマンドのサブコマンドGUIを開く
     * @param player 対象プレイヤー
     * @param commandName メインコマンド名
     */
    void openSubCommandGUI(Player player, String commandName);
    
    /**
     * ページネーション付きGUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param items 全アイテムリスト
     * @param itemsPerPage 1ページあたりのアイテム数
     * @param onItemClick アイテムクリック時のコールバック
     */
    void createPaginatedGUI(Player player, String title, List<GUIItemData> items, int itemsPerPage, Consumer<GUIItemData> onItemClick);
    
    /**
     * マテリアル選択GUIを作成
     * @param player 対象プレイヤー
     * @param title GUIタイトル
     * @param category マテリアルカテゴリ
     * @param onSelect マテリアル選択時のコールバック
     */
    void createMaterialSelectionGUI(Player player, String title, MaterialCategory category, Consumer<org.bukkit.Material> onSelect);
    
    /**
     * プレイヤーが特定のGUIにアクセス可能かチェック
     * @param player 対象プレイヤー
     * @param permission 必要な権限
     * @return アクセス可能かどうか
     */
    boolean hasGUIPermission(Player player, String permission);
    
    /**
     * GUIを閉じる
     * @param player 対象プレイヤー
     */
    void closeGUI(Player player);
}
