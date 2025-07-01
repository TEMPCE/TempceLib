package com.Tempce.tempceLib.gui.data;

import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * GUIアイテムのデータクラス
 */
public class GUIItemData {
    private final ItemStack itemStack;
    private final int slot;
    private final Consumer<GUIItemData> clickAction;
    private final String permission;
    private final boolean enabled;
    
    /**
     * GUIアイテムデータのコンストラクタ
     * @param itemStack アイテムスタック
     * @param slot スロット番号
     * @param clickAction クリック時のアクション
     * @param permission 必要な権限（空文字列の場合は権限チェックなし）
     * @param enabled 有効かどうか
     */
    public GUIItemData(ItemStack itemStack, int slot, Consumer<GUIItemData> clickAction, String permission, boolean enabled) {
        this.itemStack = itemStack;
        this.slot = slot;
        this.clickAction = clickAction;
        this.permission = permission;
        this.enabled = enabled;
    }
    
    /**
     * 権限チェックなしのコンストラクタ
     * @param itemStack アイテムスタック
     * @param slot スロット番号
     * @param clickAction クリック時のアクション
     */
    public GUIItemData(ItemStack itemStack, int slot, Consumer<GUIItemData> clickAction) {
        this(itemStack, slot, clickAction, "", true);
    }
    
    /**
     * アイテムスタックを取得
     * @return アイテムスタック
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
    
    /**
     * スロット番号を取得
     * @return スロット番号
     */
    public int getSlot() {
        return slot;
    }
    
    /**
     * クリック時のアクションを取得
     * @return クリック時のアクション
     */
    public Consumer<GUIItemData> getClickAction() {
        return clickAction;
    }
    
    /**
     * 必要な権限を取得
     * @return 必要な権限
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * 有効かどうかを取得
     * @return 有効かどうか
     */
    public boolean isEnabled() {
        return enabled;
    }
}
