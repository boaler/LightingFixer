package me.boaler.lightingfixer;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FixlightingCommand implements CommandExecutor {
	private LightingFixer plugin;
	private ArrayList<Material> materials;
	private int maxheight;
	
	public FixlightingCommand(LightingFixer plugin) {
		this.plugin = plugin;
		materials = new ArrayList<Material>();
		
		this.plugin.getCommand("fixlighting").setExecutor(this);
		
		loadConfig();
	}
	
	public void loadConfig() {
		ArrayList<String> stringMaterials = new ArrayList<String>();
		stringMaterials.addAll(plugin.getConfig().getStringList("materials"));
		for(String string : stringMaterials) {
			Material temp = Material.getMaterial(string);
			materials.add(temp);
		}
		Bukkit.getLogger().info("[LightingFixer] Materials to change:" + materials);
		stringMaterials = null;
		
		maxheight = plugin.getConfig().getInt("maxheight");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cmdName = cmd.getName();
		
		if(cmdName.equalsIgnoreCase("fixlighting")) {
			if(!(sender.hasPermission("fixlighting"))) {
				sender.sendMessage("§cYou don't have permissions to do that. ");
				return true;
			}
			if(args.length == 0 || args.length > 3) {
				sender.sendMessage("§eLightingFixer Commands:\n"
						+ "/fixlighting <minY> <maxY> [world]\n"
						+ "/fixlighting reload");
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("reload")) {
					return handleReloadCommand(sender);
				} else {
					sender.sendMessage("§eLightingFixer Commands:\n"
							+ "/fixlighting <minY> <maxY> [world]\n"
							+ "/fixlighting reload");
				}
			}
			if(args.length == 2) {
				return handleFixlightingCommand(sender, args[0], args[1]);
			}
			if(args.length == 3) {
				return handleFixlightingCommand(sender, args[0], args[1], args[2]);
			}
		}
		
		return false;
	}
	
	public boolean handleReloadCommand(CommandSender sender) {
		plugin.reloadConfig();
		materials.clear();
		loadConfig();
		sender.sendMessage("§aLightingFixer config reloaded.");
		return true;
	}
	
	public boolean handleFixlightingCommand(CommandSender sender, String minimumY, String maximumY) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cYou have to specify a world from Console.");
			return true;
		}
		
		Player player = (Player) sender;
		World world = player.getWorld();
		int minY;
		int maxY;
		try {
			minY = Integer.parseInt(minimumY);
			maxY = Integer.parseInt(maximumY);
		} catch(Exception e) {
			player.sendMessage("§cError while loading coordinates.");
			return true;
		}
		
		return fixLighting(sender, world, minY, maxY);
	}
	
	public boolean handleFixlightingCommand(CommandSender sender, String minimumY, String maximumY, String worldName) {
		World world;
		try {
			if(Bukkit.getWorld(worldName) == null) {
				sender.sendMessage("§cCan't find world " + worldName);
				return true;
			}
			world = Bukkit.getWorld(worldName);
		} catch(Exception e) {
			sender.sendMessage("§cCan't find world " + worldName);
			return true;
		}
		
		int minY;
		int maxY;
		try {
			minY = Integer.parseInt(minimumY);
			maxY = Integer.parseInt(maximumY);
		} catch(Exception e) {
			sender.sendMessage("§cError while loading coordinates.");
			return true;
		}
		
		return fixLighting(sender, world, minY, maxY);
	}
	
	public boolean fixLighting(CommandSender sender, World world, int minY, int maxY) {
		//Creating a mashmap for the blocks changing
		HashMap<Location, Material> blocks = new HashMap<Location, Material>();
		
		if(minY > maxY) {
			sender.sendMessage("§cError: MinY can't be greater than MaxY!");
			return true;
		}
		if((maxY - minY) > maxheight) {
			sender.sendMessage("§cError: Maximum height of lighting fix: " + maxheight);
			return true;
		}

		sender.sendMessage("§eAttempting to fix lighting for world " + world.getName() + " for minY:" + minY + " maxY:" + maxY + ". This may take a long time!");
		
		for (Chunk chunk : world.getLoadedChunks()) {
			for(int x = 0; x < 16; x++){
		    	for(int y = minY; y < maxY; y++) {
		    		for(int z = 0; z< 16; z++) {
		    			Block block = chunk.getBlock(x, y, z);
		    			if(materials.contains(block.getType())) {
		    				//Debug: Bukkit.getLogger().info("Updating " + block);
		    				blocks.put(block.getLocation(), block.getType());
			    			block.setType(Material.STONE);
		    			}
		    		}
		    	}
		    }
		}
		//Debug: Bukkit.getLogger().info(blocks);
		new BukkitRunnable() {
			public void run() {
				for(Location loc : blocks.keySet()) {
					Material mat = blocks.get(loc);
					Block block = loc.getWorld().getBlockAt(loc);
					if(mat.equals(Material.REDSTONE_LAMP)) {
						block.setType(mat);
						Lightable lightable = (Lightable) block.getBlockData();

				        lightable.setLit(true);
				        block.setBlockData(lightable);
					} else {
						block.setType(mat);
					}
					block.getState().update();
				}
				sender.sendMessage("§aLighting fixed!");
			}
		}.runTaskLater(plugin, 1*20);
		
		return true;
	}
}
