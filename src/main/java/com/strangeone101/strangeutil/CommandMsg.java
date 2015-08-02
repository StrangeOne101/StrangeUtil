package com.strangeone101.strangeutil;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.strangeone101.strangeutil.MessageDatabase.MsgData;

public class CommandMsg 
{	
	@SuppressWarnings("deprecation")
	public static boolean run(CommandSender sender, String[] args)
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			sender.sendMessage(ChatColor.YELLOW + "Usage is: " + ChatColor.GOLD + "/msg <player> <message>");
			sender.sendMessage(ChatColor.GOLD + "/msg view [page]" + ChatColor.YELLOW + " or " + ChatColor.GOLD + "/msg clear");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("read"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage("Only players can run this command!");
				return true;
			}
			
			if (StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).size() == 0)
			{
				sender.sendMessage(ChatColor.RED + "You have no unread messages!");
				return true;
			}
			
			int page = 0;
			if (args.length >= 2)
			{
				try
				{
					page = Integer.parseInt(args[1]) - 1;
					if (page <= 0 || page > StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).size() / 5 + 1)
					{
						sender.sendMessage(ChatColor.RED + "Page not found!");
						return true;
					}
				}
				catch (NumberFormatException e)
				{
					sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Page must be a number!");
					return true;
				}
			}
			int i = (page) * 5;
			int size = StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).size();
			while (i < page * 5 + 5 && i < size)
			{
				sendPlayerMsg((Player) sender, StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i));
				StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i).read = true;
				i++;
			}
			
			if (size - (page * 5) > 5) //If not on the last page
			{
				sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/msg view " + (page + 2) + ChatColor.YELLOW + " to go to the next page");
			}
			sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/msg clear" + ChatColor.YELLOW + " to clear your messages");
			return true;
			
			/*int i = 0;
			try
			{
				i = Integer.parseInt(args[1]);
				if (i < 0 || i > StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).size())
				{
					sender.sendMessage(ChatColor.RED + "Message not found!");
					return true;
				}
				else
				{
					sendPlayerMsg((Player) sender, StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i));
					StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i).read = true;
					return true;
				}
				
			}
			catch (NumberFormatException e)
			{
				for (int i1 = 0; i1 < StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).size(); i1++)
				{
					if (args[1].equalsIgnoreCase(StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i1).from.getName()))
					{
						sendPlayerMsg((Player) sender, StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i1));
						StrangeUtil.instance.msgdata.getMsgForPlayer(((Player) sender).getUniqueId()).get(i1).read = true;
						return true;
					}
				}
			}*/
		}
		else if (args[0].equalsIgnoreCase("clear"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You cannot clear messages from the console!");
				return true;
			}
			if (StrangeUtil.instance.msgdata.getMsgForPlayer(((Player)sender).getUniqueId()).isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "Your have no messages to clear!");
			}
			else
			{
				StrangeUtil.instance.msgdata.getMsgForPlayer(((Player)sender).getUniqueId()).clear();
				sender.sendMessage(ChatColor.YELLOW + "Messages cleared!");
			}
			return true;
		}
		
		Player player = (Player)Bukkit.getPlayer(args[0]);
		OfflinePlayer offlinePlayer = null;
		if (player == null)
		{
			offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		}
		else
		{
			offlinePlayer = player;
		}
		
		if (//(player != null && !player.hasPlayedBefore()) || 
				(offlinePlayer != null && !offlinePlayer.hasPlayedBefore()) && !offlinePlayer.isOnline())
		{
			sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + args[0] + " has never played before!");
			return true;
		}
		
		String s = "";
		for (int i = 1; i < args.length; i++)
		{
			if (i != args.length - 1)
			{
				s = s + args[i] + " ";
			}
			else s = s + args[i];
		}
		
		if (//(player != null && !player.isOnline()) || 
				offlinePlayer != null && !offlinePlayer.isOnline())
		{
			/*if (!(sender instanceof Player))
			{
				sender.sendMessage("Error: Player is not online!");
				return true;
			}*/
			
			sender.sendMessage(ChatColor.RED + args[0] + " is currently offline! They will recieve your message when they next log in.");
			StrangeUtil.instance.msgdata.addMessage(sender, offlinePlayer, System.currentTimeMillis(), s);
		}
		else if (player instanceof Player)
		{		
			((Player)player).sendMessage(ChatColor.GOLD + "[" + sender.getName() + " -> You] " + ChatColor.WHITE + s);
			if (sender instanceof Player)
			{
				StrangeUtil.instance.msgdata.lastPlayerMsg.put(player.getUniqueId(), ((Player)sender).getUniqueId());
			}
			else
			{
				StrangeUtil.instance.msgdata.lastPlayerMsg.put(player.getUniqueId(), null);
			}
		}

		ChatColor color = offlinePlayer.isOnline() ? ChatColor.GOLD : ChatColor.DARK_GRAY;
		sender.sendMessage(ChatColor.GOLD + "[You -> " + color + args[0] + ChatColor.GOLD + "] " + ChatColor.WHITE + s);
		if (sender.getName().equalsIgnoreCase(args[0]))
		{
			if (StrangeUtil.instance.loners.containsKey(sender.getName()))
			{
				StrangeUtil.instance.loners.put(sender.getName(), StrangeUtil.instance.loners.get(sender.getName()) + 1);
			}
			else
			{
				StrangeUtil.instance.loners.put(sender.getName(), 0);
			}
			makeSureTheyAreOkay(sender);
			
		}
		return true;
	}
	
	protected static void makeSureTheyAreOkay(CommandSender sender)
	{
		if (StrangeUtil.instance.loners.containsKey(sender.getName()))
		{
			int i = StrangeUtil.instance.loners.get(sender.getName());
			if (i == 0) {sender.sendMessage(ChatColor.GRAY + "(You want to talk to yourself? Lolk then, fine by me.)");}
			else if (i == 5) {sender.sendMessage(ChatColor.GRAY + "(Having fun?)");}
			else if (i == 10) {sender.sendMessage(ChatColor.GRAY + "(Must be pretty intense. Talking to yourself.)");}
			else if (i == 15) {sender.sendMessage(ChatColor.GRAY + "(Really.)");}
			else if (i == 20) {sender.sendMessage(ChatColor.GRAY + "(Sure you don't want to stop?)");}
			else if (i == 25) {sender.sendMessage(ChatColor.GRAY + "(Hello...?)");}
			else if (i == 30) {sender.sendMessage(ChatColor.GRAY + "(I know you like talking to yourself but it's getting kinda creepy...)");}
			else if (i == 35) {sender.sendMessage(ChatColor.GRAY + "(Seriously dude)");}
			else if (i == 45) {sender.sendMessage(ChatColor.GRAY + "(I'm actually starting to get worried)");}
			else if (i == 50) {sender.sendMessage(ChatColor.GRAY + "(You have messages yourself 50 TIMES! It's time to STOP!)");}
			else if (i == 55) {sender.sendMessage(ChatColor.GRAY + "(STOP DAMMIT)");}
			else if (i == 60) {sender.sendMessage(ChatColor.GRAY + "(STOP!)");}
			else if (i == 65) {sender.sendMessage(ChatColor.GRAY + "(Fine then. Alright. You like talking to yourself.)");}
			else if (i == 70) {sender.sendMessage(ChatColor.GRAY + "(It's okay. I'm sure it's complely normal.)");}
			else if (i == 75) {sender.sendMessage(ChatColor.GRAY + "(I was kidding, this is beyond abnormal. STOP, dude, STOP!");}
			else if (i == 80) {sender.sendMessage(ChatColor.GRAY + "(I'm calling a doctor now. Hope you're okay.)");}
			else if (i == 85) {sender.sendMessage(ChatColor.GRAY + "(Well, I got good news and I got bad news.)");}
			else if (i == 90) {sender.sendMessage(ChatColor.GRAY + "(Actually, I lied. I just got bad news.)");}
			else if (i == 95) {sender.sendMessage(ChatColor.GRAY + "(The doctor is out of town and I'm out of ideas. Sorry dude. Looks like you're a goner.)");}
			else if (i == 100) {sender.sendMessage(ChatColor.GRAY + "(You've even reached 100 messages to yourself. I don't know whether to congratulate you or to be utterly ashamed.)");}
			else if (i == 105) {sender.sendMessage(ChatColor.GRAY + "(Ashamed... yeah. Just ashamed.)");}
			else if (i == 110) {sender.sendMessage(ChatColor.GRAY + "(Keep up this... whatever you're doing... Yeah. I'm sure someone will find you.)");}
			else if (i == 115) {sender.sendMessage(ChatColor.GRAY + "(#Loner4evars)");}
			else if (i == 500) {sender.sendMessage(ChatColor.GRAY + "(That's certainly quite a feat! I called the doctor again but he was useless. So I'm sure you're fine :D )");}
			else if (i == 505) 
			{
				sender.sendMessage(ChatColor.GRAY + "(And here... have this diamond for all your effort! :D)");
				if (sender instanceof Player)
				{
					ItemStack stack = new ItemStack(Material.DIAMOND);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Loner Diamond");
					meta.setLore(Arrays.asList(new String[] {ChatColor.DARK_GRAY + "A special diamond for that one loner"}));
					stack.setItemMeta(meta);
					((Player)sender).getInventory().addItem(stack);
				}
			}
			else if (i == 510) {sender.sendMessage(ChatColor.GRAY + "(**Bling bling**)");}
		}
	}
	
	protected static void sendPlayerMsg(Player player, MsgData data)
	{
		long diff = System.currentTimeMillis() - data.timestamp;
		String s = "-1 years ago";
		if (TimeUnit.MILLISECONDS.toDays(diff) != 0)
		{
			s = TimeUnit.MILLISECONDS.toDays(diff) + " days ago";
		}
		else if (TimeUnit.MILLISECONDS.toHours(diff) != 0)
		{
			s = TimeUnit.MILLISECONDS.toHours(diff) + " hours ago";
		}
		else if (TimeUnit.MILLISECONDS.toMinutes(diff) != 0)
		{
			s = TimeUnit.MILLISECONDS.toMinutes(diff) + " minutes ago";
		}
		else
		{
			s = TimeUnit.MILLISECONDS.toSeconds(diff) + " seconds ago";
		}
		player.sendMessage(ChatColor.GOLD + data.from.getName() + ChatColor.GRAY + " (" + s + ")" + ChatColor.GOLD+ ": " + ChatColor.YELLOW + data.msg);
	}
}
