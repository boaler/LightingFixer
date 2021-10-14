package me.boaler.lightingfixer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LightingFixer extends JavaPlugin {
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		new FixlightingCommand(this);
		
		Bukkit.getLogger().info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " loaded successfully.");
	}
	
	@Override
	public void onDisable() {
		Bukkit.getLogger().info(this.getDescription().getName() + " disabled.");
	}
}
