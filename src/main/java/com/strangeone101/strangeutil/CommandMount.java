package com.strangeone101.strangeutil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMount 
{
	public static boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "Only players can use this command!");
			return true;
		}
		
		Player player = (Player) sender;
		if (args.length == 0)
		{
			
		}
		else if (args[0].equalsIgnoreCase("accept"))
		{
			
		}
		else if (args.length > 1 || args[0].equalsIgnoreCase("help"))
		{
			sender.sendMessage(ChatColor.YELLOW + "Usage is: " + ChatColor.GOLD + "/mount [player]");
			sender.sendMessage(ChatColor.GOLD + "Allows you to ride other players or mobs");
		}
		return true;
	}
}
