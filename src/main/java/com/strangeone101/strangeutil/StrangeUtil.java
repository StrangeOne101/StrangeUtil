package com.strangeone101.strangeutil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.strangeone101.strangeutil.MessageDatabase.MsgData;

public class StrangeUtil extends JavaPlugin implements Listener
{	
	public static StrangeUtil instance;
	public static Logger log = Logger.getLogger("StrangeUtil");
	
	/**For Msg command and stuff. msgdata is the stored message database for offline players
	 * Loners is a list of players who message themselves XD*/
	protected MessageDatabase msgdata; 
	protected HashMap<String, Integer> loners = new HashMap<String, Integer>();
	
	protected List<UUID> convertingPlayers = new ArrayList<UUID>();
	
	@Override
	public void onEnable() 
	{
		instance = this;
		msgdata = new MessageDatabase();
		msgdata.load();
		
		CommandWorld.load();
		CommandPWarp.load();
		ConfigManager.load(new File(getDataFolder(), "config.yml"));
		//OldWorldLoader.load();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		super.onEnable();
	}
	
	public void onDisableMethod() 
	{
		for (UUID p : msgdata.data.keySet())
		{
			List<MsgData> data = msgdata.data.get(p);
			for (MsgData d : data)
			{
				if (d.read)
				{
					msgdata.data.get(p).remove(d);
				}
			}
		}
		
		msgdata.save();
		CommandWorld.save();
		//OldWorldLoader.save();
		CommandPWarp.save();
		super.onDisable();
	}
	
	@Override
	public void saveConfig() 
	{
		this.onDisableMethod();
		super.saveConfig();
	}
	
	@Override
	public void onDisable() 
	{
		this.onDisableMethod();
		super.onDisable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if (command.getName().equalsIgnoreCase("msg") || command.getName().equalsIgnoreCase("tell") || command.getName().equalsIgnoreCase("message"))
		{
			return CommandMsg.run(sender, args);
		}
		else if (command.getName().equalsIgnoreCase("strangedebug"))
		{
			if (sender instanceof Player)
			{
				EntityPlayer player = ((CraftPlayer)sender).getHandle();
				player.noclip = player.noclip ? false : true;
				((CraftPlayer)sender).setHandle(player);
			}
			return true;
		}
		else if (command.getName().equalsIgnoreCase("world"))
		{
			return CommandWorld.run(sender, args);
		}
		else if (command.getName().equalsIgnoreCase("r") || command.getName().equalsIgnoreCase("reply"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage("Only players can use this command.");
				return true;
			}
			if (msgdata.lastPlayerMsg.containsKey(((Player)sender).getUniqueId()))
			{
				UUID id = msgdata.lastPlayerMsg.get(((Player)sender).getUniqueId());
				if (id == null)
				{
					sender.sendMessage(ChatColor.RED + "You cannot send messages to the console!");
					return true;
				}
				OfflinePlayer player = Bukkit.getOfflinePlayer(id);
				String s = player.getName();
				for (String s1 : args)
				{
					s = s + " " + s1;
				}
				Bukkit.dispatchCommand(sender, "msg " + s);
				return true;
			}
		}
		else if (command.getName().equalsIgnoreCase("pw") || command.getName().equalsIgnoreCase("privatewarp") || command.getName().equalsIgnoreCase("pwarp") || command.getName().equalsIgnoreCase("privwarp"))
		{
			return CommandPWarp.execute(sender, args);
		}
		else if (command.getName().equalsIgnoreCase("mount"))
		{
			return CommandMount.execute(sender, args);
		}
		
		return false;
	}
	
	
	
