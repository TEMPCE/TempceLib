package com.Tempce.tempceLib.gui.manager;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.api.GUIAPI;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.manager.handlers.*;
import com.Tempce.tempceLib.gui.manager.util.GUIItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

/**
 * GUI管理システムのメインマネージャー（リファクタリング版）
 */
public class GUIManager implements GUIAPI {
    private static GUIManager instance;
    private boolean debugMode = false;
    
    // 各機能の管理クラス
    private final PaginationManager paginationManager;
    private final PlayerSelectionManager playerSelectionManager;
    private final CommandGUIManager commandGUIManager;
    private final GUIEventHandler eventHandler;
    
    /**
     * プライベートコンストラクタ（シングルトン）
     */
    private GUIManager() {
        // 各管理クラスを初期化
        this.paginationManager = new PaginationManager();
        this.playerSelectionManager = new PlayerSelectionManager();
        this.commandGUIManager = new CommandGUIManager();
        this.eventHandler = new GUIEventHandler(this::hasGUIPermission, () -> {
            // データクリーンアップコールバック
            UUID[] playerIds = new UUID[0];
            for (UUID playerId : playerIds) {
                paginationManager.clearPaginationData(playerId);
            }
        });
    }
    
    /**
     * GUIManagerのシングルトンインスタンスを取得
     * @return GUIManagerインスタンス
     */
    public static GUIManager getInstance() {
        if (instance == null) {
            instance = new GUIManager();
        }
        return instance;
    }
    
