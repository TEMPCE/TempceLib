package com.Tempce.tempceLib.gui.manager.handlers;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.data.SubCommandData;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.manager.util.GUIItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * コマンドGUI機能を管理するクラス
 */
public class CommandGUIManager {
    
    private final ArgumentInputGUIManager argumentInputManager;
    private final ArgumentInputChatListener chatListener;
    
    public CommandGUIManager() {
        this.chatListener = new ArgumentInputChatListener();
        this.argumentInputManager = new ArgumentInputGUIManager(this.chatListener);
    }
    
    public ArgumentInputChatListener getChatListener() {
        return chatListener;
    }
    
    /**
     * プレイヤーの権限をチェック
     * @param player プレイヤー
     * @param permission 権限
     * @return 権限を持っているか
     */
    private boolean hasPermission(Player player, String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        return !player.hasPermission(permission);
    }
    
    /**
     * コマンド自動GUIを開く
     * @param player プレイヤー
     * @param paginationCreator ページネーション作成関数
     */
    public void openCommandAutoGUI(Player player, Consumer<CommandGUIData> paginationCreator) {
        Map<String, CommandData> commands = TempceLib.getInstance().getCommandManager().getCommands();
        List<GUIItemData> guiItems = new ArrayList<>();
        
        int slot = 0;
        for (Map.Entry<String, CommandData> entry : commands.entrySet()) {
            CommandData commandData = entry.getValue();
            
            // 権限チェック
            if (hasPermission(player, commandData.getPermission())) {
                continue;
            }
            
            ItemStack commandItem = GUIItemCreator.createItem(Material.COMMAND_BLOCK, 
                    ChatColor.GOLD + "/" + commandData.getName(),
                    Arrays.asList(
                            ChatColor.GRAY + "説明: " + ChatColor.WHITE + commandData.getDescription(),
                            ChatColor.GRAY + "権限: " + ChatColor.WHITE + 
                                    (commandData.getPermission().isEmpty() ? "なし" : commandData.getPermission()),
                            "",
                            ChatColor.YELLOW + "クリックしてサブコマンドを表示"
                    ));
            
            guiItems.add(new GUIItemData(commandItem, slot++, (guiItemData) -> 
                    openSubCommandGUI(player, commandData.getName(), paginationCreator)));
            
            if (slot >= 54) break; // インベントリサイズ制限
        }
        
        if (guiItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "利用可能なコマンドがありません。");
            return;
        }
        
        paginationCreator.accept(new CommandGUIData(
            ChatColor.DARK_GREEN + "コマンド一覧", guiItems, 45, null, CommandGUIType.COMMAND_LIST));
    }
    
    /**
     * サブコマンドGUIを開く
     * @param player プレイヤー
     * @param commandName コマンド名
     * @param paginationCreator ページネーション作成関数
     */
    public void openSubCommandGUI(Player player, String commandName, Consumer<CommandGUIData> paginationCreator) {
        CommandData commandData = TempceLib.getInstance().getCommandManager().getCommands().get(commandName);
        if (commandData == null) {
            player.sendMessage(ChatColor.RED + "コマンドが見つかりません: " + commandName);
            return;
        }
        
        List<GUIItemData> guiItems = new ArrayList<>();
        
        // メインコマンド実行ボタン
        ItemStack mainCommandItem = GUIItemCreator.createItem(Material.EMERALD, 
                ChatColor.GREEN + "/" + commandName,
                Arrays.asList(
                        ChatColor.GRAY + "説明: " + ChatColor.WHITE + commandData.getDescription(),
                        "",
                        ChatColor.YELLOW + "クリックして実行"
                ));
        guiItems.add(new GUIItemData(mainCommandItem, 0, (guiItemData) -> {
            player.closeInventory();
            player.performCommand(commandName);
        }));
        
        // サブコマンドボタン
        int slot = 9; // 2行目から開始
        for (SubCommandData subCommandData : commandData.getSubCommands().values()) {
            // 権限チェック
            if (hasPermission(player, subCommandData.getPermission())) {
                continue;
            }
            
            // 引数情報を説明に追加
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "説明: " + ChatColor.WHITE + subCommandData.getDescription());
            lore.add(ChatColor.GRAY + "権限: " + ChatColor.WHITE + 
                    (subCommandData.getPermission().isEmpty() ? "なし" : subCommandData.getPermission()));
            
            if (subCommandData.hasArguments()) {
                lore.add("");
                lore.add(ChatColor.GOLD + "引数:");
                for (int i = 0; i < subCommandData.getArguments().size(); i++) {
                    var arg = subCommandData.getArguments().get(i);
                    String prefix = arg.isRequired() ? ChatColor.RED + "必須" : ChatColor.GREEN + "任意";
                    lore.add(ChatColor.GRAY + " " + (i + 1) + ". " + prefix + ChatColor.WHITE + " " + 
                            arg.getName() + " (" + arg.getType().getDisplayName() + ")");
                }
                lore.add("");
                lore.add(ChatColor.YELLOW + "クリックして引数を入力");
            } else {
                lore.add("");
                lore.add(ChatColor.YELLOW + "クリックして実行");
            }
            
            ItemStack subCommandItem = GUIItemCreator.createItem(Material.PAPER, 
                    ChatColor.AQUA + "/" + commandName + " " + String.join(" ", subCommandData.getPathLevels()),
                    lore);
            
            final String subCommandPath = String.join(".", subCommandData.getPathLevels());
            guiItems.add(new GUIItemData(subCommandItem, slot++, (guiItemData) -> {
                // 引数があるかチェックして適切な処理を行う
                if (subCommandData.hasArguments()) {
                    // 引数入力GUIを開始
                    argumentInputManager.startArgumentInputFlow(
                        player, 
                        commandName, 
                        subCommandPath, 
                        subCommandData.getArguments(), 
                        paginationCreator
                    );
                } else {
                    // 引数がない場合は直接実行
                    final String fullCommand = commandName + " " + String.join(" ", subCommandData.getPathLevels());
                    player.closeInventory();
                    player.performCommand(fullCommand);
                }
            }));
            
            if (slot >= 54) break;
        }
        
        // 戻るボタン
        ItemStack backItem = GUIItemCreator.createItem(Material.ARROW, ChatColor.YELLOW + "戻る",
                List.of(ChatColor.GRAY + "コマンド一覧に戻る"));
        guiItems.add(new GUIItemData(backItem, 53, (guiItemData) -> {
            // コマンド一覧に戻る際はページネーションGUIを再表示
            openCommandAutoGUI(player, paginationCreator);
        }));
        
        int size = Math.min(54, ((slot - 1) / 9 + 1) * 9);
        
        paginationCreator.accept(new CommandGUIData(
            ChatColor.DARK_BLUE + "コマンド: " + commandName, guiItems, size, null, CommandGUIType.SUBCOMMAND_MENU));
    }

    /**
     * コマンドGUI用のデータクラス
     */
        public record CommandGUIData(String title, List<GUIItemData> items, int size, Consumer<GUIItemData> onItemClick,
                                     CommandGUIType type) {
    }
    
    /**
     * コマンドGUIのタイプ
     */
    public enum CommandGUIType {
        COMMAND_LIST,
        SUBCOMMAND_MENU
    }
}
