package com.Tempce.tempceLib.command.data;

import java.util.List;

/**
 * コマンド引数の情報を保持するクラス
 */
public class ArgumentData {
    private final String name;
    private final ArgumentType type;
    private final String description;
    private final boolean required;
    private final String defaultValue;
    private final List<String> suggestions;
    private final double min;
    private final double max;
    
    public ArgumentData(String name, ArgumentType type, String description, 
                       boolean required, String defaultValue, List<String> suggestions,
                       double min, double max) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.suggestions = suggestions;
        this.min = min;
        this.max = max;
    }
    
    public String getName() {
        return name;
    }
    
    public ArgumentType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    /**
     * 引数が数値型かどうかを判定
     */
    public boolean isNumeric() {
        return type == ArgumentType.INTEGER || type == ArgumentType.DOUBLE ||
               type == ArgumentType.COORDINATE_X || type == ArgumentType.COORDINATE_Y || 
               type == ArgumentType.COORDINATE_Z;
    }
    
    /**
     * 引数が選択式かどうかを判定
     */
    public boolean hasSelections() {
        return type == ArgumentType.PLAYER || type == ArgumentType.WORLD ||
               type == ArgumentType.ITEM_ID || type == ArgumentType.ENCHANTMENT ||
               type == ArgumentType.POTION_EFFECT || type == ArgumentType.BOOLEAN ||
               !suggestions.isEmpty();
    }
}
