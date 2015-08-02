package com.strangeone101.strangeutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class OldWorldLoader 
{
	protected static List<UUID> players = new ArrayList<UUID>();
	public static String convertingWorld;
	public static boolean ENABLED = false;
	
	public static void save()
	{
		try 
		{
			
			File file = new File(StrangeUtil.instance.getDataFolder(), "updatedplayers.dat");
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			
			BufferedWriter writer  = new BufferedWriter(new FileWriter(file));
			
			for (UUID id : players)
			{
				writer.write(id.toString() + " (" + Bukkit.getOfflinePlayer(id).getName() + ")");
				writer.newLine();
				writer.flush();
			}
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void load()
	{
		try 
		{
			if (ConfigManager.oldWorldLevel.equals("") && ConfigManager.convertFromOldWorld)
			{
				StrangeUtil.log(Level.WARNING, "WorldConversion is enabled but no level name is provided. Nothing will happen.");
				return;
			}
			else if (!ConfigManager.oldWorldLevel.equals("") && ConfigManager.convertFromOldWorld)
			{
				String level = ConfigManager.oldWorldLevel;
				String key = "";
				for (String name : CommandWorld.loadedWorlds.keySet())
				{
					if (CommandWorld.loadedWorlds.get(name).equalsIgnoreCase(level))
					{
						key = name;
						break;
					}
				}
				
				if (key.equals(""))
				{
					StrangeUtil.log(Level.WARNING, "Could not find valid LevelName for WorldConversion! Please check this is correct in your config!");
					return;
				}
				else
				{
					convertingWorld = key;
					ENABLED = true;
				}
			}
			else if (!ConfigManager.convertFromOldWorld)
			{
				return;
			}
			
			File file = new File(StrangeUtil.instance.getDataFolder(), "updatedplayers.dat");
			if (file.exists())
			{
				BufferedReader reader  = new BufferedReader(new FileReader(file));
				
				String s = reader.readLine();
				while (s != null)
				{
					UUID id;
					if (s.contains(" "))
					{
						id = UUID.fromString(s.split(" ")[0]);
					}
					else
					{
						id = UUID.fromString(s);
					}
					players.add(id);
					s = reader.readLine();
				}
				
				reader.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
