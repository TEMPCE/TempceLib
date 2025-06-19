package com.Tempce.tempceLib.message;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * リッチメッセージシステムのメイン管理クラス
 * Spigot APIを活用したインタラクティブなメッセージ作成を支援
 */
public class MessageManager {
    
    /**
     * MessageBuilderの新しいインスタンスを作成
     * @param initialText 初期テキスト
     * @return MessageBuilder
     */
    public MessageBuilder builder(String initialText) {
        return new MessageBuilder(initialText);
    }
    
    /**
     * MessageBuilderの新しいインスタンスを作成（空のテキストから開始）
     * @return MessageBuilder
     */
    public MessageBuilder builder() {
        return new MessageBuilder();
    }
    
    /**
     * シンプルなメッセージを作成
     * @param text メッセージテキスト
     * @return TextComponent
     */
    public TextComponent createSimpleMessage(String text) {
        return new TextComponent(text);
    }
    
    /**
     * クリッカブルメッセージを作成
     * @param text 表示テキスト
     * @param command クリック時に実行するコマンド
     * @return TextComponent
     */
    public TextComponent createClickableMessage(String text, String command) {
        TextComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }
    
    /**
     * ホバー可能なメッセージを作成
     * @param text 表示テキスト
     * @param hoverText ホバー時に表示するテキスト
     * @return TextComponent
     */
    public TextComponent createHoverMessage(String text, String hoverText) {
        TextComponent component = new TextComponent(text);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(hoverText).create()));
        return component;
    }
    
    /**
     * プレイヤーにメッセージを送信
     * @param player 対象プレイヤー
     * @param component 送信するコンポーネント
     */
    public void sendMessage(Player player, TextComponent component) {
        player.spigot().sendMessage(component);
    }
}
