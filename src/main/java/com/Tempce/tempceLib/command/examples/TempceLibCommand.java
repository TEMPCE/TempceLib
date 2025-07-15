package com.Tempce.tempceLib.command.examples;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.CommandArguments;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.command.data.ArgumentType;
import com.Tempce.tempceLib.command.data.SubCommandData;
import com.Tempce.tempceLib.command.helper.ArgumentValidator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * TempceLibのメインコマンドのサンプル実装
 */
@Command(
    name = "tempcelib",
    permission = "tempcelib.use",
    alias = {"tcl"},
    description = "TempceLibのメインコマンド",
    usage = "/tempcelib <subcommand>"
)
public class TempceLibCommand {
    
    /**
     * 現在のサブコマンドデータを取得するヘルパーメソッド
     */
    private SubCommandData getCurrentSubCommandData(String subCommandPath) {
        return TempceLib.getInstance().getCommandManager()
                .getCommands().get("tempcelib")
                .getSubCommands().get(subCommandPath);
    }
    
    /**
     * 引数を自動バリデーションするヘルパーメソッド
     */
    private boolean validateArguments(CommandSender sender, String[] args, String subCommandPath) {
        SubCommandData subCommandData = getCurrentSubCommandData(subCommandPath);
        if (subCommandData != null) {
            return ArgumentValidator.validateAndSendErrors(sender, args, subCommandData);
        }
        return true;
    }
    