	@EventHandler
	public void onInvPickup(PlayerPickupItemEvent e)
	{
		ItemStack item = e.getItem().getItemStack();
		if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3)
		{
			SkullMeta meta = (SkullMeta)item.getItemMeta();
			if (meta.hasOwner())
			{
				String owner = meta.getOwner();
				meta.setDisplayName(ChatColor.YELLOW + owner + ChatColor.RESET + (owner.endsWith("s") ? "'" : "'s") + " Head");
			}
			if (ConfigManager.skullsEnabled && e.getPlayer().hasPermission("strangeutil.skulls"))
			{
				ArrayList<String> lore = new ArrayList<String>();
	        	lore.add(ChatColor.GRAY + "Right click on a player to change!");
	        	meta.setLore(lore);
			}
			else
			{
				meta.setLore(new ArrayList<String>());
			}
			item.setItemMeta(meta);
			e.getItem().setItemStack(item);
		}
	}
	
	@EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
    	Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if(player.getItemInHand().getType() == Material.SKULL_ITEM && player.getItemInHand().getDurability() == 3) 
    	{
        	if(entity instanceof Player == true && player.hasPermission("strangeutil.skulls"))
        	{ 
            	Player playerClicked  = (Player)event.getRightClicked();
            	
            		ItemStack skull = player.getItemInHand();
            		SkullMeta meta = (SkullMeta)skull.getItemMeta();
            		meta.setOwner(playerClicked.getName());
            		String owner = playerClicked.getName();
            		meta.setDisplayName(ChatColor.YELLOW + owner + ChatColor.RESET + (owner.endsWith("s") ? "'" : "'s") + " Head");
            		ArrayList<String> lore = new ArrayList<String>();
            		lore.add(ChatColor.GRAY + "Right click on a player to change!");
            		meta.setLore(lore);
            		skull.setItemMeta(meta);
            }
            else if (!player.hasPermission("strangeutil.skulls") && entity instanceof Player == true)
            {
            	player.sendMessage(ChatColor.RED + "You do not have permission to edit skulls!");
            }
    	}
    }
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e)
	{
		
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLoginPre(PlayerLoginEvent e)
	{
		//Autokicker
		Player player = e.getPlayer();
		if (player.getName().contains(" "))
		{
			log(Level.INFO, "Auto kicked user from " + e.getAddress() + " for logging in with an invalid username: \"" + player.getName() + "\"");
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (p.hasPermission("strangeutil.alerts"))
				{
					p.sendMessage(ChatColor.GREEN + "Prevented user \"" + player.getName() + "\" from connecting (" + e.getAddress() + ") for logging in with an invalid username.");
				}
			}
			e.setKickMessage("Please use a valid username and try again.");
			e.setResult(Result.KICK_OTHER);
		}
		
		if (!OldWorldLoader.players.contains(e.getPlayer().getUniqueId()) && e.getPlayer().hasPlayedBefore())
		{
			log(Level.INFO, "Detecting old player connecting - Updating data...");
			convertingPlayers.add(e.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLoginPost(PlayerJoinEvent e)
	{
		//Message database
		final Player player = e.getPlayer();
		if (msgdata.data.containsKey(player.getUniqueId()))
		{
			List<MsgData> list = msgdata.getMsgForPlayer(player.getUniqueId());
			if (list.size() > 0)
			{
				List<String> players = new ArrayList<String>();
				String first = "";
				for (MsgData d : list)
				{
					if (!players.contains(d.from.getName()))
					{
						players.add(d.from.getName());
						if (first.equals(""))
						{
							first = d.from.getName();
						}
					}
				}
				final ChatColor y = ChatColor.YELLOW;
				final ChatColor g = ChatColor.GOLD;
				final List<String> p = players;
				final int size = list.size();
				BukkitRunnable run = new BukkitRunnable() {

					public void run() 
					{
						String s = p.size() == 1 ? g + p.get(0) : (p.size() == 2 ? g + p.get(0) + y + " and " + g + p.get(1) : g + p.get(0) + y + " and " + (p.size() - 1) + " others");
						player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + size + ChatColor.YELLOW + " unread messages from " + ChatColor.GOLD + s);
						player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/msg view" + ChatColor.YELLOW + " to view them.");
					}
				};
				run.runTaskLater(this, 1L);
			}
		}
		
		if (ConfigManager.convertFromOldWorld && OldWorldLoader.ENABLED)
		{
			if (convertingPlayers.contains(e.getPlayer().getUniqueId()))
			{
				OldWorldLoader.players.add(e.getPlayer().getUniqueId());
				convertingPlayers.remove(e.getPlayer().getUniqueId());
				Map<UUID, Location> map = CommandWorld.playerLocations.get(OldWorldLoader.convertingWorld);
				if (!map.containsKey(e.getPlayer().getUniqueId()))
				{
					CommandWorld.playerLocations.get(OldWorldLoader.convertingWorld).put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
					e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
					e.getPlayer().sendMessage(ChatColor.YELLOW + ConfigManager.welcomeMsg);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent e)
	{
		log.info(e.getPlayer().toString());
		if (msgdata.getMsgForPlayer(e.getPlayer().getUniqueId()).size() > 0) //TODO FIX
		{
			for (MsgData d : msgdata.getMsgForPlayer(e.getPlayer().getUniqueId()))
			{
				if (d.read)
				{
					msgdata.getMsgForPlayer(e.getPlayer().getUniqueId()).remove(d);
				}
			}
		}
		if (loners.containsKey(e.getPlayer().getName()))
		{
			loners.remove(e.getPlayer().getName());
		}
	}
	
	public static String makeListFancy(List<String> list)
	{
		if (list.size() == 0)
		{
			return ChatColor.ITALIC + "(None)";
		}
		String s = "";
		for (int i = 0; i < list.size(); i++)
		{
			if (i == 0) {s = list.get(0);}
			else if (i == list.size() - 1) {s = s + " and " + list.get(i);}
			else {s = s + ", " + list.get(i);}
		}
		return s;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFireSpread(BlockSpreadEvent e)
	{
		if (e.getNewState().getType() == Material.FIRE && ConfigManager.preventFireSpreading)
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFireBurn(BlockBurnEvent e)
	{
		if (ConfigManager.preventFireBlockBurning && ConfigManager.fireDie != 0.0F)
		{
			if (new Random().nextInt(100) < ConfigManager.fireDie * 100)
			{
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(0, 1, 0).getLocation());
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(0, -1, 0).getLocation());
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(1, 0, 0).getLocation());
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(-1, 0, 0).getLocation());
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(0, 0, 1).getLocation());
				this.putFireOut(e.getBlock().getWorld(),e.getBlock().getRelative(0, 0, -1).getLocation());
				
			}
			e.setCancelled(true);
		}
	}
	
	public void putFireOut(World world, Location loc)
	{
		if (world.getBlockAt(loc).getType() == Material.FIRE && world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType() != Material.NETHERRACK)
		{
			world.getBlockAt(loc).breakNaturally();
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e)
	{
		if (ConfigManager.preventIceDamage && e.getCause() == DamageCause.SUFFOCATION)
		{
			if (e.getEntity() instanceof EntityLiving)
			{
				EntityLiving entity = (EntityLiving) e.getEntity();
				BlockPosition bp = new BlockPosition(entity.getBukkitEntity().getLocation().getBlockX(), 
						entity.getBukkitEntity().getLocation().getBlockY() + (int)entity.getHeadHeight(),
						entity.getBukkitEntity().getLocation().getBlockZ());
				Block block = entity.getWorld().getType(bp).getBlock();
				if (block.getMaterial() == net.minecraft.server.v1_8_R3.Material.ICE)
				{
					e.setCancelled(true);
					e.setDamage(0D);
				}
			}
		}
	}
	
	public static void log(Level level, String msg)
	{
		log.log(level, "[StrangeUtil] " + msg);
	}
}
