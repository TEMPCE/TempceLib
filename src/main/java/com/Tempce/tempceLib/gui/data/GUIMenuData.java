package com.Tempce.tempceLib.gui.data;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * GUIメニューのデータクラス
 */
public class GUIMenuData {
    private final String title;
    private final int size;
    private final List<GUIItemData> items;
    private final Map<String, Object> properties;
    private final boolean allowPlayerInventoryClick;
    private final ItemStack fillItem;
    
    /**
     * GUIメニューデータのコンストラクタ
     * @param title メニュータイトル
     * @param size メニューサイズ（9の倍数）
     * @param items メニューアイテムリスト
     * @param properties 追加プロパティ
     * @param allowPlayerInventoryClick プレイヤーインベントリのクリックを許可するか
     * @param fillItem 空のスロットを埋めるアイテム（nullの場合は埋めない）
     */
    public GUIMenuData(String title, int size, List<GUIItemData> items, Map<String, Object> properties, 
                       boolean allowPlayerInventoryClick, ItemStack fillItem) {
        this.title = title;
        this.size = size;
        this.items = items;
        this.properties = properties;
        this.allowPlayerInventoryClick = allowPlayerInventoryClick;
        this.fillItem = fillItem;
    }
    
    /**
     * シンプルなコンストラクタ
     * @param title メニュータイトル
     * @param size メニューサイズ
     * @param items メニューアイテムリスト
     */
    public GUIMenuData(String title, int size, List<GUIItemData> items) {
        this(title, size, items, null, false, null);
    }
    
    /**
     * メニュータイトルを取得
     * @return メニュータイトル
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * メニューサイズを取得
     * @return メニューサイズ
     */
    public int getSize() {
        return size;
    }
    
    /**
     * メニューアイテムリストを取得
     * @return メニューアイテムリスト
     */
    public List<GUIItemData> getItems() {
        return items;
    }
    
    /**
     * 追加プロパティを取得
     * @return 追加プロパティ
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    /**
     * プレイヤーインベントリのクリックを許可するかを取得
     * @return プレイヤーインベントリのクリックを許可するか
     */
    public boolean isAllowPlayerInventoryClick() {
        return allowPlayerInventoryClick;
    }
    
    /**
     * 空のスロットを埋めるアイテムを取得
     * @return 空のスロットを埋めるアイテム
     */
    public ItemStack getFillItem() {
        return fillItem;
    }
}
