package com.Tempce.tempceLib;

import com.Tempce.tempceLib.api.DatabaseAPI;
import com.Tempce.tempceLib.command.examples.TempceLibCommand;
import com.Tempce.tempceLib.command.manager.CommandManager;
import com.Tempce.tempceLib.database.manager.DatabaseManager;
import com.Tempce.tempceLib.gui.manager.GUIManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TempceLib extends JavaPlugin {

  private static TempceLib instance;
  private CommandManager commandManager;
  private GUIManager guiManager;
  private DatabaseManager databaseManager;

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    
    // データベースシステムの初期化
    initializeDatabaseSystem();
    
    // コマンドシステムの初期化
    initializeCommandSystem();
    
    // GUIシステムの初期化
    initializeGUISystem();
    
    getLogger().info("TempceLibが有効化されました！");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    
    // データベースシステムのシャットダウン
    if (databaseManager != null) {
      databaseManager.shutdown();
    }
    
    getLogger().info("TempceLibが無効化されました！");
  }

  public static TempceLib getInstance() {
    return instance;
  }
  
  /**
   * データベースシステムを初期化する
   */
  private void initializeDatabaseSystem() {
    try {
      databaseManager = new DatabaseManager(this);
      databaseManager.initialize();
      
      // DatabaseAPIを初期化
      DatabaseAPI.initialize(this);
      
      getLogger().info("データベースシステムを初期化しました (" + 
          databaseManager.getConfig().getType().getName() + ")");
    } catch (Exception e) {
      getLogger().severe("データベースシステムの初期化に失敗しました: " + e.getMessage());
      e.printStackTrace();
    }
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
  
  /**
   * データベースマネージャーを取得する
   * @return データベースマネージャー
   */
  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }
}
