package com.Tempce.tempceLib.message;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 多言語サポートのためのメッセージ翻訳システム
 * 設定ファイルベースの翻訳機能を提供
 */
public class TranslationManager {
    
    private final Map<String, Map<String, String>> translations;
    private String defaultLanguage;
    
    public TranslationManager() {
        this.translations = new HashMap<>();
        this.defaultLanguage = "ja";
        loadDefaultTranslations();
    }
    
    /**
     * デフォルトの翻訳を読み込み
     */
    private void loadDefaultTranslations() {
        // 日本語翻訳
        Map<String, String> japanese = new HashMap<>();
        japanese.put("welcome", "§6サーバーへようこそ！");
        japanese.put("help.click", "§eヘルプはこちらをクリック");
        japanese.put("help.hover", "§7ヘルプとサポート情報を表示");
        japanese.put("admin.panel", "§8管理パネル");
        japanese.put("error.permission", "§c権限がありません");
        japanese.put("success.saved", "§a正常に保存されました");
        translations.put("ja", japanese);
        
        // 英語翻訳
        Map<String, String> english = new HashMap<>();
        english.put("welcome", "§6Welcome to the server!");
        english.put("help.click", "§eClick here for help");
        english.put("help.hover", "§7Get help and information");
        english.put("admin.panel", "§8Admin Panel");
        english.put("error.permission", "§cYou don't have permission");
        english.put("success.saved", "§aSuccessfully saved");
        translations.put("en", english);
    }
    
    /**
     * 翻訳を追加
     * @param language 言語コード
     * @param key 翻訳キー
     * @param value 翻訳値
     */
    public void addTranslation(String language, String key, String value) {
        translations.computeIfAbsent(language, k -> new HashMap<>()).put(key, value);
    }
    
    /**
     * 翻訳を取得
     * @param language 言語コード
     * @param key 翻訳キー
     * @return 翻訳されたテキスト
     */
    public String getTranslation(String language, String key) {
        Map<String, String> langMap = translations.get(language);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        
        // デフォルト言語にフォールバック
        Map<String, String> defaultMap = translations.get(defaultLanguage);
        if (defaultMap != null && defaultMap.containsKey(key)) {
            return defaultMap.get(key);
        }
        
        // 翻訳が見つからない場合はキーをそのまま返す
        return key;
    }
    
    /**
     * プレイヤーの言語設定に基づいて翻訳を取得
     * @param player プレイヤー
     * @param key 翻訳キー
     * @return 翻訳されたテキスト
     */
    public String getTranslation(Player player, String key) {
        String language = getPlayerLanguage(player);
        return getTranslation(language, key);
    }
    
    /**
     * プレイヤーの言語設定を取得
     * @param player プレイヤー
     * @return 言語コード
     */
    public String getPlayerLanguage(Player player) {
        // Spigot APIを使用してプレイヤーのロケールを取得
        String locale = player.getLocale();
        if (locale != null && locale.length() >= 2) {
            String language = locale.substring(0, 2).toLowerCase();
            if (translations.containsKey(language)) {
                return language;
            }
        }
        return defaultLanguage;
    }
    
    /**
     * 翻訳されたメッセージコンポーネントを作成
     * @param player プレイヤー
     * @param key 翻訳キー
     * @return TextComponent
     */
    public TextComponent createTranslatedMessage(Player player, String key) {
        String translatedText = getTranslation(player, key);
        return new TextComponent(translatedText);
    }
    
    /**
     * デフォルト言語を設定
     * @param defaultLanguage デフォルト言語コード
     */
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
    
    /**
     * サポートしている言語の一覧を取得
     * @return 言語コードの配列
     */
    public String[] getSupportedLanguages() {
        return translations.keySet().toArray(new String[0]);
    }
}
