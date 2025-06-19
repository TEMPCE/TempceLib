package com.Tempce.tempceLib.message.examples;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.message.MessageBuilder;
import com.Tempce.tempceLib.message.MessageManager;
import com.Tempce.tempceLib.message.MessageUtils;
import com.Tempce.tempceLib.message.TranslationManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * メッセージシステムの使用例を示すコマンドクラス
 * /tempce message で様々なメッセージ機能をテスト可能
 */
public class MessageExampleCommand implements CommandExecutor {

    private final TempceLib plugin;
    private final MessageManager messageManager;
    private final TranslationManager translationManager;

    public MessageExampleCommand(TempceLib plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.translationManager = plugin.getTranslationManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみが実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "welcome":
                sendWelcomeMessage(player);
                break;
            case "interactive":
                sendInteractiveMessage(player);
                break;
            case "progress":
                sendProgressMessage(player);
                break;
            case "translation":
                sendTranslationExample(player);
                break;
            case "utils":
                sendUtilsExample(player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        MessageBuilder helpMessage = messageManager.builder("§6=== TempceLib Message System Examples ===")
                .newLine()
                .append("§e/tempce message welcome §7- ウェルカムメッセージの例")
                .newLine()
                .append("§e/tempce message interactive §7- インタラクティブメッセージの例")
                .newLine()
                .append("§e/tempce message progress §7- プログレスバーの例")
                .newLine()
                .append("§e/tempce message translation §7- 翻訳システムの例")
                .newLine()
                .append("§e/tempce message utils §7- ユーティリティ機能の例");

        player.spigot().sendMessage(helpMessage.build());
    }

    private void sendWelcomeMessage(Player player) {
        // READMEの例をそのまま実装
        TextComponent message = new TextComponent("§6Welcome to the server!");
        TextComponent helpText = new TextComponent("\n§eClick here for help");
        helpText.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/help"));
        helpText.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder("§7Get help and information").create()));

        message.addExtra(helpText);
        player.spigot().sendMessage(message);

        // TempceLib簡易API版
        player.sendMessage("§7--- TempceLib簡易API版 ---");
        MessageBuilder builder = messageManager.builder("§6Welcome to the server!")
                .newLine()
                .append("§eClick here for help")
                .click("/help")
                .hover("§7Get help and information");

        player.spigot().sendMessage(builder.build());
    }

    private void sendInteractiveMessage(Player player) {
        MessageBuilder interactive = messageManager.builder("§6インタラクティブメッセージの例:")
                .newLine()
                .append("§a[クリックしてコマンド実行]")
                .clickAndHover("/time set day", "§7時間を昼に設定")
                .newLine()
                .append("§b[クリックしてURL開く]")
                .clickUrl("https://www.minecraft.net")
                .hover("§7Minecraft公式サイトを開く")
                .newLine()
                .append("§e[クリックしてコマンド提案]")
                .suggest("/gamemode creative")
                .hover("§7クリエイティブモードに変更するコマンドを提案");

        player.spigot().sendMessage(interactive.build());
    }

    private void sendProgressMessage(Player player) {
        String healthBar = MessageUtils.createProgressBar((int) player.getHealth(), 20);
        String expBar = MessageUtils.createProgressBar(player.getLevel(), 100);

        MessageBuilder progress = messageManager.builder("§6プログレスバーの例:")
                .newLine()
                .append("§cHealth: " + healthBar + " §7(" + Math.round(player.getHealth()) + "/20)")
                .newLine()
                .append("§bLevel: " + expBar + " §7(" + player.getLevel() + "/100)");

        player.spigot().sendMessage(progress.build());
    }

    private void sendTranslationExample(Player player) {
        String language = translationManager.getPlayerLanguage(player);
        String welcome = translationManager.getTranslation(player, "welcome");
        String helpClick = translationManager.getTranslation(player, "help.click");
        String helpHover = translationManager.getTranslation(player, "help.hover");

        MessageBuilder translation = messageManager.builder("§6翻訳システムの例:")
                .newLine()
                .append("§7あなたの言語: §e" + language)
                .newLine()
                .append(welcome)
                .newLine()
                .append(helpClick)
                .click("/help")
                .hover(helpHover);

        player.spigot().sendMessage(translation.build());
    }

    private void sendUtilsExample(Player player) {
        String colorized = MessageUtils.colorize("&a色コード変換の例: &b青 &c赤 &e黄");
        String centered = MessageUtils.centerText("§6センタリングされたテキスト");
        String withPlaceholders = MessageUtils.replacePlayerPlaceholders(
                "§7プレイヤー: §e{player} §7位置: §b{world} ({x}, {y}, {z})", player);

        player.sendMessage("§6ユーティリティ機能の例:");
        player.sendMessage(colorized);
        player.sendMessage(centered);
        player.sendMessage(withPlaceholders);

        // 複数行ホバーの例
        MessageBuilder multiHover = messageManager.builder("§e[複数行ホバーの例]")
                .hover("§6=== プレイヤー情報 ===",
                        "§7名前: §e" + player.getName(),
                        "§7UUID: §b" + player.getUniqueId(),
                        "§7ワールド: §a" + player.getWorld().getName(),
                        "§7レベル: §d" + player.getLevel());

        player.spigot().sendMessage(multiHover.build());
    }
}
