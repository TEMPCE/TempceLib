package com.Tempce.tempceLib.message;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * メッセージフォーマットとユーティリティクラス
 * 色コード変換、プレースホルダー置換などの機能を提供
 */
public class MessageUtils {
    
    // 色コードパターン（&による色コード）
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    
    // プレースホルダーパターン（{placeholder}形式）
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    /**
     * 色コードを変換（&文字を§に変換）
     * @param text 変換するテキスト
     * @return 変換されたテキスト
     */
    public static String colorize(String text) {
        if (text == null) return null;
        return COLOR_PATTERN.matcher(text).replaceAll("§$1");
    }
    
    /**
     * プレースホルダーを置換
     * @param text 元のテキスト
     * @param placeholder プレースホルダー名
     * @param value 置換する値
     * @return 置換されたテキスト
     */
    public static String replacePlaceholder(String text, String placeholder, String value) {
        if (text == null) return null;
        return text.replace("{" + placeholder + "}", value);
    }
    
    /**
     * プレイヤー関連のプレースホルダーを置換
     * @param text 元のテキスト
     * @param player プレイヤー
     * @return 置換されたテキスト
     */
    public static String replacePlayerPlaceholders(String text, Player player) {
        if (text == null || player == null) return text;
        
        text = replacePlaceholder(text, "player", player.getName());
        text = replacePlaceholder(text, "displayname", player.getDisplayName());
        text = replacePlaceholder(text, "uuid", player.getUniqueId().toString());
        text = replacePlaceholder(text, "world", player.getWorld().getName());
        text = replacePlaceholder(text, "x", String.valueOf(player.getLocation().getBlockX()));
        text = replacePlaceholder(text, "y", String.valueOf(player.getLocation().getBlockY()));
        text = replacePlaceholder(text, "z", String.valueOf(player.getLocation().getBlockZ()));
        text = replacePlaceholder(text, "health", String.valueOf(Math.round(player.getHealth())));
        text = replacePlaceholder(text, "level", String.valueOf(player.getLevel()));
        
        return text;
    }
    
    /**
     * テキストをセンタリング（チャット幅に合わせて中央揃え）
     * @param text センタリングするテキスト
     * @return センタリングされたテキスト
     */
    public static String centerText(String text) {
        if (text == null) return null;
        
        int chatWidth = 53; // Minecraftのチャット幅（概算）
        int textLength = stripColors(text).length();
        
        if (textLength >= chatWidth) return text;
        
        int padding = (chatWidth - textLength) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            builder.append(" ");
        }
        builder.append(text);
        
        return builder.toString();
    }
    
    /**
     * 色コードを除去
     * @param text 元のテキスト
     * @return 色コードが除去されたテキスト
     */
    public static String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    /**
     * プログレスバーを作成
     * @param current 現在の値
     * @param max 最大値
     * @param length バーの長さ
     * @param filledChar 満たされた部分の文字
     * @param emptyChar 空の部分の文字
     * @param filledColor 満たされた部分の色
     * @param emptyColor 空の部分の色
     * @return プログレスバー文字列
     */
    public static String createProgressBar(int current, int max, int length, 
                                         char filledChar, char emptyChar,
                                         String filledColor, String emptyColor) {
        
        double percentage = (double) current / max;
        int filledLength = (int) (length * percentage);
        
        StringBuilder builder = new StringBuilder();
        builder.append(filledColor);
        for (int i = 0; i < filledLength; i++) {
            builder.append(filledChar);
        }
        builder.append(emptyColor);
        for (int i = filledLength; i < length; i++) {
            builder.append(emptyChar);
        }
        
        return builder.toString();
    }
    
    /**
     * デフォルトのプログレスバーを作成
     * @param current 現在の値
     * @param max 最大値
     * @return プログレスバー文字列
     */
    public static String createProgressBar(int current, int max) {
        return createProgressBar(current, max, 20, '█', '▒', "§a", "§7");
    }
    
    /**
     * テキストを指定幅で折り返し
     * @param text 元のテキスト
     * @param width 折り返し幅
     * @return 折り返されたテキスト配列
     */
    public static String[] wrapText(String text, int width) {
        if (text == null) return new String[0];
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (stripColors(testLine).length() <= width) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (result.length() > 0) result.append("\n");
                result.append(currentLine);
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            if (result.length() > 0) result.append("\n");
            result.append(currentLine);
        }
        
        return result.toString().split("\n");
    }
}
