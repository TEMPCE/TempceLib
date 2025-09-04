package com.Tempce.tempceLib.command.examples;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.api.DatabaseAPI;
import com.Tempce.tempceLib.command.annotations.Command;
import com.Tempce.tempceLib.command.annotations.SubCommand;
import com.Tempce.tempceLib.gui.manager.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
        path = "gui",
        description = "コマンド自動GUI化メニューを開く",
        usage = "gui",
        playerOnly = true
    )
    public void gui(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return;
        }
        
        Player player = (Player) sender;
        GUIManager.getInstance().openCommandAutoGUI(player);
        player.sendMessage(ChatColor.GREEN + "コマンド自動GUI化メニューを開きました！");
    }
    
    @SubCommand(
        path = "database info",
        permission = "tempcelib.admin",
        description = "データベース情報を表示",
        usage = "database info"
    )
    public void databaseInfo(CommandSender sender, String[] args) {
        TempceLib plugin = TempceLib.getInstance();
        
        if (!plugin.getDatabaseManager().isInitialized()) {
            sender.sendMessage(ChatColor.RED + "データベースが初期化されていません。");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "========== データベース情報 ==========");
        
        DatabaseAPI.getDatabaseStats().thenAccept(stats -> {
            sender.sendMessage(ChatColor.YELLOW + "データベース種別: " + ChatColor.WHITE + stats.get("database_type"));
            sender.sendMessage(ChatColor.YELLOW + "テーブル数: " + ChatColor.WHITE + stats.get("table_count"));
            sender.sendMessage(ChatColor.YELLOW + "接続プール状態: " + ChatColor.WHITE + stats.get("connection_pool_stats"));
            sender.sendMessage(ChatColor.GREEN + "======================================");
        }).exceptionally(throwable -> {
            sender.sendMessage(ChatColor.RED + "データベース情報の取得に失敗しました: " + throwable.getMessage());
            return null;
        });
    }
    
    @SubCommand(
        path = "database test",
        permission = "tempcelib.admin",
        description = "データベース接続をテスト",
        usage = "database test"
    )
    public void databaseTest(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "データベース接続をテストしています...");
        
        // テストテーブルの作成
        Map<String, String> columns = new HashMap<>();
        columns.put("test_column", "VARCHAR(255)");
        columns.put("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        
        DatabaseAPI.createTable("test_table", columns).thenAccept(success -> {
            if (success) {
                sender.sendMessage(ChatColor.GREEN + "✓ テストテーブルの作成に成功しました");
                
                // テストデータの挿入
                DatabaseAPI.executeUpdateAsync(
                    "INSERT INTO tempce_test_table (test_column) VALUES (?)",
                    "Test Data"
                ).thenAccept(result -> {
                    sender.sendMessage(ChatColor.GREEN + "✓ テストデータの挿入に成功しました (影響行数: " + result + ")");
                    
                    // データの読み取りテスト
                    DatabaseAPI.executeQueryAsync(
                        "SELECT * FROM tempce_test_table WHERE test_column = ?",
                        "Test Data"
                    ).thenAccept(queryResult -> {
                        sender.sendMessage(ChatColor.GREEN + "✓ データの読み取りに成功しました (結果行数: " + 
                                queryResult.getRowCount() + ", 実行時間: " + queryResult.getExecutionTime() + "ms)");
                        sender.sendMessage(ChatColor.GREEN + "データベース接続テストが完了しました！");
                    }).exceptionally(throwable -> {
                        sender.sendMessage(ChatColor.RED + "✗ データの読み取りに失敗しました: " + throwable.getMessage());
                        return null;
                    });
                }).exceptionally(throwable -> {
                    sender.sendMessage(ChatColor.RED + "✗ テストデータの挿入に失敗しました: " + throwable.getMessage());
                    return null;
                });
            } else {
                sender.sendMessage(ChatColor.RED + "✗ テストテーブルの作成に失敗しました");
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(ChatColor.RED + "✗ データベーステストに失敗しました: " + throwable.getMessage());
            return null;
        });
    }
    
    @SubCommand(
        path = "database optimize",
        permission = "tempcelib.admin",
        description = "データベースを最適化",
        usage = "database optimize"
    )
    public void databaseOptimize(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "データベースの最適化を開始しています...");
        
        DatabaseAPI.optimizeDatabase().thenAccept(success -> {
            if (success) {
                sender.sendMessage(ChatColor.GREEN + "✓ データベースの最適化が完了しました");
            } else {
                sender.sendMessage(ChatColor.RED + "✗ データベースの最適化に失敗しました");
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(ChatColor.RED + "✗ データベースの最適化でエラーが発生しました: " + throwable.getMessage());
            return null;
        });
    }
    
    @SubCommand(
        path = "database cleanup",
        permission = "tempcelib.admin",
        description = "指定したテーブルの重複データをクリーンアップ",
        usage = "database cleanup <table_name> <key_column1> [key_column2...]"
    )
    public void databaseCleanup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "使用法: /tempcelib database cleanup <table_name> <key_column1> [key_column2...]");
            return;
        }
        
        String tableName = args[0];
        String[] keyColumns = new String[args.length - 1];
        System.arraycopy(args, 1, keyColumns, 0, keyColumns.length);
        
        sender.sendMessage(ChatColor.YELLOW + "テーブル '" + tableName + "' の重複データをクリーンアップしています...");
        
        DatabaseAPI.cleanupDuplicates(tableName, keyColumns).thenAccept(deletedRows -> {
            if (deletedRows > 0) {
                sender.sendMessage(ChatColor.GREEN + "✓ " + deletedRows + "行の重複データを削除しました");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "重複データは見つかりませんでした");
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(ChatColor.RED + "✗ 重複データのクリーンアップに失敗しました: " + throwable.getMessage());
            return null;
        });
    }
}
