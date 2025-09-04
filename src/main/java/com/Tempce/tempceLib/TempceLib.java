package com.Tempce.tempceLib;

import com.Tempce.tempceLib.command.examples.TempceLibCommand;
import com.Tempce.tempceLib.command.manager.CommandManager;
import com.Tempce.tempceLib.gui.manager.GUIManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TempceLib extends JavaPlugin {

  private static TempceLib instance;
  private CommandManager commandManager;
  private GUIManager guiManager;

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    
    // コマンドシステムの初期化
    initializeCommandSystem();
    
    // GUIシステムの初期化
    initializeGUISystem();
    
    getLogger().info("TempceLibが有効化されました！");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("TempceLibが無効化されました！");
  }

  public static TempceLib getInstance() {
    return instance;
  }
  
  /**
   * コマンドシステムを初期化する
   */
  private void initializeCommandSystem() {
    commandManager = new CommandManager();
    
    // plugin.ymlで定義されたコマンドにエグゼキューターとタブコンプリーターを設定
    commandManager.registerCommand(TempceLibCommand.class);
    
    getLogger().info("コマンドシステムを初期化しました (登録コマンド: " + 
        commandManager.getCommandCount() + "個, サブコマンド: " + 
        commandManager.getSubCommandCount() + "個)");
  }
  
  /**
   * GUIシステムを初期化する
   */
  private void initializeGUISystem() {
    guiManager = GUIManager.getInstance();
    guiManager.initialize();
    
    getLogger().info("GUIシステムを初期化しました");
  }
  
  /**
   * コマンドマネージャーを取得する
   * @return コマンドマネージャー
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }
  
  /**
   * GUIマネージャーを取得する
   * @return GUIマネージャー
   */
  public GUIManager getGuiManager() {
    return guiManager;
  }
}
