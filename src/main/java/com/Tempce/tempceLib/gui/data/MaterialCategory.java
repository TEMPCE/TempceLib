package com.Tempce.tempceLib.gui.data;

/**
 * マテリアルカテゴリ列挙型
 * アイテム選択GUIで使用するマテリアルの分類
 */
public enum MaterialCategory {
    ALL("全てのアイテム"),
    TOOLS("ツール類"),
    BLOCKS("ブロック類"),
    NATURE_BLOCKS("自然ブロック"),
    WEAPONS_ARMOR("武器・防具"),
    FOOD("食べ物"),
    DECORATION("装飾ブロック");
    
    private final String displayName;
    
    MaterialCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
