package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.manager.util.GUIItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * ページネーション機能を管理するクラス
 */
public class PaginationManager {
    private final Map<UUID, Integer> currentPages = new ConcurrentHashMap<>();
    private final Map<UUID, List<GUIItemData>> paginatedItems = new ConcurrentHashMap<>();
    private final Map<UUID, Consumer<GUIItemData>> paginatedCallbacks = new ConcurrentHashMap<>();
    private boolean debugMode = false;
    
    /**
     * デバッグモードを設定
     * @param debug デバッグモード
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
    
    /**
     * デバッグログを出力
     * @param message メッセージ
     */
    private void debugLog(String message) {
        if (debugMode) {
            TempceLib.getInstance().getLogger().info("[PAGINATION DEBUG] " + message);
        }
    }
    
    /**
     * プレイヤーのページネーション情報を一時保存するためのクラス
     */
    public static class PaginationContext {
        public final Integer savedPage;
        public final List<GUIItemData> savedItems;
        public final Consumer<GUIItemData> savedCallback;
        
        public PaginationContext(Integer page, List<GUIItemData> items, Consumer<GUIItemData> callback) {
            this.savedPage = page;
            this.savedItems = items;
            this.savedCallback = callback;
        }
    }
    
    /**
     * プレイヤーのページネーション情報を保存
     * @param playerId プレイヤーID
     * @return 保存された情報
     */
    public PaginationContext savePaginationContext(UUID playerId) {
        Integer savedPage = currentPages.get(playerId);
        List<GUIItemData> savedItems = paginatedItems.get(playerId);
        Consumer<GUIItemData> savedCallback = paginatedCallbacks.get(playerId);
        return new PaginationContext(savedPage, savedItems, savedCallback);
    }
    
    /**
     * プレイヤーのページネーション情報を復元
     * @param playerId プレイヤーID
     * @param context 復元する情報
     */
    public void restorePaginationContext(UUID playerId, PaginationContext context) {
        if (context != null && context.savedPage != null && context.savedItems != null) {
            currentPages.put(playerId, context.savedPage);
            paginatedItems.put(playerId, context.savedItems);
            if (context.savedCallback != null) {
                paginatedCallbacks.put(playerId, context.savedCallback);
            }
        }
    }
    
    /**
     * ページネーション用のGUIを作成
     * @param player プレイヤー
     * @param title タイトル
     * @param items 全アイテムリスト
     * @param itemsPerPage ページあたりのアイテム数
     * @param onItemClick アイテムクリック時のコールバック
     * @param guiCreator GUIMenuDataを受け取ってGUIを作成する関数
     */
    public void createPaginatedGUI(Player player, String title, List<GUIItemData> items, 
                                   int itemsPerPage, Consumer<GUIItemData> onItemClick,
                                   Consumer<GUIMenuData> guiCreator) {
        UUID playerId = player.getUniqueId();
        
        // 既存のページネーションデータをクリア
        clearPaginationData(playerId);
        
        // 新しいデータを設定
        paginatedItems.put(playerId, new ArrayList<>(items));
        if (onItemClick != null) {
            paginatedCallbacks.put(playerId, onItemClick);
        }
        currentPages.put(playerId, 0);
        
        debugLog("ページネーションGUI初期化: アイテム数=" + items.size() + ", ページあたり=" + itemsPerPage);
        
        showPaginatedPage(player, title, 0, itemsPerPage, guiCreator);
    }
    
    /**
     * ページネーション付きGUIの特定ページを表示
     */
    public void showPaginatedPage(Player player, String title, int page, int itemsPerPage,
                                  Consumer<GUIMenuData> guiCreator) {
        UUID playerId = player.getUniqueId();
        List<GUIItemData> allItems = paginatedItems.get(playerId);
        if (allItems == null) {
            debugLog("ページネーション用のアイテムデータが見つかりません: " + playerId);
            return;
        }
        
        // ページ数の計算
        int totalPages = Math.max(1, (allItems.size() + itemsPerPage - 1) / itemsPerPage);
        
        // ページ番号の妥当性チェック
        int validPage = Math.max(0, Math.min(page, totalPages - 1));
        
        int startIndex = validPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());
        
        List<GUIItemData> pageItems = new ArrayList<>();
        
        // ページ内のアイテムを追加
        int slotCounter = 0;
        for (int i = startIndex; i < endIndex; i++) {
            GUIItemData originalItem = allItems.get(i);
            Consumer<GUIItemData> clickAction = paginatedCallbacks.getOrDefault(playerId, originalItem.getClickAction());
            
            pageItems.add(new GUIItemData(originalItem.getItemStack(), slotCounter, 
                    clickAction, originalItem.getPermission(), originalItem.isEnabled()));
            slotCounter++;
        }
        
