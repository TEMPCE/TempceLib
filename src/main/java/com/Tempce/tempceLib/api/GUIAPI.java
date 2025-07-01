package com.Tempce.tempceLib.api;

import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.data.GUIItemData;
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
