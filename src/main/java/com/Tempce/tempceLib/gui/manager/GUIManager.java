package com.Tempce.tempceLib.gui.manager;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.api.GUIAPI;
import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.data.SubCommandData;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * GUI管理システムのメインマネージャー
 */
public class GUIManager implements GUIAPI, Listener {
    private static GUIManager instance;
    private final Map<UUID, Inventory> openGUIs = new ConcurrentHashMap<>();
    private final Map<UUID, GUIMenuData> guiData = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentPages = new ConcurrentHashMap<>();
    private final Map<UUID, List<GUIItemData>> paginatedItems = new ConcurrentHashMap<>();
    private final Map<UUID, Consumer<GUIItemData>> paginatedCallbacks = new ConcurrentHashMap<>();
    private boolean debugMode = false; // デバッグモード制御
    
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
        Bukkit.getPluginManager().registerEvents(this, TempceLib.getInstance());
    }
    
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
            TempceLib.getInstance().getLogger().info("[GUI DEBUG] " + message);
        }
    }
    
    /**
     * プレイヤーのページネーション情報を一時保存するためのクラス
     */
    private static class PaginationContext {
        Integer savedPage;
        List<GUIItemData> savedItems;
        Consumer<GUIItemData> savedCallback;
        
        PaginationContext(Integer page, List<GUIItemData> items, Consumer<GUIItemData> callback) {
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
    private PaginationContext savePaginationContext(UUID playerId) {
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
    private void restorePaginationContext(UUID playerId, PaginationContext context) {
        if (context != null && context.savedPage != null && context.savedItems != null) {
            currentPages.put(playerId, context.savedPage);
            paginatedItems.put(playerId, context.savedItems);
            if (context.savedCallback != null) {
                paginatedCallbacks.put(playerId, context.savedCallback);
            }
        }
    }
    
    /**
     * GUI関連データを削除（ページネーションデータは保持）
     * @param playerId プレイヤーID
     */
    private void cleanupGUIData(UUID playerId) {
        openGUIs.remove(playerId);
        guiData.remove(playerId);
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
        
        // ページネーション関連データを一時保存（数字選択GUIはページネーションをクリアすべきではない）
        PaginationContext context = savePaginationContext(playerId);
        
        List<GUIItemData> guiItems = new ArrayList<>();
        int currentValue = Math.max(min, Math.min(max, defaultValue));
        
        // 1行目のボタン配置: [最小] [-10] [-5] [-1] [INFO] [+1] [+5] [+10] [最大]
        
        // 最小値ボタン（スロット0）
        if (currentValue != min) {
            ItemStack minItem = createItem(Material.BEDROCK, ChatColor.DARK_RED + "最小値",
                    List.of(ChatColor.GRAY + "最小値 " + min + " に設定"));
            guiItems.add(new GUIItemData(minItem, 0, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, min, onSelect)));
        }
        
        // -10 ボタン（スロット1）
        if (currentValue - 10 >= min) {
            ItemStack decrease10Item = createItem(Material.PURPLE_CONCRETE, ChatColor.DARK_PURPLE + "-10",
                    List.of(ChatColor.GRAY + "10減らす"));
            guiItems.add(new GUIItemData(decrease10Item, 1, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.max(min, currentValue - 10), onSelect)));
        }
        
        // -5 ボタン（スロット2）
        if (currentValue - 5 >= min) {
            ItemStack decrease5Item = createItem(Material.ORANGE_CONCRETE, ChatColor.GOLD + "-5",
                    List.of(ChatColor.GRAY + "5減らす"));
            guiItems.add(new GUIItemData(decrease5Item, 2, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.max(min, currentValue - 5), onSelect)));
        }
        
        // -1 ボタン（スロット3）
        if (currentValue > min) {
            ItemStack decreaseItem = createItem(Material.RED_WOOL, ChatColor.RED + "-1",
                    List.of(ChatColor.GRAY + "1減らす"));
            guiItems.add(new GUIItemData(decreaseItem, 3, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue - 1, onSelect)));
        }
        
        // 数値表示アイテム（スロット4）
        ItemStack displayItem = createItem(Material.PAPER, ChatColor.GREEN + "現在の値: " + currentValue, 
                Arrays.asList(
                    ChatColor.GRAY + "範囲: " + min + " - " + max,
                    ChatColor.GRAY + "左右のボタンで調整してください"
                ));
        guiItems.add(new GUIItemData(displayItem, 4, null));
        
        // +1 ボタン（スロット5）
        if (currentValue < max) {
            ItemStack increaseItem = createItem(Material.LIME_WOOL, ChatColor.GREEN + "+1",
                    List.of(ChatColor.GRAY + "1増やす"));
            guiItems.add(new GUIItemData(increaseItem, 5, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue + 1, onSelect)));
        }
        
        // +5 ボタン（スロット6）
        if (currentValue + 5 <= max) {
            ItemStack increase5Item = createItem(Material.LIGHT_BLUE_CONCRETE, ChatColor.AQUA + "+5",
                    List.of(ChatColor.GRAY + "5増やす"));
            guiItems.add(new GUIItemData(increase5Item, 6, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.min(max, currentValue + 5), onSelect)));
        }
        
        // +10 ボタン（スロット7）
        if (currentValue + 10 <= max) {
            ItemStack increase10Item = createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+10",
                    List.of(ChatColor.GRAY + "10増やす"));
            guiItems.add(new GUIItemData(increase10Item, 7, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, Math.min(max, currentValue + 10), onSelect)));
        }
        
        // 最大値ボタン（スロット8）
        if (currentValue != max) {
            ItemStack maxItem = createItem(Material.BEACON, ChatColor.YELLOW + "最大値",
                    List.of(ChatColor.GRAY + "最大値 " + max + " に設定"));
            guiItems.add(new GUIItemData(maxItem, 8, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, max, onSelect)));
        }
        
        // 2行目のボタン配置: [ ] [ ] [リセット] [ ] [ ] [確定] [キャンセル] [ ] [ ]
        
        // リセットボタン（スロット11 = 2行目の3個目）
        ItemStack resetItem = createItem(Material.REDSTONE, ChatColor.RED + "リセット",
                List.of(ChatColor.GRAY + "値を最小値 " + min + " にリセット"));
        guiItems.add(new GUIItemData(resetItem, 11, (guiItemData) -> 
                createNumberSelectionGUI(player, title, min, max, min, onSelect)));
        
        // 確定ボタン（スロット13 = 2行目の真ん中）
        ItemStack confirmItem = createItem(Material.EMERALD, ChatColor.GREEN + "確定",
                List.of(ChatColor.GRAY + "この値で決定する"));
        guiItems.add(new GUIItemData(confirmItem, 13, (guiItemData) -> {
            // ページネーションデータを復元
            restorePaginationContext(playerId, context);
            
            // GUI関連データを削除（ページネーションデータは保持）
            cleanupGUIData(playerId);
            
            // インベントリを閉じてからコールバック実行
            player.closeInventory();
            onSelect.accept(currentValue);
        }));
        
        // キャンセルボタン（スロット14 = 2行目の6つ目）
        ItemStack cancelItem = createItem(Material.BARRIER, ChatColor.RED + "キャンセル",
                List.of(ChatColor.GRAY + "変更をキャンセル"));
        guiItems.add(new GUIItemData(cancelItem, 14, (guiItemData) -> {
            // ページネーションデータを復元
            restorePaginationContext(playerId, context);
            
            // GUI関連データを削除（ページネーションデータは保持）
            cleanupGUIData(playerId);
            
            // インベントリを閉じる
            player.closeInventory();
        }));
        
        // タイトルに現在の値を含める
        String titleWithValue = title + " [" + currentValue + "]";
        GUIMenuData menuData = new GUIMenuData(titleWithValue, 18, guiItems);
        
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createConfirmationGUI(Player player, String title, String message, Runnable onConfirm, Runnable onCancel) {
        UUID playerId = player.getUniqueId();
        
        // ページネーション関連データを一時保存
        PaginationContext context = savePaginationContext(playerId);
        
        List<GUIItemData> guiItems = new ArrayList<>();
        
        // メッセージ表示
        ItemStack messageItem = createItem(Material.BOOK, ChatColor.YELLOW + "確認",
                List.of(ChatColor.WHITE + message));
        guiItems.add(new GUIItemData(messageItem, 4, null));
        
        // 確認ボタン
        ItemStack confirmItem = createItem(Material.EMERALD, ChatColor.GREEN + "はい",
                List.of(ChatColor.GRAY + "クリックして確認"));
        guiItems.add(new GUIItemData(confirmItem, 2, (guiItemData) -> {
            // ページネーションデータを復元
            restorePaginationContext(playerId, context);
            
            // GUI関連データを削除（ページネーションデータは保持）
            cleanupGUIData(playerId);
            
            // インベントリを閉じてからコールバック実行
            player.closeInventory();
            onConfirm.run();
        }));
        
        // キャンセルボタン
        ItemStack cancelItem = createItem(Material.REDSTONE, ChatColor.RED + "いいえ",
                List.of(ChatColor.GRAY + "クリックしてキャンセル"));
        guiItems.add(new GUIItemData(cancelItem, 6, (guiItemData) -> {
            // ページネーションデータを復元
            restorePaginationContext(playerId, context);
            
            // GUI関連データを削除（ページネーションデータは保持）
            cleanupGUIData(playerId);
            
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
        
        // ページネーション関連データを一時保存
        PaginationContext context = savePaginationContext(playerId);
        
        // 既存のGUIデータを削除（インベントリは閉じない）
        if (openGUIs.containsKey(playerId)) {
            openGUIs.remove(playerId);
            guiData.remove(playerId);
        }
        
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
        openGUIs.put(playerId, inventory);
        guiData.put(playerId, menuData);
        
        // ページネーションデータを復元（ページネーション中のGUIの場合）
        restorePaginationContext(playerId, context);
        
        debugLog("カスタムGUI作成: プレイヤー=" + player.getName() + 
                ", タイトル=" + menuData.getTitle() + ", アイテム数=" + itemCount + "/" + menuData.getItems().size());
        
        // インベントリを開く（既存のGUIがある場合は直接置き換える）
        player.openInventory(inventory);
    }
    
    @Override
    public void createPlayerSelectionGUI(Player player, String title, Consumer<Player> onSelect) {
        createPlayerSelectionGUI(player, title, null, onSelect);
    }
    
    @Override
    public void createPlayerSelectionGUI(Player player, String title, String permission, Consumer<Player> onSelect) {
        List<Player> availablePlayers = new ArrayList<>();
        
        // オンラインプレイヤーを取得
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // 権限チェック（nullの場合はフィルタなし）
            if (permission == null || onlinePlayer.hasPermission(permission)) {
                availablePlayers.add(onlinePlayer);
            }
        }
        
        if (availablePlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "選択可能なプレイヤーがいません。");
            return;
        }
        
        // プレイヤーをGUIアイテムに変換
        List<GUIItemData> guiItems = new ArrayList<>();
        for (Player targetPlayer : availablePlayers) {
            ItemStack playerHead = createPlayerHeadWithSkin(targetPlayer.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "プレイヤー: " + ChatColor.WHITE + targetPlayer.getName());
            lore.add(ChatColor.GRAY + "オンライン: " + ChatColor.GREEN + "はい");
            if (permission != null) {
                lore.add(ChatColor.GRAY + "権限: " + ChatColor.WHITE + permission);
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "クリックして選択");
            
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + targetPlayer.getName());
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            
            guiItems.add(new GUIItemData(playerHead, -1, (guiItemData) -> {
                player.closeInventory();
                onSelect.accept(targetPlayer);
            }));
        }
        
        // ページネーション付きGUIとして表示
        createPaginatedGUI(player, title, guiItems, 45, null);
    }
    
    @Override
    public void createAllPlayerSelectionGUI(Player player, String title, boolean includeOffline, Consumer<Player> onSelect) {
        // オンラインプレイヤーのリストを作成
        List<PlayerInfo> availablePlayers = new ArrayList<>();
        
        // オンラインプレイヤーを追加
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            availablePlayers.add(new PlayerInfo(onlinePlayer.getName(), true, onlinePlayer));
        }
        
        if (includeOffline) {
            // オフラインプレイヤーも含める場合の処理
            player.sendMessage(ChatColor.YELLOW + "オフラインプレイヤーを検索中...");
            
            // サーバーに参加したことがあるプレイヤーを取得
            for (org.bukkit.OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                // オンラインプレイヤーと重複しないようにチェック
                boolean isOnline = false;
                for (Player onlinePlayerCheck : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayerCheck.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                        isOnline = true;
                        break;
                    }
                }
                
                // オフラインプレイヤーのみを追加
                if (!isOnline && offlinePlayer.getName() != null && offlinePlayer.hasPlayedBefore()) {
                    availablePlayers.add(new PlayerInfo(offlinePlayer.getName(), false, null));
                }
            }
        }
        
        if (availablePlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "選択可能なプレイヤーがいません。");
            return;
        }
        
        // プレイヤー情報をソート（オンライン → オフライン、名前順）
        availablePlayers.sort((p1, p2) -> {
            if (p1.online() && !p2.online()) return -1;
            if (!p1.online() && p2.online()) return 1;
            return p1.name().compareToIgnoreCase(p2.name());
        });
        
        // プレイヤーをGUIアイテムに変換
        List<GUIItemData> guiItems = new ArrayList<>();
        for (PlayerInfo playerInfo : availablePlayers) {
            ItemStack playerHead = createPlayerHeadWithSkin(playerInfo.name());

            List<String> lore = getStrings(playerInfo);

            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + playerInfo.name());
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            
            guiItems.add(new GUIItemData(playerHead, -1, (guiItemData) -> {
                player.closeInventory();
                if (playerInfo.online() && playerInfo.onlinePlayer() != null) {
                    onSelect.accept(playerInfo.onlinePlayer());
                } else {
                    // オフラインプレイヤーの場合、プレイヤー名のメッセージを送信
                    player.sendMessage(ChatColor.YELLOW + "選択されたプレイヤー: " + playerInfo.name() + " (オフライン)");
                }
            }));
        }
        
        // ページネーション付きGUIとして表示
        createPaginatedGUI(player, title + " (" + availablePlayers.size() + "人)", guiItems, 45, null);
    }

    private static List<String> getStrings(PlayerInfo playerInfo) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "プレイヤー: " + ChatColor.WHITE + playerInfo.name());
        lore.add(ChatColor.GRAY + "オンライン: " +
                (playerInfo.online() ? ChatColor.GREEN + "はい" : ChatColor.RED + "いいえ"));

        if (!playerInfo.online()) {
            lore.add(ChatColor.GRAY + "最終ログイン: " + ChatColor.WHITE + "不明");
        }

        lore.add("");
        lore.add(ChatColor.YELLOW + "クリックして選択");
        return lore;
    }

    @Override
    public void createPlayerNameSelectionGUI(Player player, String title, boolean includeOffline, Consumer<String> onSelectName) {
        // オンラインプレイヤーのリストを作成
        List<PlayerInfo> availablePlayers = new ArrayList<>();
        
        // オンラインプレイヤーを追加
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            availablePlayers.add(new PlayerInfo(onlinePlayer.getName(), true, onlinePlayer));
        }
        
        if (includeOffline) {
            // オフラインプレイヤーも含める場合の処理
            player.sendMessage(ChatColor.YELLOW + "オフラインプレイヤーを検索中...");
            
            // サーバーに参加したことがあるプレイヤーを取得
            for (org.bukkit.OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                // オンラインプレイヤーと重複しないようにチェック
                boolean isOnline = false;
                for (Player onlinePlayerCheck : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayerCheck.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                        isOnline = true;
                        break;
                    }
                }
                
                // オフラインプレイヤーのみを追加
                if (!isOnline && offlinePlayer.getName() != null && offlinePlayer.hasPlayedBefore()) {
                    availablePlayers.add(new PlayerInfo(offlinePlayer.getName(), false, null));
                }
            }
        }
        
        if (availablePlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "選択可能なプレイヤーがいません。");
            return;
        }
        
        // プレイヤー情報をソート（オンライン → オフライン、名前順）
        availablePlayers.sort((p1, p2) -> {
            if (p1.online() && !p2.online()) return -1;
            if (!p1.online() && p2.online()) return 1;
            return p1.name().compareToIgnoreCase(p2.name());
        });
        
        // プレイヤーをGUIアイテムに変換
        List<GUIItemData> guiItems = new ArrayList<>();
        for (PlayerInfo playerInfo : availablePlayers) {
            ItemStack playerHead = createPlayerHeadWithSkin(playerInfo.name());

            List<String> lore = getStrings(playerInfo);

            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + playerInfo.name());
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            
            guiItems.add(new GUIItemData(playerHead, -1, (guiItemData) -> {
                player.closeInventory();
                onSelectName.accept(playerInfo.name());
            }));
        }
        
        // ページネーション付きGUIとして表示
        createPaginatedGUI(player, title + " (" + availablePlayers.size() + "人)", guiItems, 45, null);
    }
    
    /**
     * プレイヤーの頭アイテムを作成
     * @param player 対象プレイヤー
     * @return プレイヤーの頭のItemStack
     */
    private ItemStack createPlayerHead(Player player) {
        return createPlayerHeadWithSkin(player.getName());
    }
    
    /**
     * プレイヤーの頭アイテムをスキン付きで作成
     * @param playerName プレイヤー名
     * @return プレイヤーの頭のItemStack
     */
    private ItemStack createPlayerHeadWithSkin(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
          skullMeta.setDisplayName(ChatColor.AQUA + playerName);
            
            // プレイヤーのスキンを設定
            try {
                // オンラインプレイヤーの場合
                Player onlinePlayer = Bukkit.getPlayer(playerName);
                if (onlinePlayer != null) {
                    skullMeta.setOwningPlayer(onlinePlayer);
                } else {
                    // オフラインプレイヤーの場合
                    org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    if (offlinePlayer.hasPlayedBefore()) {
                        skullMeta.setOwningPlayer(offlinePlayer);
                    }
                }
            } catch (Exception e) {
                // スキン設定に失敗した場合はデフォルトのスカルを使用
                debugLog("プレイヤーヘッドのスキン設定に失敗: " + playerName + " - " + e.getMessage());
            }
            
            skull.setItemMeta(skullMeta);
        }
        return skull;
    }

    /**
     * プレイヤー情報を保持するプライベートクラス
     */
        private record PlayerInfo(String name, boolean online, Player onlinePlayer) {
    }

    @Override
    public void openCommandAutoGUI(Player player) {
        Map<String, CommandData> commands = TempceLib.getInstance().getCommandManager().getCommands();
        List<GUIItemData> guiItems = new ArrayList<>();
        
        int slot = 0;
        for (Map.Entry<String, CommandData> entry : commands.entrySet()) {
            CommandData commandData = entry.getValue();
            
            // 権限チェック
            if (!commandData.getPermission().isEmpty() && !hasGUIPermission(player, commandData.getPermission())) {
                continue;
            }
            
            ItemStack commandItem = createItem(Material.COMMAND_BLOCK, 
                    ChatColor.GOLD + "/" + commandData.getName(),
                    Arrays.asList(
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + commandData.getDescription(),
                            ChatColor.GRAY + "権限: " + ChatColor.WHITE + 
                                    (commandData.getPermission().isEmpty() ? "なし" : commandData.getPermission()),
                            "",
                            ChatColor.YELLOW + "クリックしてサブコマンドを表示"
                    ));
            
            guiItems.add(new GUIItemData(commandItem, slot++, (guiItemData) -> 
                    openSubCommandGUI(player, commandData.getName())));
            
            if (slot >= 54) break; // インベントリサイズ制限
        }
        
        if (guiItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "利用可能なコマンドがありません。");
            return;
        }
        
        createPaginatedGUI(player, ChatColor.DARK_GREEN + "コマンド一覧", guiItems, 45, null);
    }
    
    @Override
    public void openSubCommandGUI(Player player, String commandName) {
        CommandData commandData = TempceLib.getInstance().getCommandManager().getCommands().get(commandName);
        if (commandData == null) {
            player.sendMessage(ChatColor.RED + "コマンドが見つかりません: " + commandName);
            return;
        }
        
        List<GUIItemData> guiItems = new ArrayList<>();
        
        // メインコマンド実行ボタン
        ItemStack mainCommandItem = createItem(Material.EMERALD, 
                ChatColor.GREEN + "/" + commandName,
                Arrays.asList(
                        ChatColor.GRAY + "説明: " + ChatColor.WHITE + commandData.getDescription(),
                        "",
                        ChatColor.YELLOW + "クリックして実行"
                ));
        guiItems.add(new GUIItemData(mainCommandItem, 0, (guiItemData) -> {
            closeGUI(player);
            player.performCommand(commandName);
        }));
        
        // サブコマンドボタン
        int slot = 9; // 2行目から開始
        for (SubCommandData subCommandData : commandData.getSubCommands().values()) {
            // 権限チェック
            if (!subCommandData.getPermission().isEmpty() && !hasGUIPermission(player, subCommandData.getPermission())) {
                continue;
            }
            
            ItemStack subCommandItem = createItem(Material.PAPER, 
                    ChatColor.AQUA + "/" + commandName + " " + String.join(" ", subCommandData.getPathLevels()),
                    Arrays.asList(
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + subCommandData.getDescription(),
                            ChatColor.GRAY + "権限: " + ChatColor.WHITE + 
                                    (subCommandData.getPermission().isEmpty() ? "なし" : subCommandData.getPermission()),
                            "",
                            ChatColor.YELLOW + "クリックして実行"
                    ));
            
            final String fullCommand = commandName + " " + String.join(" ", subCommandData.getPathLevels());
            guiItems.add(new GUIItemData(subCommandItem, slot++, (guiItemData) -> {
                closeGUI(player);
                player.performCommand(fullCommand);
            }));
            
            if (slot >= 54) break;
        }
        
        // 戻るボタン
        ItemStack backItem = createItem(Material.ARROW, ChatColor.YELLOW + "戻る",
                List.of(ChatColor.GRAY + "コマンド一覧に戻る"));
        guiItems.add(new GUIItemData(backItem, 53, (guiItemData) -> {
            // コマンド一覧に戻る際はページネーションGUIを再表示
            openCommandAutoGUI(player);
        }));
        
        int size = Math.min(54, ((slot - 1) / 9 + 1) * 9);
        GUIMenuData menuData = new GUIMenuData(ChatColor.DARK_BLUE + "コマンド: " + commandName, size, guiItems);
        
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createPaginatedGUI(Player player, String title, List<GUIItemData> items, int itemsPerPage, Consumer<GUIItemData> onItemClick) {
        UUID playerId = player.getUniqueId();
        
        // 既存のページネーションデータをクリア
        currentPages.remove(playerId);
        paginatedItems.remove(playerId);
        paginatedCallbacks.remove(playerId);
        
        // 新しいデータを設定
        paginatedItems.put(playerId, new ArrayList<>(items));
        if (onItemClick != null) {
            paginatedCallbacks.put(playerId, onItemClick);
        }
        currentPages.put(playerId, 0);
        
        debugLog("ページネーションGUI初期化: アイテム数=" + items.size() + ", ページあたり=" + itemsPerPage);
        
        showPaginatedPage(player, title, 0, itemsPerPage);
    }
    
    /**
     * ページネーション付きGUIの特定ページを表示
     */
    private void showPaginatedPage(Player player, String title, int page, int itemsPerPage) {
        UUID playerId = player.getUniqueId();
        List<GUIItemData> allItems = paginatedItems.get(playerId);
        if (allItems == null) {
            debugLog("ページネーション用のアイテムデータが見つかりません: " + playerId);
            return;
        }
        
        // ページ数の計算を修正
        int totalPages = Math.max(1, (allItems.size() + itemsPerPage - 1) / itemsPerPage);
        
        // ページ番号の妥当性チェック
        int validPage = page;
        if (validPage < 0) {
            validPage = 0;
        } else if (validPage >= totalPages) {
            validPage = totalPages - 1;
        }
        
        int startIndex = validPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());
        
        List<GUIItemData> pageItems = new ArrayList<>();
        
        // ページ内のアイテムを追加（スロット0から開始し、9の倍数で改行）
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
        
        // ラムダ式で使用するためのfinal変数
        final int currentPage = validPage;
        
        // ナビゲーションボタンを追加（最下段に配置）
        if (currentPage > 0) {
            ItemStack prevItem = createItem(Material.ARROW, ChatColor.YELLOW + "◀ 前のページ",
                    Arrays.asList(
                        ChatColor.GRAY + "現在: " + (currentPage + 1) + "/" + totalPages,
                        ChatColor.GRAY + "クリックして前のページへ"
                    ));
            pageItems.add(new GUIItemData(prevItem, 45, (guiItemData) -> {
                debugPageNavigation(player, "前のページクリック: " + (currentPage - 1));
                debugLog("前のページクリック: " + (currentPage - 1));
                showPaginatedPage(player, title, currentPage - 1, itemsPerPage);
            }));
        }
        
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = createItem(Material.ARROW, ChatColor.YELLOW + "次のページ ▶",
                    Arrays.asList(
                        ChatColor.GRAY + "現在: " + (currentPage + 1) + "/" + totalPages,
                        ChatColor.GRAY + "クリックして次のページへ"
                    ));
            pageItems.add(new GUIItemData(nextItem, 53, (guiItemData) -> {
                debugPageNavigation(player, "次のページクリック: " + (currentPage + 1));
                debugLog("次のページクリック: " + (currentPage + 1));
                showPaginatedPage(player, title, currentPage + 1, itemsPerPage);
            }));
        }
        
        // ページ情報表示
        ItemStack pageInfo = createItem(Material.BOOK, ChatColor.GREEN + "ページ情報",
                Arrays.asList(
                    ChatColor.GRAY + "現在のページ: " + ChatColor.WHITE + (currentPage + 1) + "/" + totalPages,
                    ChatColor.GRAY + "総アイテム数: " + ChatColor.WHITE + allItems.size(),
                    ChatColor.GRAY + "表示中: " + ChatColor.WHITE + (endIndex - startIndex) + "個"
                ));
        pageItems.add(new GUIItemData(pageInfo, 49, null));
        
        String pageTitle = title + " (" + (currentPage + 1) + "/" + totalPages + ")";
        GUIMenuData menuData = new GUIMenuData(pageTitle, 54, pageItems);
        
        debugLog("ページネーションGUI作成: ページ=" + (currentPage + 1) + "/" + totalPages + 
                ", 表示アイテム数=" + (endIndex - startIndex) + ", 総アイテム数=" + pageItems.size());
        
        createCustomMenuGUI(player, menuData);
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
        if (openGUIs.containsKey(playerId)) {
            // 全てのGUI関連データを削除
            openGUIs.remove(playerId);
            guiData.remove(playerId);
            currentPages.remove(playerId);
            paginatedItems.remove(playerId);
            paginatedCallbacks.remove(playerId);
            
            // インベントリを閉じる
            player.closeInventory();
        }
    }
    
    /**
     * アイテムを作成するユーティリティメソッド
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
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
     * GUIManagerの統計情報を取得（デバッグ用）
     * @return 統計情報
     */
    public String getStatistics() {
        return String.format("開いているGUI: %d, ページネーション中: %d", 
                openGUIs.size(), currentPages.size());
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
        debugLog("GUI開いているか: " + openGUIs.containsKey(playerId));
        debugLog("===============================");
    }
    
    /**
     * GUIデバッグ機能を有効にするメソッド（テスト用）
     * @param player プレイヤー
     */
    public void enableDebugForPlayer(Player player) {
        setDebugMode(true);
        player.sendMessage(ChatColor.GREEN + "GUIデバッグモードが有効になりました。");
        player.sendMessage(ChatColor.GRAY + "現在の統計: " + getStatistics());
        
        UUID playerId = player.getUniqueId();
        if (currentPages.containsKey(playerId)) {
            player.sendMessage(ChatColor.GRAY + "ページネーション情報: " + getPageDebugInfo(player));
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

      UUID playerId = player.getUniqueId();
        
        // 管理対象のGUIかチェック
        if (!openGUIs.containsKey(playerId)) return;
        
        Inventory openInventory = openGUIs.get(playerId);
        
        // クリックされたインベントリが管理対象のGUIかチェック
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(openInventory)) {
            // プレイヤーインベントリのクリックの場合
            GUIMenuData menuData = guiData.get(playerId);
            if (menuData == null || !menuData.isAllowPlayerInventoryClick()) {
                event.setCancelled(true);
            }
            return;
        }
        
        // GUIインベントリのクリックの場合は必ずキャンセル
        event.setCancelled(true);
        
        GUIMenuData menuData = guiData.get(playerId);
        if (menuData == null) {
            debugLog("GUIデータが見つかりません: " + playerId);
            return;
        }
        
        int slot = event.getSlot();
        
        debugLog("GUI クリック検出: プレイヤー=" + player.getName() + 
                ", スロット=" + slot + ", アイテム数=" + menuData.getItems().size());
        
        // アイテムクリック処理
        for (GUIItemData itemData : menuData.getItems()) {
            if (itemData.getSlot() == slot) {
                debugLog("アイテムが見つかりました: スロット=" + slot);
                
                // 権限チェック
                if (!itemData.getPermission().isEmpty() && !hasGUIPermission(player, itemData.getPermission())) {
                    player.sendMessage(ChatColor.RED + "このアイテムを使用する権限がありません。");
                    return;
                }
                
                // 有効性チェック
                if (!itemData.isEnabled()) {
                    debugLog("アイテムが無効です: スロット=" + slot);
                    return;
                }
                
                // クリックアクション実行
                if (itemData.getClickAction() != null) {
                    debugLog("アクションを実行します: スロット=" + slot);
                    try {
                        itemData.getClickAction().accept(itemData);
                    } catch (Exception e) {
                        TempceLib.getInstance().getLogger().severe("GUIアクション実行中にエラーが発生しました: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    debugLog("アクションが設定されていません: スロット=" + slot);
                }
                return;
            }
        }
        
        debugLog("該当するアイテムが見つかりませんでした: スロット=" + slot);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

      UUID playerId = player.getUniqueId();
        
        // 管理対象のGUIが閉じられた場合のみデータを削除
        if (openGUIs.containsKey(playerId)) {
            Inventory closedInventory = event.getInventory();
            Inventory trackedInventory = openGUIs.get(playerId);
            
            // 実際に管理しているインベントリが閉じられたかチェック
            if (closedInventory.equals(trackedInventory)) {
                debugLog("GUI自動クローズ: プレイヤー=" + player.getName());
                
                // 全てのGUI関連データを削除（メモリリーク防止）
                openGUIs.remove(playerId);
                guiData.remove(playerId);
                currentPages.remove(playerId);
                paginatedItems.remove(playerId);
                paginatedCallbacks.remove(playerId);
            }
        }
    }
}