    @SubCommand(
        path = "info",
        description = "TempceLibの情報を表示",
        usage = "info"
    )
    public void info(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "========== TempceLib Information ==========");
        sender.sendMessage(ChatColor.YELLOW + "バージョン: " + ChatColor.WHITE + "1.0.0");
        sender.sendMessage(ChatColor.YELLOW + "作者: " + ChatColor.WHITE + "Tempce");
        sender.sendMessage(ChatColor.YELLOW + "説明: " + ChatColor.WHITE + "Minecraftプラグイン開発支援ライブラリ");
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    @SubCommand(
        path = "reload",
        permission = "tempcelib.admin",
        description = "設定をリロード",
        usage = "reload"
    )
    public void reload(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "設定をリロードしました！");
    }
    
    @SubCommand(
        path = "stats",
        description = "コマンド統計を表示",
        usage = "stats"
    )
    public void stats(CommandSender sender, String[] args) {
        com.Tempce.tempceLib.TempceLib plugin = com.Tempce.tempceLib.TempceLib.getInstance();
        com.Tempce.tempceLib.command.manager.CommandManager manager = plugin.getCommandManager();
        
        sender.sendMessage(ChatColor.GOLD + "========== コマンド統計 ==========");
        sender.sendMessage(ChatColor.YELLOW + "登録コマンド数: " + ChatColor.WHITE + manager.getCommandCount());
        sender.sendMessage(ChatColor.YELLOW + "登録サブコマンド数: " + ChatColor.WHITE + manager.getSubCommandCount());
        
        sender.sendMessage(ChatColor.YELLOW + "登録コマンド一覧:");
        for (com.Tempce.tempceLib.command.data.CommandData cmdData : manager.getCommands().values()) {
            if (cmdData.getPermission().isEmpty() || sender.hasPermission(cmdData.getPermission())) {
                sender.sendMessage(ChatColor.AQUA + "  - " + cmdData.getName() + 
                    " (サブコマンド: " + cmdData.getSubCommands().size() + "個)");
            }
        }
        sender.sendMessage(ChatColor.GOLD + "===============================");
    }
    
    @SubCommand(
        path = "give",
        permission = "tempcelib.admin",
        description = "プレイヤーにアイテムを与える",
        usage = "give <player> <item> [amount]",
        playerOnly = true
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "player",
            type = ArgumentType.PLAYER,
            description = "アイテムを与えるプレイヤー",
            required = true
        ),
        @CommandArguments.Argument(
            name = "item",
            type = ArgumentType.ITEM_ID,
            description = "与えるアイテムのID",
            required = true
        ),
        @CommandArguments.Argument(
            name = "amount",
            type = ArgumentType.INTEGER,
            description = "アイテムの数量",
            required = false,
            defaultValue = "1",
            min = 1,
            max = 64
        )
    })
    public void give(CommandSender sender, String[] args) {
        // 自動バリデーション
        if (!validateArguments(sender, args, "give")) {
            return;
        }
        
        String playerName = args[0];
        String itemId = args[1];
        int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        
        sender.sendMessage(ChatColor.GREEN + playerName + " に " + itemId + " を " + amount + "個与えました！");
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
    
    @SubCommand(
        path = "teleport",
        permission = "tempcelib.admin",
        description = "プレイヤーを指定座標にテレポート",
        usage = "teleport <player> <x> <y> <z> [world]",
        playerOnly = true
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "player",
            type = ArgumentType.PLAYER,
            description = "テレポートするプレイヤー",
            required = true
        ),
        @CommandArguments.Argument(
            name = "x",
            type = ArgumentType.COORDINATE_X,
            description = "X座標",
            required = true,
            min = -30000000,
            max = 30000000
        ),
        @CommandArguments.Argument(
            name = "y",
            type = ArgumentType.COORDINATE_Y,
            description = "Y座標",
            required = true,
            min = -64,
            max = 320
        ),
        @CommandArguments.Argument(
            name = "z",
            type = ArgumentType.COORDINATE_Z,
            description = "Z座標",
            required = true,
            min = -30000000,
            max = 30000000
        ),
        @CommandArguments.Argument(
            name = "world",
            type = ArgumentType.WORLD,
            description = "ワールド名",
            required = false,
            defaultValue = "world"
        )
    })
    public void teleport(CommandSender sender, String[] args) {
        // 自動バリデーション
        if (!validateArguments(sender, args, "teleport")) {
            return;
        }
        
        String playerName = args[0];
        double x = Double.parseDouble(args[1]);
        double y = Double.parseDouble(args[2]);
        double z = Double.parseDouble(args[3]);
        String world = args.length > 4 ? args[4] : "world";
        
        sender.sendMessage(ChatColor.GREEN + playerName + " を座標 (" + x + ", " + y + ", " + z + ") にテレポートしました！");
        sender.sendMessage(ChatColor.GRAY + "ワールド: " + world);
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
    
    @SubCommand(
        path = "enchant",
        permission = "tempcelib.admin",
        description = "アイテムにエンチャントを付与",
        usage = "enchant <player> <enchantment> [level]",
        playerOnly = true
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "player",
            type = ArgumentType.PLAYER,
            description = "エンチャントを付与するプレイヤー",
            required = true
        ),
        @CommandArguments.Argument(
            name = "enchantment",
            type = ArgumentType.ENCHANTMENT,
            description = "付与するエンチャント",
            required = true
        ),
        @CommandArguments.Argument(
            name = "level",
            type = ArgumentType.INTEGER,
            description = "エンチャントレベル",
            required = false,
            defaultValue = "1",
            min = 1,
            max = 10
        )
    })
    public void enchant(CommandSender sender, String[] args) {
        // 自動バリデーション
        if (!validateArguments(sender, args, "enchant")) {
            return;
        }
        
        String playerName = args[0];
        String enchantment = args[1];
        int level = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        
        sender.sendMessage(ChatColor.GREEN + playerName + " に " + enchantment + " レベル " + level + " を付与しました！");
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
    
    @SubCommand(
        path = "give-tool",
        description = "プレイヤーにツールを与える",
        usage = "give-tool <player> <tool>"
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "player",
            type = ArgumentType.PLAYER,
            description = "対象プレイヤー",
            required = true
        ),
        @CommandArguments.Argument(
            name = "tool",
            type = ArgumentType.ITEM_ID_TOOL,
            description = "与えるツール",
            required = true
        )
    })
    public void giveTool(CommandSender sender, String[] args) {
        if (!validateArguments(sender, args, "give-tool")) {
            return;
        }
        
        String playerName = args[0];
        String tool = args[1];
        
        sender.sendMessage(ChatColor.GREEN + playerName + " にツール " + tool + " を与えました！");
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
    
    @SubCommand(
        path = "give-food",
        description = "プレイヤーに食べ物を与える",
        usage = "give-food <player> <food> [amount]"
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "player",
            type = ArgumentType.PLAYER,
            description = "対象プレイヤー",
            required = true
        ),
        @CommandArguments.Argument(
            name = "food",
            type = ArgumentType.ITEM_ID_FOOD,
            description = "与える食べ物",
            required = true
        ),
        @CommandArguments.Argument(
            name = "amount",
            type = ArgumentType.INTEGER,
            description = "個数",
            required = false,
            defaultValue = "1",
            min = 1,
            max = 64
        )
    })
    public void giveFood(CommandSender sender, String[] args) {
        if (!validateArguments(sender, args, "give-food")) {
            return;
        }
        
        String playerName = args[0];
        String food = args[1];
        int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        
        sender.sendMessage(ChatColor.GREEN + playerName + " に食べ物 " + food + " を " + amount + "個与えました！");
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
    
    @SubCommand(
        path = "place-block",
        description = "指定した座標にブロックを配置する",
        usage = "place-block <x> <y> <z> <block>"
    )
    @CommandArguments({
        @CommandArguments.Argument(
            name = "x",
            type = ArgumentType.COORDINATE_X,
            description = "X座標",
            required = true
        ),
        @CommandArguments.Argument(
            name = "y",
            type = ArgumentType.COORDINATE_Y,
            description = "Y座標",
            required = true
        ),
        @CommandArguments.Argument(
            name = "z",
            type = ArgumentType.COORDINATE_Z,
            description = "Z座標",
            required = true
        ),
        @CommandArguments.Argument(
            name = "block",
            type = ArgumentType.ITEM_ID_BLOCK,
            description = "配置するブロック",
            required = true
        )
    })
    public void placeBlock(CommandSender sender, String[] args) {
        if (!validateArguments(sender, args, "place-block")) {
            return;
        }
        
        String x = args[0];
        String y = args[1];
        String z = args[2];
        String block = args[3];
        
        sender.sendMessage(ChatColor.GREEN + "座標 (" + x + ", " + y + ", " + z + ") にブロック " + block + " を配置しました！");
        sender.sendMessage(ChatColor.GRAY + "(これはサンプル実装です)");
    }
}
