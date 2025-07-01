package com.Tempce.tempceLib.gui.examples;

import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.gui.data.GUIItemData;
import com.Tempce.tempceLib.gui.data.GUIMenuData;
import com.Tempce.tempceLib.gui.manager.GUIManager;
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
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui number-selection" + ChatColor.WHITE + " - 数値選択GUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui confirmation" + ChatColor.WHITE + " - 確認ダイアログGUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui custom" + ChatColor.WHITE + " - カスタムメニューGUIのテスト");
        sender.sendMessage(ChatColor.YELLOW + "/tempce-gui paginated" + ChatColor.WHITE + " - ページネーションGUIのテスト");
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
        GUIManager.getInstance().createNumberSelectionGUI(player, "数値を選択してください", 1, 10, 5, (selectedNumber) -> {
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
