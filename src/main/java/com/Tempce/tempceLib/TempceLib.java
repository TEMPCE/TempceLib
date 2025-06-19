package com.Tempce.tempceLib;

import com.Tempce.tempceLib.message.MessageManager;
import com.Tempce.tempceLib.message.TranslationManager;
import com.Tempce.tempceLib.message.examples.MessageExampleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TempceLib extends JavaPlugin {

  private static TempceLib instance;
  private MessageManager messageManager;
  private TranslationManager translationManager;

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    
    // Initialize managers
    this.messageManager = new MessageManager();
    this.translationManager = new TranslationManager();
    
    // Register example command
    getCommand("tempce").setExecutor(new MessageExampleCommand(this));
    
    getLogger().info("TempceLib has been enabled!");
    getLogger().info("Message system initialized with translation support");
    getLogger().info("Use /tempce message to test message system features");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("TempceLib has been disabled!");
  }

  public static TempceLib getInstance() {
    return instance;
  }
  
  /**
   * MessageManagerのインスタンスを取得
   * @return MessageManager
   */
  public MessageManager getMessageManager() {
    return messageManager;
  }
  
  /**
   * TranslationManagerのインスタンスを取得
   * @return TranslationManager
   */
  public TranslationManager getTranslationManager() {
    return translationManager;
  }
}
