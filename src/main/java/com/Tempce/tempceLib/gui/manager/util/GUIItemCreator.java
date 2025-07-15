package com.Tempce.tempceLib.gui.manager.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI用アイテム作成ユーティリティクラス
 */
public class GUIItemCreator {
    
    /**
     * アイテムを作成するユーティリティメソッド
     * @param material マテリアル
     * @param name 表示名
     * @param lore 説明文
     * @return 作成されたItemStack
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
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
     * プレイヤーの頭アイテムを作成
     * @param player 対象プレイヤー
     * @return プレイヤーの頭のItemStack
     */
    public static ItemStack createPlayerHead(Player player) {
        return createPlayerHeadWithSkin(player.getName());
    }
    
    /**
     * プレイヤーの頭アイテムをスキン付きで作成
     * @param playerName プレイヤー名
     * @return プレイヤーの頭のItemStack
     */
    public static ItemStack createPlayerHeadWithSkin(String playerName) {
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
                    @SuppressWarnings("deprecation")
                    org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    if (offlinePlayer.hasPlayedBefore()) {
                        skullMeta.setOwningPlayer(offlinePlayer);
                    }
                }
            } catch (Exception e) {
                // スキン設定に失敗した場合はデフォルトのスカルを使用
                // ログ出力は呼び出し元で行う
            }
            
            skull.setItemMeta(skullMeta);
        }
        return skull;
    }
}
