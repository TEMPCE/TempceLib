package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * GUIイベント処理を管理するクラス
 */
public class GUIEventHandler implements Listener {
    private final Map<UUID, Inventory> openGUIs = new ConcurrentHashMap<>();
    private final Map<UUID, GUIMenuData> guiData = new ConcurrentHashMap<>();
    private boolean debugMode = false;
    
    // 権限チェック関数とデータクリーンアップ関数
    private final BiFunction<Player, String, Boolean> permissionChecker;
    private final Runnable dataCleanupCallback;
    
    /**
     * コンストラクタ
     * @param permissionChecker 権限チェック関数
     * @param dataCleanupCallback データクリーンアップコールバック
     */
    public GUIEventHandler(BiFunction<Player, String, Boolean> permissionChecker,
                           Runnable dataCleanupCallback) {
        this.permissionChecker = permissionChecker;
        this.dataCleanupCallback = dataCleanupCallback;
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
            TempceLib.getInstance().getLogger().info("[GUI EVENT DEBUG] " + message);
        }
    }
    
    /**
     * GUIデータを登録
     * @param playerId プレイヤーID
     * @param inventory インベントリ
     * @param menuData メニューデータ
     */
    public void registerGUI(UUID playerId, Inventory inventory, GUIMenuData menuData) {
        openGUIs.put(playerId, inventory);
        guiData.put(playerId, menuData);
    }
    
    /**
     * GUIデータを削除
     * @param playerId プレイヤーID
     */
    public void unregisterGUI(UUID playerId) {
        openGUIs.remove(playerId);
        guiData.remove(playerId);
    }
    
    /**
     * プレイヤーがGUIを開いているかチェック
     * @param playerId プレイヤーID
     * @return GUIを開いているか
     */
    public boolean hasOpenGUI(UUID playerId) {
        return openGUIs.containsKey(playerId);
    }
    
    /**
     * 開いているGUIの統計情報を取得
     * @return 統計情報
     */
    public String getStatistics() {
        return String.format("開いているGUI: %d", openGUIs.size());
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
                if (!itemData.getPermission().isEmpty() && 
                    !permissionChecker.apply(player, itemData.getPermission())) {
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
                
                // GUI関連データを削除
                unregisterGUI(playerId);
                
                // 外部データクリーンアップコールバック実行
                if (dataCleanupCallback != null) {
                    dataCleanupCallback.run();
                }
            }
        }
    }
}
