package com.strangeone101.strangeutil;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager 
{
	public static boolean skullsEnabled = true;
	public static boolean preventFireSpreading = true;
	public static boolean preventFireBlockBurning = true;
	public static float fireDie = 0.35F;
	public static boolean preventIceDamage = true;
	public static String mainWorld = "";
	public static boolean enableWorldSwitching = true;
	public static boolean convertFromOldWorld = false;
	public static String oldWorldLevel = "";
	public static String welcomeMsg = "Welcome back to the new TLA Server! You are currently in the new world but you can teleport back to the old world anytime with /world old";
	public static int maxPrivateWarps = 5;
	public static void load(File file)
	{
		FileConfiguration config = new YamlConfiguration();		
		
		try
		{
			if (file.exists())
			{
				config.load(file);
				skullsEnabled = config.getBoolean("EnableSkulls", skullsEnabled);
				preventFireBlockBurning = config.getBoolean("Fire.PreventBlocksBurning", preventFireBlockBurning);
				preventFireSpreading = config.getBoolean("Fire.PreventSpread", preventFireSpreading);
				fireDie = (float) config.getDouble("Fire.WoodBurnoutTime", fireDie);
				preventIceDamage = config.getBoolean("Ice.PreventSuffocation", preventIceDamage);
				convertFromOldWorld = config.getBoolean("WorldConversion.Enabled", convertFromOldWorld);
				oldWorldLevel = config.getString("WorldConversion.LevelName");
				welcomeMsg = config.getString("WorldConversion.Message", welcomeMsg);
				maxPrivateWarps = config.getInt("PrivateWarps.MaxPerUser", maxPrivateWarps);
			}
			
			config.set("EnableSkulls", skullsEnabled);
			config.set("Fire.PreventBlocksBurning", preventFireBlockBurning);
			config.set("Fire.PreventSpread", preventFireSpreading);
			config.set("Fire.WoodBurnoutTime", fireDie);
			config.set("Ice.PreventSuffocation", preventIceDamage);
			config.set("WorldConversion.Enabled", convertFromOldWorld);
			config.set("WorldConversion.LevelName", oldWorldLevel);
			config.set("WorldConversion.Message", welcomeMsg);
			config.set("PrivateWarps.MaxPerUser", maxPrivateWarps);
			
			config.save(file);
		}
		catch (IOException e)
		{
			StrangeUtil.log.warning("Failed to create config file!");
			e.printStackTrace();
			return;
		} 
		catch (InvalidConfigurationException e) 
		{
			StrangeUtil.log.warning("Config file invalid! Backing up and creating new one.");
			try 
			{
				FileUtils.copyFile(file, new File(StrangeUtil.instance.getDataFolder(), "config.yml.backup"));
			} 
			catch (IOException e1) 
			{
				StrangeUtil.log.warning("Failed to backup file! Please check for permission errors, nothing more will be done.");
				e1.printStackTrace();				
			}
		}
		
		
		
	}
}
