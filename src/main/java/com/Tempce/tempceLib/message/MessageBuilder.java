package com.Tempce.tempceLib.message;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * リッチメッセージ作成のためのビルダークラス
 * メソッドチェーンによる直感的なメッセージ構築を提供
 */
public class MessageBuilder {
    
    private TextComponent mainComponent;
    private TextComponent currentComponent;
    
    /**
     * 空のMessageBuilderを作成
     */
    public MessageBuilder() {
        this.mainComponent = new TextComponent("");
        this.currentComponent = null;
    }
    
    /**
     * 初期テキストを持つMessageBuilderを作成
     * @param initialText 初期テキスト
     */
    public MessageBuilder(String initialText) {
        this.mainComponent = new TextComponent(initialText);
        this.currentComponent = this.mainComponent;
    }
    
    /**
     * 新しいテキストを追加
     * @param text 追加するテキスト
     * @return このMessageBuilder
     */
    public MessageBuilder append(String text) {
        TextComponent newComponent = new TextComponent(text);
        this.mainComponent.addExtra(newComponent);
        this.currentComponent = newComponent;
        return this;
    }
    
    /**
     * 改行を追加
     * @return このMessageBuilder
     */
    public MessageBuilder newLine() {
        return append("\n");
    }
    
    /**
     * 現在のコンポーネントにクリックイベントを追加
     * @param command クリック時に実行するコマンド
     * @return このMessageBuilder
     */
    public MessageBuilder click(String command) {
        if (currentComponent != null) {
            currentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        return this;
    }
    
    /**
     * 現在のコンポーネントにクリックイベントを追加（URLを開く）
     * @param url 開くURL
     * @return このMessageBuilder
     */
    public MessageBuilder clickUrl(String url) {
        if (currentComponent != null) {
            currentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        }
        return this;
    }
    
    /**
     * 現在のコンポーネントにクリックイベントを追加（テキストを提案）
     * @param text 提案するテキスト
     * @return このMessageBuilder
     */
    public MessageBuilder suggest(String text) {
        if (currentComponent != null) {
            currentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        }
        return this;
    }
    
    /**
     * 現在のコンポーネントにホバーイベントを追加
     * @param hoverText ホバー時に表示するテキスト
     * @return このMessageBuilder
     */
    public MessageBuilder hover(String hoverText) {
        if (currentComponent != null) {
            currentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(hoverText).create()));
        }
        return this;
    }
    
    /**
     * 現在のコンポーネントにホバーイベントを追加（複数行対応）
     * @param hoverLines ホバー時に表示する行の配列
     * @return このMessageBuilder
     */
    public MessageBuilder hover(String... hoverLines) {
        if (currentComponent != null) {
            ComponentBuilder builder = new ComponentBuilder("");
            for (int i = 0; i < hoverLines.length; i++) {
                if (i > 0) {
                    builder.append("\n");
                }
                builder.append(hoverLines[i]);
            }
            currentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                builder.create()));
        }
        return this;
    }
    
    /**
     * クリックとホバーを同時に設定（便利メソッド）
     * @param command クリック時のコマンド
     * @param hoverText ホバー時のテキスト
     * @return このMessageBuilder
     */
    public MessageBuilder clickAndHover(String command, String hoverText) {
        return click(command).hover(hoverText);
    }
    
    /**
     * 作成したTextComponentを取得
     * @return 構築されたTextComponent
     */
    public TextComponent build() {
        return mainComponent;
    }
    
    /**
     * 作成したメッセージを文字列として取得（デバッグ用）
     * @return メッセージの文字列表現
     */
    @Override
    public String toString() {
        return mainComponent.toPlainText();
    }
}
