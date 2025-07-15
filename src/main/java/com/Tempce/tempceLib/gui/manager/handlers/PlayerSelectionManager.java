package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.manager.util.GUIItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * プレイヤー選択GUI機能を管理するクラス
 */
public class PlayerSelectionManager {
    
    /**
     * プレイヤー情報を保持するレコードクラス
     */
    public record PlayerInfo(String name, boolean online, Player onlinePlayer) {
    }
    
    /**
     * プレイヤー選択GUIを作成（権限フィルタなし）
     * @param player プレイヤー
     * @param title タイトル
     * @param onSelect 選択時のコールバック
     * @param paginationCreator ページネーションGUI作成関数
     */
    public void createPlayerSelectionGUI(Player player, String title, Consumer<Player> onSelect,
                                         Consumer<PlayerSelectionGUIData> paginationCreator) {
        createPlayerSelectionGUI(player, title, null, onSelect, paginationCreator);
    }
    
    /**
     * プレイヤー選択GUIを作成（権限フィルタあり）
     * @param player プレイヤー
     * @param title タイトル
     * @param permission 権限フィルタ
     * @param onSelect 選択時のコールバック
     * @param paginationCreator ページネーションGUI作成関数
     */
    public void createPlayerSelectionGUI(Player player, String title, String permission, Consumer<Player> onSelect,
                                         Consumer<PlayerSelectionGUIData> paginationCreator) {
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
        List<GUIItemData> guiItems = createPlayerGUIItems(availablePlayers, permission, onSelect, player);
        
        // ページネーション付きGUIとして表示
        paginationCreator.accept(new PlayerSelectionGUIData(title, guiItems, 45, null));
    }
    
    /**
     * 全プレイヤー選択GUIを作成
     * @param player プレイヤー
     * @param title タイトル
     * @param includeOffline オフラインプレイヤーを含むか
     * @param onSelect 選択時のコールバック
     * @param paginationCreator ページネーションGUI作成関数
     */
    public void createAllPlayerSelectionGUI(Player player, String title, boolean includeOffline, 
                                            Consumer<Player> onSelect,
                                            Consumer<PlayerSelectionGUIData> paginationCreator) {
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
        List<GUIItemData> guiItems = createPlayerInfoGUIItems(availablePlayers, onSelect, player);
        
        // ページネーション付きGUIとして表示
        paginationCreator.accept(new PlayerSelectionGUIData(
            title + " (" + availablePlayers.size() + "人)", guiItems, 45, null));
    }
    
    /**
     * プレイヤー名選択GUIを作成
     * @param player プレイヤー
     * @param title タイトル
     * @param includeOffline オフラインプレイヤーを含むか
     * @param onSelectName 名前選択時のコールバック
     * @param paginationCreator ページネーションGUI作成関数
     */
    public void createPlayerNameSelectionGUI(Player player, String title, boolean includeOffline, 
                                             Consumer<String> onSelectName,
                                             Consumer<PlayerSelectionGUIData> paginationCreator) {
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
        List<GUIItemData> guiItems = createPlayerNameGUIItems(availablePlayers, onSelectName, player);
        
        // ページネーション付きGUIとして表示
        paginationCreator.accept(new PlayerSelectionGUIData(
            title + " (" + availablePlayers.size() + "人)", guiItems, 45, null));
    }
    
    /**
     * オンラインプレイヤー用のGUIアイテムを作成
     */
    private List<GUIItemData> createPlayerGUIItems(List<Player> players, String permission, 
                                                   Consumer<Player> onSelect, Player requestingPlayer) {
        List<GUIItemData> guiItems = new ArrayList<>();
        
        for (Player targetPlayer : players) {
            ItemStack playerHead = GUIItemCreator.createPlayerHeadWithSkin(targetPlayer.getName());
            
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
                requestingPlayer.closeInventory();
                onSelect.accept(targetPlayer);
            }));
        }
        
        return guiItems;
    }
    
    /**
     * プレイヤー情報用のGUIアイテムを作成
     */
    private List<GUIItemData> createPlayerInfoGUIItems(List<PlayerInfo> playerInfos, 
                                                        Consumer<Player> onSelect, Player requestingPlayer) {
        List<GUIItemData> guiItems = new ArrayList<>();
        
        for (PlayerInfo playerInfo : playerInfos) {
            ItemStack playerHead = GUIItemCreator.createPlayerHeadWithSkin(playerInfo.name());
            
            List<String> lore = createPlayerInfoLore(playerInfo);
            
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + playerInfo.name());
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            
            guiItems.add(new GUIItemData(playerHead, -1, (guiItemData) -> {
                requestingPlayer.closeInventory();
                if (playerInfo.online() && playerInfo.onlinePlayer() != null) {
                    onSelect.accept(playerInfo.onlinePlayer());
                } else {
                    // オフラインプレイヤーの場合、プレイヤー名のメッセージを送信
                    requestingPlayer.sendMessage(ChatColor.YELLOW + "選択されたプレイヤー: " + playerInfo.name() + " (オフライン)");
                }
            }));
        }
        
        return guiItems;
    }
    
    /**
     * プレイヤー名選択用のGUIアイテムを作成
     */
    private List<GUIItemData> createPlayerNameGUIItems(List<PlayerInfo> playerInfos, 
                                                        Consumer<String> onSelectName, Player requestingPlayer) {
        List<GUIItemData> guiItems = new ArrayList<>();
        
        for (PlayerInfo playerInfo : playerInfos) {
            ItemStack playerHead = GUIItemCreator.createPlayerHeadWithSkin(playerInfo.name());
            
            List<String> lore = createPlayerInfoLore(playerInfo);
            
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + playerInfo.name());
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            
            guiItems.add(new GUIItemData(playerHead, -1, (guiItemData) -> {
                requestingPlayer.closeInventory();
                onSelectName.accept(playerInfo.name());
            }));
        }
        
        return guiItems;
    }
    
    /**
     * プレイヤー情報の説明文を作成
     */
    private List<String> createPlayerInfoLore(PlayerInfo playerInfo) {
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
    
    /**
     * プレイヤー選択GUI用のデータクラス
     */
    public static class PlayerSelectionGUIData {
        public final String title;
        public final List<GUIItemData> items;
        public final int itemsPerPage;
        public final Consumer<GUIItemData> onItemClick;
        
        public PlayerSelectionGUIData(String title, List<GUIItemData> items, int itemsPerPage, Consumer<GUIItemData> onItemClick) {
            this.title = title;
            this.items = items;
            this.itemsPerPage = itemsPerPage;
            this.onItemClick = onItemClick;
        }
    }
}