        // 現在のページ番号を更新
        currentPages.put(playerId, validPage);
        
        // ナビゲーションボタンを追加
        addNavigationButtons(pageItems, player, title, validPage, totalPages, itemsPerPage, guiCreator);
        
        // ページ情報表示
        ItemStack pageInfo = GUIItemCreator.createItem(Material.BOOK, ChatColor.GREEN + "ページ情報",
                Arrays.asList(
                    ChatColor.GRAY + "現在のページ: " + ChatColor.WHITE + (validPage + 1) + "/" + totalPages,
                    ChatColor.GRAY + "総アイテム数: " + ChatColor.WHITE + allItems.size(),
                    ChatColor.GRAY + "表示中: " + ChatColor.WHITE + (endIndex - startIndex) + "個"
                ));
        pageItems.add(new GUIItemData(pageInfo, 49, null));
        
        String pageTitle = title + " (" + (validPage + 1) + "/" + totalPages + ")";
        GUIMenuData menuData = new GUIMenuData(pageTitle, 54, pageItems);
        
        debugLog("ページネーションGUI作成: ページ=" + (validPage + 1) + "/" + totalPages + 
                ", 表示アイテム数=" + (endIndex - startIndex) + ", 総アイテム数=" + pageItems.size());
        
        guiCreator.accept(menuData);
    }
    
    /**
     * ナビゲーションボタンを追加
     */
    private void addNavigationButtons(List<GUIItemData> pageItems, Player player, String title, 
                                      int currentPage, int totalPages, int itemsPerPage,
                                      Consumer<GUIMenuData> guiCreator) {
        if (currentPage > 0) {
            ItemStack prevItem = GUIItemCreator.createItem(Material.ARROW, ChatColor.YELLOW + "◀ 前のページ",
                    Arrays.asList(
                        ChatColor.GRAY + "現在: " + (currentPage + 1) + "/" + totalPages,
                        ChatColor.GRAY + "クリックして前のページへ"
                    ));
            pageItems.add(new GUIItemData(prevItem, 45, (guiItemData) -> {
                debugPageNavigation(player, "前のページクリック: " + (currentPage - 1));
                showPaginatedPage(player, title, currentPage - 1, itemsPerPage, guiCreator);
            }));
        }
        
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = GUIItemCreator.createItem(Material.ARROW, ChatColor.YELLOW + "次のページ ▶",
                    Arrays.asList(
                        ChatColor.GRAY + "現在: " + (currentPage + 1) + "/" + totalPages,
                        ChatColor.GRAY + "クリックして次のページへ"
                    ));
            pageItems.add(new GUIItemData(nextItem, 53, (guiItemData) -> {
                debugPageNavigation(player, "次のページクリック: " + (currentPage + 1));
                showPaginatedPage(player, title, currentPage + 1, itemsPerPage, guiCreator);
            }));
        }
    }
    
    /**
     * ページネーション関連データをクリア
     * @param playerId プレイヤーID
     */
    public void clearPaginationData(UUID playerId) {
        currentPages.remove(playerId);
        paginatedItems.remove(playerId);
        paginatedCallbacks.remove(playerId);
    }
    
    /**
     * ページネーションの詳細なデバッグ情報を出力
     * @param player プレイヤー
     * @param action 実行されたアクション
     */
    public void debugPageNavigation(Player player, String action) {
        if (!debugMode) return;
        
        UUID playerId = player.getUniqueId();
        Integer currentPage = currentPages.get(playerId);
        List<GUIItemData> items = paginatedItems.get(playerId);
        Consumer<GUIItemData> callback = paginatedCallbacks.get(playerId);
        
        debugLog("=== ページネーションデバッグ ===");
        debugLog("プレイヤー: " + player.getName());
        debugLog("アクション: " + action);
        debugLog("現在ページ: " + (currentPage != null ? (currentPage + 1) : "なし"));
        debugLog("アイテム数: " + (items != null ? items.size() : "なし"));
        debugLog("コールバック: " + (callback != null ? "設定済み" : "なし"));
        debugLog("===============================");
    }
    
    /**
     * 現在のページネーション状態を取得（デバッグ用）
     * @param player プレイヤー
     * @return ページネーション情報
     */
    public String getPageDebugInfo(Player player) {
        UUID playerId = player.getUniqueId();
        Integer currentPage = currentPages.get(playerId);
        List<GUIItemData> items = paginatedItems.get(playerId);
        
        if (currentPage == null || items == null) {
            return "ページネーションデータなし";
        }
        
        return String.format("現在ページ: %d, 総アイテム数: %d", currentPage + 1, items.size());
    }
    
    /**
     * ページネーション統計情報を取得
     * @return 統計情報
     */
    public String getStatistics() {
        return String.format("ページネーション中: %d", currentPages.size());
    }
}
