package com.Tempce.tempceLib.api;

import com.Tempce.tempceLib.TempceLib;
import com.Tempce.tempceLib.command.data.CommandData;
import com.Tempce.tempceLib.command.manager.CommandManager;

import java.util.Map;

/**
 * TempceLibのコマンドシステムAPI
 * 他のプラグインがこのAPIを使用してコマンドを登録できます
 */
public class CommandAPI {
    
    /**
     * コマンドクラスを登録する
     * @param commandClass 登録するコマンドクラス
     * @throws IllegalArgumentException コマンドクラスが無効な場合
     * @throws RuntimeException インスタンス化に失敗した場合
     */
    public static void registerCommand(Class<?> commandClass) {
        getCommandManager().registerCommand(commandClass);
    }
    
    /**
     * コマンドインスタンスを登録する
     * @param commandInstance 登録するコマンドインスタンス
     * @throws IllegalArgumentException コマンドインスタンスが無効な場合
     */
    public static void registerCommand(Object commandInstance) {
        getCommandManager().registerCommand(commandInstance);
    }
    
    /**
     * 登録されているコマンドを取得する
     * @param name コマンド名またはエイリアス
     * @return コマンドデータ（見つからない場合はnull）
     */
    public static CommandData getCommand(String name) {
        return getCommandManager().getCommand(name);
    }
    
    /**
     * 登録されているすべてのコマンドを取得する
     * @return コマンドマップ
     */
    public static Map<String, CommandData> getCommands() {
        return getCommandManager().getCommands();
    }
    
    /**
     * 登録されているコマンド数を取得する
     * @return コマンド数
     */
    public static int getCommandCount() {
        return getCommandManager().getCommandCount();
    }
    
    /**
     * 登録されているサブコマンド数を取得する
     * @return サブコマンド数
     */
    public static int getSubCommandCount() {
        return getCommandManager().getSubCommandCount();
    }
    
    /**
     * コマンドマネージャーを取得する（内部用）
     * @return コマンドマネージャー
     */
    private static CommandManager getCommandManager() {
        TempceLib plugin = TempceLib.getInstance();
        if (plugin == null) {
            throw new IllegalStateException("TempceLibが初期化されていません");
        }
        
        CommandManager manager = plugin.getCommandManager();
        if (manager == null) {
            throw new IllegalStateException("コマンドマネージャーが初期化されていません");
        }
        
        return manager;
    }
}
