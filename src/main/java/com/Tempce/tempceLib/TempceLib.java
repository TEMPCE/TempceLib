package com.Tempce.tempceLib;

import org.bukkit.plugin.java.JavaPlugin;

public final class TempceLib extends JavaPlugin {

  private static TempceLib instance;

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    
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
}
