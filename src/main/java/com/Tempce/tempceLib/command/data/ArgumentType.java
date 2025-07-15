package com.Tempce.tempceLib.command.data;

/**
 * コマンド引数のタイプを定義するenum
 */
public enum ArgumentType {
    /**
     * 文字列引数（デフォルト）
     */
    STRING("文字列"),
    
    /**
     * プレイヤー名引数
     */
    PLAYER("プレイヤー"),
    
    /**
     * 整数引数
     */
    INTEGER("整数"),
    
    /**
     * 小数引数
     */
    DOUBLE("小数"),
    
    /**
     * アイテムID引数（全種類）
     */
    ITEM_ID("アイテムID"),
    
    /**
     * アイテムID引数（ツール類）
     */
    ITEM_ID_TOOL("ツール類"),
    
    /**
     * アイテムID引数（ブロック）
     */
    ITEM_ID_BLOCK("ブロック"),
    
    /**
     * アイテムID引数（自然ブロック）
     */
    ITEM_ID_NATURE_BLOCK("自然ブロック"),
    
    /**
     * アイテムID引数（武器・防具）
     */
    ITEM_ID_WEAPON_ARMOR("武器・防具"),
    
    /**
     * アイテムID引数（食べ物）
     */
    ITEM_ID_FOOD("食べ物"),
    
    /**
     * アイテムID引数（装飾ブロック）
     */
    ITEM_ID_DECORATION("装飾ブロック"),
    
    /**
     * エンティティID引数
     */
    ENTITY_ID("エンティティID"),
    
    /**
     * ワールド名引数
     */
    WORLD("ワールド"),
    
    /**
     * 座標（X）引数
     */
    COORDINATE_X("X座標"),
    
    /**
     * 座標（Y）引数
     */
    COORDINATE_Y("Y座標"),
    
    /**
     * 座標（Z）引数
     */
    COORDINATE_Z("Z座標"),
    
    /**
     * ブール値引数
     */
    BOOLEAN("真偽値"),
    
    /**
     * エンチャント引数
     */
    ENCHANTMENT("エンチャント"),
    
    /**
     * ポーション効果引数
     */
    POTION_EFFECT("ポーション効果");
    
    private final String displayName;
    
    ArgumentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
