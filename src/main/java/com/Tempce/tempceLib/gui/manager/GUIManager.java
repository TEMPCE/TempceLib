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
        List<GUIItemData> guiItems = new ArrayList<>();
        int currentValue = Math.max(min, Math.min(max, defaultValue));
        
        // 数値表示アイテム
        ItemStack displayItem = createItem(Material.PAPER, ChatColor.GREEN + "現在の値: " + currentValue, 
                Arrays.asList(ChatColor.GRAY + "範囲: " + min + " - " + max));
        guiItems.add(new GUIItemData(displayItem, 4, null));
        
        // 減少ボタン
        if (currentValue > min) {
            ItemStack decreaseItem = createItem(Material.RED_WOOL, ChatColor.RED + "-1", 
                    Arrays.asList(ChatColor.GRAY + "クリックして値を減らす"));
            guiItems.add(new GUIItemData(decreaseItem, 3, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue - 1, onSelect)));
        }
        
        // 増加ボタン
        if (currentValue < max) {
            ItemStack increaseItem = createItem(Material.LIME_WOOL, ChatColor.GREEN + "+1", 
                    Arrays.asList(ChatColor.GRAY + "クリックして値を増やす"));
            guiItems.add(new GUIItemData(increaseItem, 5, (guiItemData) -> 
                    createNumberSelectionGUI(player, title, min, max, currentValue + 1, onSelect)));
        }
        
        // 確定ボタン
        ItemStack confirmItem = createItem(Material.EMERALD, ChatColor.GREEN + "確定", 
                Arrays.asList(ChatColor.GRAY + "この値で決定する"));
        guiItems.add(new GUIItemData(confirmItem, 8, (guiItemData) -> {
            closeGUI(player);
            onSelect.accept(currentValue);
        }));
        
        GUIMenuData menuData = new GUIMenuData(title, 9, guiItems);
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createConfirmationGUI(Player player, String title, String message, Runnable onConfirm, Runnable onCancel) {
        List<GUIItemData> guiItems = new ArrayList<>();
        
        // メッセージ表示
        ItemStack messageItem = createItem(Material.BOOK, ChatColor.YELLOW + "確認", 
                Arrays.asList(ChatColor.WHITE + message));
        guiItems.add(new GUIItemData(messageItem, 4, null));
        
        // 確認ボタン
        ItemStack confirmItem = createItem(Material.EMERALD, ChatColor.GREEN + "はい", 
                Arrays.asList(ChatColor.GRAY + "クリックして確認"));
        guiItems.add(new GUIItemData(confirmItem, 2, (guiItemData) -> {
            closeGUI(player);
            onConfirm.run();
        }));
        
        // キャンセルボタン
        ItemStack cancelItem = createItem(Material.REDSTONE, ChatColor.RED + "いいえ", 
                Arrays.asList(ChatColor.GRAY + "クリックしてキャンセル"));
        guiItems.add(new GUIItemData(cancelItem, 6, (guiItemData) -> {
            closeGUI(player);
            onCancel.run();
        }));
        
        GUIMenuData menuData = new GUIMenuData(title, 9, guiItems);
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createCustomMenuGUI(Player player, GUIMenuData menuData) {
        Inventory inventory = Bukkit.createInventory(null, menuData.getSize(), menuData.getTitle());
        
        // Fill itemがある場合は全スロットを埋める
        if (menuData.getFillItem() != null) {
            for (int i = 0; i < menuData.getSize(); i++) {
                inventory.setItem(i, menuData.getFillItem());
            }
        }
        
        // アイテムを配置
        for (GUIItemData itemData : menuData.getItems()) {
            if (itemData.getSlot() < menuData.getSize()) {
                // 権限チェック
                if (!itemData.getPermission().isEmpty() && !hasGUIPermission(player, itemData.getPermission())) {
                    continue;
                }
                inventory.setItem(itemData.getSlot(), itemData.getItemStack());
            }
        }
        
        UUID playerId = player.getUniqueId();
        openGUIs.put(playerId, inventory);
        guiData.put(playerId, menuData);
        
        player.openInventory(inventory);
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
                Arrays.asList(ChatColor.GRAY + "コマンド一覧に戻る"));
        guiItems.add(new GUIItemData(backItem, 53, (guiItemData) -> openCommandAutoGUI(player)));
        
        int size = Math.min(54, ((slot - 1) / 9 + 1) * 9);
        GUIMenuData menuData = new GUIMenuData(ChatColor.DARK_BLUE + commandName + " コマンド", size, guiItems);
        createCustomMenuGUI(player, menuData);
    }
    
    @Override
    public void createPaginatedGUI(Player player, String title, List<GUIItemData> items, int itemsPerPage, Consumer<GUIItemData> onItemClick) {
        UUID playerId = player.getUniqueId();
        paginatedItems.put(playerId, items);
        if (onItemClick != null) {
            paginatedCallbacks.put(playerId, onItemClick);
        }
        currentPages.put(playerId, 0);
        
        showPaginatedPage(player, title, 0, itemsPerPage);
    }
    
    /**
     * ページネーション付きGUIの特定ページを表示
     */
    private void showPaginatedPage(Player player, String title, int page, int itemsPerPage) {
        UUID playerId = player.getUniqueId();
        List<GUIItemData> allItems = paginatedItems.get(playerId);
        if (allItems == null) return;
        
        int totalPages = (allItems.size() - 1) / itemsPerPage + 1;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());
        
        List<GUIItemData> pageItems = new ArrayList<>();
        
        // ページ内のアイテムを追加
        for (int i = startIndex; i < endIndex; i++) {
            GUIItemData originalItem = allItems.get(i);
            int newSlot = i - startIndex;
            pageItems.add(new GUIItemData(originalItem.getItemStack(), newSlot, 
                    paginatedCallbacks.getOrDefault(playerId, originalItem.getClickAction()), 
                    originalItem.getPermission(), originalItem.isEnabled()));
        }
        
        // ナビゲーションボタンを追加
        if (page > 0) {
            ItemStack prevItem = createItem(Material.ARROW, ChatColor.YELLOW + "前のページ",
                    Arrays.asList(ChatColor.GRAY + "ページ " + page + "/" + totalPages));
            pageItems.add(new GUIItemData(prevItem, 45, (guiItemData) -> 
                    showPaginatedPage(player, title, page - 1, itemsPerPage)));
        }
        
        if (page < totalPages - 1) {
            ItemStack nextItem = createItem(Material.ARROW, ChatColor.YELLOW + "次のページ",
                    Arrays.asList(ChatColor.GRAY + "ページ " + (page + 2) + "/" + totalPages));
            pageItems.add(new GUIItemData(nextItem, 53, (guiItemData) -> 
                    showPaginatedPage(player, title, page + 1, itemsPerPage)));
        }
        
        // ページ情報表示
        ItemStack pageInfo = createItem(Material.BOOK, ChatColor.GREEN + "ページ情報",
                Arrays.asList(ChatColor.GRAY + "現在のページ: " + (page + 1) + "/" + totalPages,
                             ChatColor.GRAY + "総アイテム数: " + allItems.size()));
        pageItems.add(new GUIItemData(pageInfo, 49, null));
        
        currentPages.put(playerId, page);
        
        String pageTitle = title + " (" + (page + 1) + "/" + totalPages + ")";
        GUIMenuData menuData = new GUIMenuData(pageTitle, 54, pageItems);
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
        openGUIs.remove(playerId);
        guiData.remove(playerId);
        currentPages.remove(playerId);
        paginatedItems.remove(playerId);
        paginatedCallbacks.remove(playerId);
        player.closeInventory();
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
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        if (!openGUIs.containsKey(playerId)) return;
        
        event.setCancelled(true);
        
        GUIMenuData menuData = guiData.get(playerId);
        if (menuData == null) return;
        
        // プレイヤーインベントリのクリックが許可されていない場合はキャンセル
        if (!menuData.isAllowPlayerInventoryClick() && event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        
        int slot = event.getSlot();
        for (GUIItemData itemData : menuData.getItems()) {
            if (itemData.getSlot() == slot && itemData.getClickAction() != null) {
                // 権限チェック
                if (!itemData.getPermission().isEmpty() && !hasGUIPermission(player, itemData.getPermission())) {
                    player.sendMessage(ChatColor.RED + "このアイテムを使用する権限がありません。");
                    return;
                }
                
                if (itemData.isEnabled()) {
                    itemData.getClickAction().accept(itemData);
                }
                break;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // ページネーション関連のデータは保持して、GUIが再開できるようにする
        openGUIs.remove(playerId);
        guiData.remove(playerId);
    }
}
