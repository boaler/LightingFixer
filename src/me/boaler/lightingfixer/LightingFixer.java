package me.boaler.lightingfixer;

import org.bukkit.plugin.java.JavaPlugin;

public class LightingFixer extends JavaPlugin {
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		new FixlightingCommand(this);
		
		System.out.println(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " loaded successfully.");
	}
	
	@Override
	public void onDisable() {
		System.out.println(this.getDescription().getName() + " disabled.");
	}
}
