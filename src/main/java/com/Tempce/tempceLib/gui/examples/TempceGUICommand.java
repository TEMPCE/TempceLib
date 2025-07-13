package com.Tempce.tempceLib.gui.examples;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.manager.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUIシステムのテスト用コマンド
 */
@Command(name = "tempce-gui", description = "TempceLib GUI機能のテストコマンド", permission = "tempcelib.gui")
public class TempceGUICommand {
    
    @SubCommand(path = "help", description = "GUI機能のヘルプを表示")
    public void help(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "=== TempceLib GUI機能 ===");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui commands" + ChatColor.WHITE + " - コマンド自動GUI化を開く");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui item-selection" + ChatColor.WHITE + " - アイテム選択GUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui number-selection [min] [max] [default]" + ChatColor.WHITE + " - 数値選択GUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui confirmation" + ChatColor.WHITE + " - 確認ダイアログGUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui custom" + ChatColor.WHITE + " - カスタムメニューGUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui paginated" + ChatColor.WHITE + " - ページネーションGUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui debug <on|off>" + ChatColor.WHITE + " - デバッグモードの切り替え");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui number-test-large" + ChatColor.WHITE + " - 大きな範囲での数値選択テスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui number-test-negative" + ChatColor.WHITE + " - 負の数を含む数値選択テスト");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "例: /tempce-gui number-selection 0 100 50");
    }
    
    @SubCommand(path = "debug", description = "デバッグモードの切り替え")
    public void debug(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "使用法: /tempce-gui debug <on|off>");
            return;
        }
        
        boolean enable = args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true");
        GUIManager.getInstance().setDebugMode(enable);
        
        if (enable) {
            sender.sendMessage(ChatColor.GREEN + "GUIデバッグモードを有効にしました。");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "GUIデバッグモードを無効にしました。");
        }
    }
    
    @SubCommand(path = "commands", description = "コマンド自動GUI化を開く")
    public void commands(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        GUIManager.getInstance().openCommandAutoGUI(player);
        player.sendMessage(ChatColor.GREEN + "コマンドGUIを開きました！");
    }
    
    @SubCommand(path = "item-selection", description = "アイテム選択GUIのテスト")
    public void itemSelection(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        List<ItemStack> items = Arrays.asList(
                createItem(Material.DIAMOND, "ダイヤモンド"),
                createItem(Material.GOLD_INGOT, "金インゴット"),
                createItem(Material.IRON_INGOT, "鉄インゴット"),
                createItem(Material.EMERALD, "エメラルド"),
                createItem(Material.REDSTONE, "レッドストーン")
        );
        
        GUIManager.getInstance().createItemSelectionGUI(player, "アイテムを選択してください", items, (selectedItem) -> {
            player.sendMessage(ChatColor.GREEN + "選択されたアイテム: " + 
                    selectedItem.getItemMeta().getDisplayName());
        });
    }
    
    @SubCommand(path = "number-selection", description = "数値選択GUIのテスト")
    public void numberSelection(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        
        // 引数で範囲を指定できるようにする
        int min = -128, max = 128, defaultValue = 0;
        
        if (args.length >= 1) {
            try {
                min = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "最小値は数値で指定してください。");
                return;
            }
        }
        
        if (args.length >= 2) {
            try {
                max = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "最大値は数値で指定してください。");
                return;
            }
        }
        
        if (args.length >= 3) {
            try {
                defaultValue = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "デフォルト値は数値で指定してください。");
                return;
            }
        }
        
        if (min >= max) {
            player.sendMessage(ChatColor.RED + "最小値は最大値より小さくなければなりません。");
            return;
        }
        
        // デフォルト値を範囲内に収める
        defaultValue = Math.max(min, Math.min(max, defaultValue));
        
        player.sendMessage(ChatColor.GRAY + "数値選択GUI: 範囲=" + min + "~" + max + ", デフォルト=" + defaultValue);
        
        GUIManager.getInstance().createNumberSelectionGUI(player, "数値を選択してください", min, max, defaultValue, (selectedNumber) -> {
            player.sendMessage(ChatColor.GREEN + "選択された数値: " + selectedNumber);
        });
    }
    
    @SubCommand(path = "confirmation", description = "確認ダイアログGUIのテスト")
    public void confirmation(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        GUIManager.getInstance().createConfirmationGUI(player, "確認", "本当に実行しますか？", 
                () -> player.sendMessage(ChatColor.GREEN + "実行されました！"),
                () -> player.sendMessage(ChatColor.YELLOW + "キャンセルされました。"));
    }
    
    @SubCommand(path = "custom", description = "カスタムメニューGUIのテスト")
    public void custom(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        List<GUIItemData> items = new ArrayList<>();
        
        // 各種アクションボタン
        items.add(new GUIItemData(
                createItem(Material.EXPERIENCE_BOTTLE, "経験値をもらう"),
                0,
                (guiItemData) -> {
                    player.giveExp(100);
                    player.sendMessage(ChatColor.GREEN + "経験値を100もらいました！");
                }
        ));
        
        items.add(new GUIItemData(
                createItem(Material.GOLDEN_APPLE, "体力回復"),
                1,
                (guiItemData) -> {
                    player.setHealth(20.0);
                    player.sendMessage(ChatColor.GREEN + "体力が回復しました！");
                }
        ));
        
        items.add(new GUIItemData(
                createItem(Material.COOKED_BEEF, "満腹度回復"),
                2,
                (guiItemData) -> {
                    player.setFoodLevel(20);
                    player.sendMessage(ChatColor.GREEN + "満腹度が回復しました！");
                }
        ));
        
        items.add(new GUIItemData(
                createItem(Material.BARRIER, "GUIを閉じる"),
                8,
                (guiItemData) -> GUIManager.getInstance().closeGUI(player)
        ));
        
        GUIMenuData menuData = new GUIMenuData("カスタムメニュー", 9, items);
        GUIManager.getInstance().createCustomMenuGUI(player, menuData);
    }
    
    @SubCommand(path = "paginated", description = "ページネーションGUIのテスト")
    public void paginated(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        List<GUIItemData> items = new ArrayList<>();
        
        // 大量のアイテムを作成
        for (int i = 1; i <= 100; i++) {
            final int number = i;
            items.add(new GUIItemData(
                    createItem(Material.PAPER, "アイテム " + i),
                    0, // ページネーションでは自動的に配置される
                    (guiItemData) -> player.sendMessage(ChatColor.GREEN + "アイテム " + number + " がクリックされました！")
            ));
        }
        
        GUIManager.getInstance().createPaginatedGUI(player, "ページネーションテスト", items, 28, null);
    }
    
    @SubCommand(path = "player-selection", description = "プレイヤー選択GUIのテスト")
    public void playerSelection(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }

        Player player = (Player) sender;
        GUIManager.getInstance().createPlayerSelectionGUI(player, "プレイヤーを選択してください", (selectedPlayer) -> {
            player.sendMessage(ChatColor.GREEN + "選択されたプレイヤー: " + selectedPlayer.getName());
        });
    }

    @SubCommand(path = "player-selection-permission", description = "権限フィルタ付きプレイヤー選択GUIのテスト")
    public void playerSelectionPermission(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }

        Player player = (Player) sender;
        String permission = "tempcelib.gui"; // テスト用権限
        GUIManager.getInstance().createPlayerSelectionGUI(player, "権限を持つプレイヤーを選択", permission, (selectedPlayer) -> {
            player.sendMessage(ChatColor.GREEN + "選択されたプレイヤー: " + selectedPlayer.getName());
            player.sendMessage(ChatColor.GRAY + "権限: " + permission);
        });
    }

    @SubCommand(path = "all-player-selection", description = "全プレイヤー選択GUIのテスト")
    public void allPlayerSelection(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }

        Player player = (Player) sender;
        boolean includeOffline = args.length > 0 && args[0].equalsIgnoreCase("offline");
        GUIManager.getInstance().createAllPlayerSelectionGUI(player, "全プレイヤーから選択", includeOffline, (selectedPlayer) -> {
            player.sendMessage(ChatColor.GREEN + "選択されたプレイヤー: " + selectedPlayer.getName());
        });
    }

    @SubCommand(path = "player-name-selection", description = "プレイヤー名選択GUIのテスト（オフライン対応）")
    public void playerNameSelection(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }

        Player player = (Player) sender;
        boolean includeOffline = args.length > 0 && args[0].equalsIgnoreCase("offline");
        GUIManager.getInstance().createPlayerNameSelectionGUI(player, 
            includeOffline ? "全プレイヤー名から選択" : "オンラインプレイヤー名から選択", 
            includeOffline, (selectedPlayerName) -> {
                player.sendMessage(ChatColor.GREEN + "選択されたプレイヤー名: " + selectedPlayerName);
                
                // オンラインかどうかをチェック
                Player onlinePlayer = Bukkit.getPlayer(selectedPlayerName);
                if (onlinePlayer != null) {
                    player.sendMessage(ChatColor.GREEN + "  → 現在オンラインです");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "  → 現在オフラインです");
                }
        });
    }

    @SubCommand(path = "number-test-large", description = "大きな範囲での数値選択GUIのテスト")
    public void numberTestLarge(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        GUIManager.getInstance().createNumberSelectionGUI(player, "大きな数値を選択", 0, 10000, 1000, (selectedNumber) -> {
            player.sendMessage(ChatColor.GREEN + "選択された大きな数値: " + selectedNumber);
        });
    }
    
    @SubCommand(path = "number-test-negative", description = "負の数を含む数値選択GUIのテスト")
    public void numberTestNegative(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        GUIManager.getInstance().createNumberSelectionGUI(player, "負の数を含む範囲", -1000, 1000, 0, (selectedNumber) -> {
            player.sendMessage(ChatColor.GREEN + "選択された数値: " + selectedNumber);
        });
    }

    /**
     * アイテムを作成するヘルパーメソッド
     */
    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
