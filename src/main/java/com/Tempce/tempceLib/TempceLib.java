package com.Tempce.tempceLib;

import org.bukkit.plugin.java.JavaPlugin;

public final class TempceLib extends JavaPlugin {

  private static TempceLib instance;

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    
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
}