    /**
     * GUIManagerを初期化
     */
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(eventHandler, TempceLib.getInstance());
    }
    
    /**
     * デバッグモードを設定
     * @param debug デバッグモード
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        paginationManager.setDebugMode(debug);
        eventHandler.setDebugMode(debug);
    }
    
    /**
     * デバッグログを出力
     * @param message メッセージ
     */
    private void debugLog(String message) {
        if (debugMode) {
            TempceLib.getInstance().getLogger().info("[GUI DEBUG] " + message);
        }
    }
    
    @Override
    public void createItemSelectionGUI(Player player, String title, List<ItemStack> items, Consumer<ItemStack> onSelect) {
        List<GUIItemData> guiItems = new ArrayList<>();
        for (int i = 0; i < items.size() && i < 54; i++) {
            ItemStack item = items.get(i);
            guiItems.add(new GUIItemData(item, i, (guiItemData) -> onSelect.accept(item)));
        }
        
        int size = Math.min(54, ((items.size() - 1) / 9 + 1) * 9);
        GUIMenuData menuData = new GUIMenuData(title, size, guiItems);
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createNumberSelectionGUI(Player player, String title, int min, int max, int defaultValue, Consumer<Integer> onSelect) {
        UUID playerId = player.getUniqueId();
        
        // ページネーション関連データを一時保存
        PaginationManager.PaginationContext context = paginationManager.savePaginationContext(playerId);
        
        List<GUIItemData> guiItems = new ArrayList<>();
        int currentValue = Math.max(min, Math.min(max, defaultValue));
        
        // 数値選択ボタンを作成
        createNumberSelectionButtons(guiItems, player, title, min, max, currentValue, onSelect);
        
        // 確定・キャンセルボタンを作成
        createNumberSelectionControlButtons(guiItems, player, playerId, context, currentValue, onSelect);
        
        // タイトルに現在の値を含める
        String titleWithValue = title + " [" + currentValue + "]";
        GUIMenuData menuData = new GUIMenuData(titleWithValue, 18, guiItems);
        
        createCustomMenuGUI(player, menuData);
    }
    
    /**
     * 数値選択用のボタンを作成
     */
    private void createNumberSelectionButtons(List<GUIItemData> guiItems, Player player, String title, 
                                              int min, int max, int currentValue, Consumer<Integer> onSelect) {
        // 最小値ボタン（スロット0）
        if (currentValue != min) {
            ItemStack minItem = GUIItemCreator.createItem(Material.BEDROCK, ChatColor.DARK_RED + "最小値",
                    List.of(ChatColor.GRAY + "最小値 " + min + " に設定"));
            guiItems.add(new GUIItemData(minItem, 0, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, min, onSelect)));
        }
        
        // -10 ボタン（スロット1）
        if (currentValue - 10 >= min) {
            ItemStack decrease10Item = GUIItemCreator.createItem(Material.PURPLE_CONCRETE, ChatColor.DARK_PURPLE + "-10",
                    List.of(ChatColor.GRAY + "10減らす"));
            guiItems.add(new GUIItemData(decrease10Item, 1, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.max(min, currentValue - 10), onSelect)));
        }
        
        // その他のボタンも同様に作成...
        createNumberButtons(guiItems, player, title, min, max, currentValue, onSelect);
    }
    
    /**
     * 数値操作ボタンを作成
     */
    private void createNumberButtons(List<GUIItemData> guiItems, Player player, String title, 
                                     int min, int max, int currentValue, Consumer<Integer> onSelect) {
        // -5 ボタン（スロット2）
        if (currentValue - 5 >= min) {
            ItemStack decrease5Item = GUIItemCreator.createItem(Material.ORANGE_CONCRETE, ChatColor.GOLD + "-5",
                    List.of(ChatColor.GRAY + "5減らす"));
            guiItems.add(new GUIItemData(decrease5Item, 2, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.max(min, currentValue - 5), onSelect)));
        }
        
        // -1 ボタン（スロット3）
        if (currentValue > min) {
            ItemStack decreaseItem = GUIItemCreator.createItem(Material.RED_WOOL, ChatColor.RED + "-1",
                    List.of(ChatColor.GRAY + "1減らす"));
            guiItems.add(new GUIItemData(decreaseItem, 3, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue - 1, onSelect)));
        }
        
        // 数値表示アイテム（スロット4）
        ItemStack displayItem = GUIItemCreator.createItem(Material.PAPER, ChatColor.GREEN + "現在の値: " + currentValue, 
                Arrays.asList(
                    ChatColor.GRAY + "範囲: " + min + " - " + max,
                    ChatColor.GRAY + "左右のボタンで調整してください"
                ));
        guiItems.add(new GUIItemData(displayItem, 4, null));
        
        // +1 ボタン（スロット5）
        if (currentValue < max) {
            ItemStack increaseItem = GUIItemCreator.createItem(Material.LIME_WOOL, ChatColor.GREEN + "+1",
                    List.of(ChatColor.GRAY + "1増やす"));
            guiItems.add(new GUIItemData(increaseItem, 5, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue + 1, onSelect)));
        }
        
        // +5, +10, 最大値ボタンも同様に作成
        createIncrementButtons(guiItems, player, title, min, max, currentValue, onSelect);
    }
    
    /**
     * 増加ボタンを作成
     */
    private void createIncrementButtons(List<GUIItemData> guiItems, Player player, String title,
                                        int min, int max, int currentValue, Consumer<Integer> onSelect) {
        // +5 ボタン（スロット6）
        if (currentValue + 5 <= max) {
            ItemStack increase5Item = GUIItemCreator.createItem(Material.LIGHT_BLUE_CONCRETE, ChatColor.AQUA + "+5",
                    List.of(ChatColor.GRAY + "5増やす"));
            guiItems.add(new GUIItemData(increase5Item, 6, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.min(max, currentValue + 5), onSelect)));
        }
        
        // +10 ボタン（スロット7）
        if (currentValue + 10 <= max) {
            ItemStack increase10Item = GUIItemCreator.createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+10",
                    List.of(ChatColor.GRAY + "10増やす"));
            guiItems.add(new GUIItemData(increase10Item, 7, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.min(max, currentValue + 10), onSelect)));
        }
        
        // 最大値ボタン（スロット8）
        if (currentValue != max) {
            ItemStack maxItem = GUIItemCreator.createItem(Material.BEACON, ChatColor.YELLOW + "最大値",
                    List.of(ChatColor.GRAY + "最大値 " + max + " に設定"));
            guiItems.add(new GUIItemData(maxItem, 8, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, max, onSelect)));
        }
    }
    
    /**
     * 確定・キャンセルボタンを作成
     */
    private void createNumberSelectionControlButtons(List<GUIItemData> guiItems, Player player, UUID playerId,
                                                     PaginationManager.PaginationContext context, 
                                                     int currentValue, Consumer<Integer> onSelect) {
        // リセットボタン（スロット11）
        ItemStack resetItem = GUIItemCreator.createItem(Material.REDSTONE, ChatColor.RED + "リセット",
                List.of(ChatColor.GRAY + "値を最小値にリセット"));
        guiItems.add(new GUIItemData(resetItem, 11, (guiItemData) -> {
            // 必要に応じてリセット処理
        }));
        
        // 確定ボタン（スロット13）
        ItemStack confirmItem = GUIItemCreator.createItem(Material.EMERALD, ChatColor.GREEN + "確定",
                List.of(ChatColor.GRAY + "この値で決定する"));
        guiItems.add(new GUIItemData(confirmItem, 13, (guiItemData) -> {
            // ページネーションデータを復元
            paginationManager.restorePaginationContext(playerId, context);
            
            // インベントリを閉じてからコールバック実行
            player.closeInventory();
            onSelect.accept(currentValue);
        }));
        
        // キャンセルボタン（スロット14）
        ItemStack cancelItem = GUIItemCreator.createItem(Material.BARRIER, ChatColor.RED + "キャンセル",
                List.of(ChatColor.GRAY + "変更をキャンセル"));
        guiItems.add(new GUIItemData(cancelItem, 14, (guiItemData) -> {
            // ページネーションデータを復元
            paginationManager.restorePaginationContext(playerId, context);
            
            // インベントリを閉じる
            player.closeInventory();
        }));
    }
    
    @Override
    public void createConfirmationGUI(Player player, String title, String message, Runnable onConfirm, Runnable onCancel) {
        UUID playerId = player.getUniqueId();
        
        // ページネーション関連データを一時保存
        PaginationManager.PaginationContext context = paginationManager.savePaginationContext(playerId);
        
        List<GUIItemData> guiItems = new ArrayList<>();
        
        // メッセージ表示
        ItemStack messageItem = GUIItemCreator.createItem(Material.BOOK, ChatColor.YELLOW + "確認",
                List.of(ChatColor.WHITE + message));
        guiItems.add(new GUIItemData(messageItem, 4, null));
        
        // 確認ボタン
        ItemStack confirmItem = GUIItemCreator.createItem(Material.EMERALD, ChatColor.GREEN + "はい",
                List.of(ChatColor.GRAY + "クリックして確認"));
        guiItems.add(new GUIItemData(confirmItem, 2, (guiItemData) -> {
            // ページネーションデータを復元
            paginationManager.restorePaginationContext(playerId, context);
            
            // インベントリを閉じてからコールバック実行
            player.closeInventory();
            onConfirm.run();
        }));
        
        // キャンセルボタン
        ItemStack cancelItem = GUIItemCreator.createItem(Material.REDSTONE, ChatColor.RED + "いいえ",
                List.of(ChatColor.GRAY + "クリックしてキャンセル"));
        guiItems.add(new GUIItemData(cancelItem, 6, (guiItemData) -> {
            // ページネーションデータを復元
            paginationManager.restorePaginationContext(playerId, context);
            
            // インベントリを閉じてからコールバック実行
            player.closeInventory();
            onCancel.run();
        }));
        
        GUIMenuData menuData = new GUIMenuData(title, 9, guiItems);
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createCustomMenuGUI(Player player, GUIMenuData menuData) {
        UUID playerId = player.getUniqueId();
        
        // 既存のGUIデータを削除
        eventHandler.unregisterGUI(playerId);
        
        Inventory inventory = Bukkit.createInventory(null, menuData.getSize(), menuData.getTitle());
        
        // Fill itemがある場合は全スロットを埋める
        if (menuData.getFillItem() != null) {
            for (int i = 0; i < menuData.getSize(); i++) {
                inventory.setItem(i, menuData.getFillItem());
            }
        }
        
        // アイテムを配置
        int itemCount = 0;
        for (GUIItemData itemData : menuData.getItems()) {
            if (itemData.getSlot() >= 0 && itemData.getSlot() < menuData.getSize()) {
                // 権限チェック
                if (!itemData.getPermission().isEmpty() && !hasGUIPermission(player, itemData.getPermission())) {
                    continue;
                }
                inventory.setItem(itemData.getSlot(), itemData.getItemStack());
                itemCount++;
            }
        }
        
        // データを保存
        eventHandler.registerGUI(playerId, inventory, menuData);
        
        debugLog("カスタムGUI作成: プレイヤー=" + player.getName() + 
                ", タイトル=" + menuData.getTitle() + ", アイテム数=" + itemCount + "/" + menuData.getItems().size());
        
        // インベントリを開く
        player.openInventory(inventory);
    }
    
    @Override
    public void createPlayerSelectionGUI(Player player, String title, Consumer<Player> onSelect) {
        playerSelectionManager.createPlayerSelectionGUI(player, title, onSelect, 
            data -> paginationManager.createPaginatedGUI(player, data.title, data.items, data.itemsPerPage, 
                                                          data.onItemClick, menuData -> createCustomMenuGUI(player, menuData)));
    }
    
    @Override
    public void createPlayerSelectionGUI(Player player, String title, String permission, Consumer<Player> onSelect) {
        playerSelectionManager.createPlayerSelectionGUI(player, title, permission, onSelect,
            data -> paginationManager.createPaginatedGUI(player, data.title, data.items, data.itemsPerPage, 
                                                          data.onItemClick, menuData -> createCustomMenuGUI(player, menuData)));
    }
    
    @Override
    public void createAllPlayerSelectionGUI(Player player, String title, boolean includeOffline, Consumer<Player> onSelect) {
        playerSelectionManager.createAllPlayerSelectionGUI(player, title, includeOffline, onSelect,
            data -> paginationManager.createPaginatedGUI(player, data.title, data.items, data.itemsPerPage, 
                                                          data.onItemClick, menuData -> createCustomMenuGUI(player, menuData)));
    }
    
    @Override
    public void createPlayerNameSelectionGUI(Player player, String title, boolean includeOffline, Consumer<String> onSelectName) {
        playerSelectionManager.createPlayerNameSelectionGUI(player, title, includeOffline, onSelectName,
            data -> paginationManager.createPaginatedGUI(player, data.title, data.items, data.itemsPerPage, 
                                                          data.onItemClick, menuData -> createCustomMenuGUI(player, menuData)));
    }
    
    @Override
    public void openCommandAutoGUI(Player player) {
        commandGUIManager.openCommandAutoGUI(player, data -> {
            if (data.type == CommandGUIManager.CommandGUIType.COMMAND_LIST) {
                paginationManager.createPaginatedGUI(player, data.title, data.items, data.size, 
                                                      data.onItemClick, menuData -> createCustomMenuGUI(player, menuData));
            } else {
                GUIMenuData menuData = new GUIMenuData(data.title, data.size, data.items);
                createCustomMenuGUI(player, menuData);
            }
        });
    }
    
    @Override
    public void openSubCommandGUI(Player player, String commandName) {
        commandGUIManager.openSubCommandGUI(player, commandName, data -> {
            GUIMenuData menuData = new GUIMenuData(data.title, data.size, data.items);
            createCustomMenuGUI(player, menuData);
        });
    }
    
    @Override
    public void createPaginatedGUI(Player player, String title, List<GUIItemData> items, int itemsPerPage, Consumer<GUIItemData> onItemClick) {
        paginationManager.createPaginatedGUI(player, title, items, itemsPerPage, onItemClick, menuData -> createCustomMenuGUI(player, menuData));
    }
    
    @Override
    public boolean hasGUIPermission(Player player, String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return player.hasPermission(permission);
    }
    
    @Override
    public void closeGUI(Player player) {
        UUID playerId = player.getUniqueId();
        
        debugLog("GUI手動クローズ: プレイヤー=" + player.getName());
        
        // GUIが開いている場合のみクローズ処理を行う
        if (eventHandler.hasOpenGUI(playerId)) {
            // 全てのGUI関連データを削除
            eventHandler.unregisterGUI(playerId);
            paginationManager.clearPaginationData(playerId);
            
            // インベントリを閉じる
            player.closeInventory();
        }
    }
    
    /**
     * 現在のページネーション状態を取得（デバッグ用）
     * @param player プレイヤー
     * @return ページネーション情報
     */
    public String getPageDebugInfo(Player player) {
        return paginationManager.getPageDebugInfo(player);
    }
    
    /**
     * GUIManagerの統計情報を取得（デバッグ用）
     * @return 統計情報
     */
    public String getStatistics() {
        return String.format("%s, %s", eventHandler.getStatistics(), paginationManager.getStatistics());
    }
    
    /**
     * ページネーションの詳細なデバッグ情報を出力
     * @param player プレイヤー
     * @param action 実行されたアクション
     */
    public void debugPageNavigation(Player player, String action) {
        paginationManager.debugPageNavigation(player, action);
    }
    
    /**
     * GUIデバッグ機能を有効にするメソッド（テスト用）
     * @param player プレイヤー
     */
    public void enableDebugForPlayer(Player player) {
        setDebugMode(true);
        player.sendMessage(ChatColor.GREEN + "GUIデバッグモードが有効になりました。");
        player.sendMessage(ChatColor.GRAY + "現在の統計: " + getStatistics());
        
        String pageInfo = getPageDebugInfo(player);
        if (!pageInfo.equals("ページネーションデータなし")) {
            player.sendMessage(ChatColor.GRAY + "ページネーション情報: " + pageInfo);
        }
    }
    
    /**
     * GUIデバッグ機能を無効にするメソッド（テスト用）
     * @param player プレイヤー
     */
    public void disableDebugForPlayer(Player player) {
        setDebugMode(false);
        player.sendMessage(ChatColor.RED + "GUIデバッグモードが無効になりました。");
    }
}
