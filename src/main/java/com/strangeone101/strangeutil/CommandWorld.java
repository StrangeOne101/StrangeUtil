package com.strangeone101.strangeutil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

public class CommandWorld 
{
	protected static Map<String, String> loadedWorlds = new HashMap<String, String>();
	protected static Map<String, Map<UUID, Location>> playerLocations = new HashMap<String, Map<UUID, Location>>();
	public static boolean run(CommandSender sender, String[] args)
	{
		if (args.length == 0)
		{
			sender.sendMessage(ChatColor.RED + "Usage is " + ChatColor.DARK_RED + "/world list " + ChatColor.RED + " or " + ChatColor.DARK_RED + "/world <world>");
			return true;
		}
		if (args[0].equalsIgnoreCase("list"))
		{
			sender.sendMessage(ChatColor.YELLOW + "List of avaliable worlds: " + StrangeUtil.makeListFancy(Lists.newArrayList(loadedWorlds.keySet())));
		}
		else if (args[0].equalsIgnoreCase("add"))
		{
			sender.sendMessage(ChatColor.RED + "Coming soon!");
		}
		else if (CommandWorld.loadedWorlds.keySet().contains(args[0]))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "This command can only be run by players!");
				return true;
			}
			Player p = (Player) sender;
			if (CommandWorld.playerLocations.get(args[0]) == null || !CommandWorld.playerLocations.get(args[0]).containsKey(p.getUniqueId()))
			{
				World world = Bukkit.getWorld(CommandWorld.loadedWorlds.get(args[0]));
				Location prevLoc = p.getLocation();
				String w = getWorldFromLocation(prevLoc);
				if (playerLocations.get(w) == null)
				{
					playerLocations.put(w, new HashMap<UUID, Location>());
				}
				playerLocations.get(w).put(p.getUniqueId(), prevLoc);
				p.teleport(world.getSpawnLocation());
			}
			else
			{
				Location loc = CommandWorld.playerLocations.get(args[0]).get(p.getUniqueId());
				Location prevLoc = p.getLocation();
				String w = getWorldFromLocation(prevLoc);
				if (playerLocations.get(w) == null)
				{
					playerLocations.put(w, new HashMap<UUID, Location>());
				}
				playerLocations.get(w).put(p.getUniqueId(), prevLoc);
				p.teleport(loc);
			}
		}
		return true;
	}
	
	public static void load()
	{
		try
		{
			File file = new File(StrangeUtil.instance.getDataFolder(), "worlds.yml");
			if (!file.exists())
			{
				file.createNewFile();
			}
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			Set<String> worlds = config.getKeys(false);
			for (String s : worlds)
			{
				final String level = config.getString(s + "." + "LevelName");
				if (level == null || level.equals("") || level.equalsIgnoreCase("<LevelName>"))
				{
					StrangeUtil.log.warning("[StrangeUtil] World \"" + s + "\" not loaded as no LevelName was provided");
				}
				else
				{
					World world = Bukkit.getWorld(level);
					if (world == null)
					{
						StrangeUtil.log(Level.WARNING, "[StrangeUtil] World \"" + s + "\" not found with levelname: " + level + ". Generating new world...");
						BukkitRunnable run = new BukkitRunnable() {

							public void run() 
							{
								Bukkit.createWorld(new WorldCreator(level));
								StrangeUtil.log(Level.INFO, "World generation for world \"" + level + "\" complete.");
							}
						};
						run.runTask(StrangeUtil.instance);
						
						//config.set(s + ".LevelName", "<LevelName>");
					}
					loadedWorlds.put(s, level);
						
					if (config.contains(s + ".PlayerData"))
					{
						ConfigurationSection section = config.getConfigurationSection(s + ".PlayerData");
						Set<String> keys = section.getKeys(false);
						for (String s1 : keys)
						{
							try
							{
								StrangeUtil.log(Level.INFO, "Loading key: " + s1);
								double x = Double.parseDouble(config.getString(s + ".PlayerData." + s1).split("\\,")[0]);
								double y = Double.parseDouble(config.getString(s + ".PlayerData." + s1).split("\\,")[1]);
								double z = Double.parseDouble(config.getString(s + ".PlayerData." + s1).split("\\,")[2]);
								float pitch = Float.parseFloat(config.getString(s + ".PlayerData." + s1).split("\\,")[3]);
								float yaw = Float.parseFloat(config.getString(s + ".PlayerData." + s1).split("\\,")[4]);
								Location loc = new Location(world, x, y, z);
								loc.setPitch(pitch);
								loc.setYaw(yaw);
								if (playerLocations.get(s) == null)
								{
									playerLocations.put(s, new HashMap<UUID, Location>());
								}
								playerLocations.get(s).put(UUID.fromString(s1), loc);
									
							}
							catch (IndexOutOfBoundsException e1)
							{
								StrangeUtil.log.warning("[StrangeUtil] Playerdata not loaded correctly for UUID: " + s1);
								//config.set(s + ".PlayerData." + s1, "");
							}
							catch (NumberFormatException e1)
							{
								StrangeUtil.log.warning("[StrangeUtil] Playerdata not loaded correctly for UUID: " + s1);
								//config.set(s + ".PlayerData." + s1, "");
							}
						}
					}
				}
				
			}
			config.save(file);
			
			StrangeUtil.log(Level.INFO, "Loaded " + loadedWorlds.keySet().size() + " worlds successfully!");
		}
		catch (IOException e) {e.printStackTrace();} 
		catch (InvalidConfigurationException e) {e.printStackTrace();}
	}
	
	public static void save()
	{
		try
		{
			File file = new File(StrangeUtil.instance.getDataFolder(), "worlds.yml");
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			YamlConfiguration config = new YamlConfiguration();
			//config.load(file);
			
			for (String worldKey : loadedWorlds.keySet())
			{
				config.set(worldKey + ".LevelName", loadedWorlds.get(worldKey));
				Map<UUID, Location> playerData = playerLocations.get(worldKey);
				if (playerData != null)
				{
					for (UUID playerID : playerData.keySet())
					{
						Location loc = playerData.get(playerID);
						String location = loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw();
						config.set(worldKey + ".PlayerData." + playerID.toString(), location);
						StrangeUtil.log(Level.INFO, location);
					}
				}
			}
			config.save(file);
		}
		catch (IOException e) {e.printStackTrace();}
		
	}
	
	/**Get a world String to use in loadedWorlds from a location. Returns null if not found.*/
	public static String getWorldFromLocation(Location location)
	{
		World w = location.getWorld();
		for (String worldKey : loadedWorlds.keySet())
		{
			if (Bukkit.getWorld(loadedWorlds.get(worldKey)).getName().equals(w.getName()))
			{
				return worldKey;
			}
		}
		return null;
	}
}
