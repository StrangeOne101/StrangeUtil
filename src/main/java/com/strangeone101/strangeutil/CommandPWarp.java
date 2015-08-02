package com.strangeone101.strangeutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPWarp 
{
	public static class Warp
	{
		String name, backupWorld;
		float x, y, z, pitch, yaw;
		World world;
		public Warp(String name, Location loc)
		{
			this.name = name;
			this.world = loc.getWorld();
			this.x = (float) loc.getX();
			this.y = (float) loc.getY();
			this.z = (float) loc.getZ();
			this.yaw = (float) loc.getYaw();
			this.pitch = (float) loc.getPitch();
		}
	}
	
	protected static Map<UUID, List<Warp>> warps = new ConcurrentHashMap<UUID, List<Warp>>();
	
	@SuppressWarnings("deprecation")
	public static boolean execute(CommandSender sender, String[] args)
	{
		if (args.length != 0 && args[0].equalsIgnoreCase("list"))
		{			
			if (args.length == 2)
			{
				if (!sender.hasPermission("strangeutil.warps.others"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to view other people's private warps!");
					return true;
				}
				String p = args[1];
				OfflinePlayer player = Bukkit.getOfflinePlayer(p);
				if (player == null)
				{
					sender.sendMessage(ChatColor.RED + "Player not found!");
					return true;
				}
				String s = "";
				List<String> warps = getWarpsForUser(player);
				for (String s1 : warps)
				{
					s = s == "" ? ChatColor.GOLD + s1 : s + ChatColor.YELLOW + "," + ChatColor.GOLD + s1;
				}
				sender.sendMessage(ChatColor.YELLOW + "List of private warps (" + player.getName() + "): " + ChatColor.GOLD + StrangeUtil.makeListFancy(getWarpsForUser(player)));
			}
			else
			{
				if (!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Only players can run this command!");
					return true;
				}
				sender.sendMessage(ChatColor.YELLOW + "List of private warps: " + ChatColor.GOLD + StrangeUtil.makeListFancy(getWarpsForUser((Player) sender)));
			}
		}
		else if (args.length != 0 && Bukkit.getOfflinePlayer(args[0]) != null && Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore())
		{
			if (!sender.hasPermission("strangeutil.warps.others"))
			{
				sender.sendMessage(ChatColor.RED + "Warp not found!");
				return true;
			}
			if (args.length == 1)
			{
				sender.sendMessage(ChatColor.RED + "Usage is /pwarp <user> <warp>");
				return true;
			}
			
			Warp w = getWarpFromUser(Bukkit.getOfflinePlayer(args[0]), args[1]);
			if (w == null)
			{
				sender.sendMessage(ChatColor.YELLOW + "List of private warps (" + Bukkit.getOfflinePlayer(args[0]).getName() + "): " + ChatColor.GOLD + StrangeUtil.makeListFancy(getWarpsForUser(Bukkit.getOfflinePlayer(args[0]))));
				return true;
			}
			else if (sender instanceof Player)
			{
				World world = w.world;
				if (world == null)
				{
					if (Bukkit.getWorld(w.backupWorld) == null)
					{
						sender.sendMessage(ChatColor.RED + "The world you are looking for isn't currently loaded! Please try again or contact an admin!");
						return true;
					}
					world = Bukkit.getWorld(w.backupWorld);
				}
				Location loc = new Location(world, w.x, w.y, w.z, w.yaw, w.pitch);
				sender.sendMessage(ChatColor.GOLD + "Teleporting...");
				((Player)sender).teleport(loc);
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
			}
		}
		else if (args.length != 0 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("add")))
		{
			if (args.length == 1)
			{
				sender.sendMessage(ChatColor.YELLOW + "Usage is: " + ChatColor.GOLD + "/pwarp add <name>");
				return true;
			}
			if (sender instanceof Player)
			{
				if (getWarpsForUser((OfflinePlayer) sender).size() >= ConfigManager.maxPrivateWarps)
				{
					sender.sendMessage(ChatColor.RED + "You cannot create any more private warps because you have exceeded the max number of " + ConfigManager.maxPrivateWarps + " warps. Delete a private warp and try again!");
					return true;
				}
				
				List<String> invalidChars = Arrays.asList(new String[] {"-", ",", ".", "§", "|"});
				for (String c : args[1].split(""))
				{
					if (invalidChars.contains(c))
					{
						sender.sendMessage(ChatColor.RED + "Private warp names can only have letters and numbers in them!");
						return true;
					}
				}
				if (getWarpFromUser((OfflinePlayer) sender, args[1]) != null)
				{
					sender.sendMessage(ChatColor.RED + "A private warp already exists with that name!");
					return true;
				}
				if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("list"))
				{
					sender.sendMessage(ChatColor.RED + "You cannot create a warp with that name!");
					return true;
				}
				if (Bukkit.getOfflinePlayer(args[1]) != null && Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore())
				{
					sender.sendMessage(ChatColor.RED + "You cannot create a warp that is a name of a player!");
					return true;
				}
				if (!warps.containsKey(((Player)sender).getUniqueId()))
				{
					warps.put(((Player)sender).getUniqueId(), new ArrayList<Warp>());
				}
				warps.get(((Player)sender).getUniqueId()).add(new Warp(args[1], ((Player)sender).getLocation()));
				sender.sendMessage(ChatColor.YELLOW + "Private warp " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " created!");
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
			}
		}
		else if (args.length != 0 && args[0].equalsIgnoreCase("remove"))
		{
			if (sender instanceof Player)
			{
				if (args.length == 1)
				{
					sender.sendMessage(ChatColor.YELLOW + "Usage is: " + ChatColor.GOLD + "/pwarp remove <name>");
					return true;
				}
				if (getWarpFromUser((OfflinePlayer) sender, args[1]) == null)
				{
					sender.sendMessage(ChatColor.RED + "Private warp \"" + args[1] + "\" not found!");
					return true;
				}
				Warp w = getWarpFromUser((OfflinePlayer) sender, args[1]);
				warps.get(((OfflinePlayer)sender).getUniqueId()).remove(w);
				sender.sendMessage(ChatColor.RED + "Warp \"" + args[1] + "\" removed!");
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
			}
		}
		else if (args.length == 0 || args[0].equalsIgnoreCase("help")) // /pwarp help
		{
			sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.GOLD + "/pwarp <warp>" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/pwarp <add/remove/list>");
			sender.sendMessage(ChatColor.GOLD + "Allows you to teleport to private warps that only you can access");
		}
		else 
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
				return true;
			}
			Warp w = getWarpFromUser((OfflinePlayer) sender, args[0]);
			if (w == null)
			{
				sender.sendMessage(ChatColor.RED + "Warp \"" + args[0] + "\" not found!");
				return true;
			}
			World world = w.world;
			if (world == null)
			{
				if (Bukkit.getWorld(w.backupWorld) == null)
				{
					sender.sendMessage(ChatColor.RED + "The world you are looking for isn't currently loaded! Please try again or contact an admin!");
					return true;
				}
				world = Bukkit.getWorld(w.backupWorld);
			}
			Location loc = new Location(world, w.x, w.y, w.z, w.yaw, w.pitch);
			sender.sendMessage(ChatColor.GOLD + "Teleporting...");
			((Player)sender).teleport(loc);
		}
		
		return true;
	}
	
	public static List<String> getWarpsForUser(OfflinePlayer player)
	{
		List<String> warps_ = new ArrayList<String>();
		if (!warps.containsKey(player.getUniqueId()))
		{
			warps.put(player.getUniqueId(), new ArrayList<Warp>());
		}
		List<Warp> warps__ = warps.get(player.getUniqueId());
		for (Warp w : warps__)
		{
			warps_.add(w.name);		
		}
		return warps_;
	}
	
	public static Warp getWarpFromUser(OfflinePlayer player, String warpname)
	{
		if (!warps.containsKey(player.getUniqueId())) 
		{
			warps.put(player.getUniqueId(), new ArrayList<Warp>());
			return null;
		}
		List<Warp> warps__ = warps.get(player.getUniqueId());
		for (Warp w : warps__)
		{
			if (w.name.equalsIgnoreCase(warpname))
			{
				return w;
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static void load()
	{
		File file = new File(StrangeUtil.instance.getDataFolder(), "privatewarps.dat");
		if (file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				while (line != null)
				{
					UUID id = UUID.fromString(line.split(" ", 3)[0]);
					String playerS = line.split(" ", 3)[1];
					String s = line.split(" ", 3)[2];
					if (id == null)
					{
						id = Bukkit.getOfflinePlayer(playerS).getUniqueId();
					}
					
					
					List<Warp> ws = new ArrayList<Warp>();
					for (String s1 : s.split("\\|"))
					{
						if (s1 != "")
						{
							try
							{
								String warpName = s1.split("\\,")[0];
								double x = Double.parseDouble(s1.split("\\,")[1]);
								double y = Double.parseDouble(s1.split("\\,")[2]);
								double z = Double.parseDouble(s1.split("\\,")[3]);
								float yaw = Float.parseFloat(s1.split("\\,")[4]);
								float pitch = Float.parseFloat(s1.split("\\,")[5]);
								String world = s1.split("\\,")[6];
								Warp warp = new Warp(warpName, new Location(null, x, y, z, yaw, pitch));
								warp.backupWorld = world;
								ws.add(warp);	
							}
							catch (Exception e) {StrangeUtil.log(Level.INFO, "Could not load warp from player " + playerS);e.printStackTrace();}	
						}	
					}
					
					warps.put(id, ws);
					
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException e) {e.printStackTrace();}
			catch (Exception e) {e.printStackTrace();}
		}
	}
	
	public static void save()
	{
		File file = new File(StrangeUtil.instance.getDataFolder(), "privatewarps.dat");
		if (file.exists())
		{
			file.delete();
		}
		
		try
		{
			file.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (UUID id : warps.keySet())
			{
				String s = id.toString() + " " + Bukkit.getOfflinePlayer(id).getName() + " ";
				List<Warp> ws = warps.get(id);
				if (ws != null && !ws.isEmpty() && ws.size() != 0)
				{
					for (Warp w : ws)
					{
						try
						{
							if (w != null) 
							{
								String s1 = w.name + "," + w.x + "," + w.y + "," + w.z + "," + w.yaw + "," + w.pitch + "," + (w.world == null ? w.backupWorld : w.world.getName());
								
								if (!w.equals(ws.get(ws.size() - 1)))
								{
									s = s + s1 + "|";
								}
								else
								{
									s = s + s1;
								}
							}
						}
						catch (Exception e) {StrangeUtil.log(Level.SEVERE, "Failed to save private warp!");e.printStackTrace();continue;}
					}
					
					writer.write(s);
					writer.newLine();
					writer.flush();
				}
			}
			writer.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}
}
