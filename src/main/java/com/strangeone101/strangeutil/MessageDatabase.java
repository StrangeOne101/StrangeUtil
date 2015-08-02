package com.strangeone101.strangeutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class MessageDatabase 
{
	public HashMap<UUID, List<MsgData>> data;
	public HashMap<UUID, UUID> lastPlayerMsg = new HashMap<UUID, UUID>();
	
	@SuppressWarnings("deprecation")
	public void load()
	{
		try
		{
			data = new HashMap<UUID, List<MsgData>>();
			File file = new File(StrangeUtil.instance.getDataFolder(), "msgdb.dat");
			if (!file.exists())
			{
				file.createNewFile();
			}
			BufferedReader reader;
		
			reader = new BufferedReader(new FileReader(file));
		
			String s;
		
			s = reader.readLine();
			while (s != null)
			{
				final String from = s.split(" ")[0];
				CommandSender sender;
				if (from.equals("CONSOLE"))
				{
					sender = Bukkit.getConsoleSender();
				}
				else if (Bukkit.getPlayer(from) != null)
				{
					sender = Bukkit.getPlayer(from);
				}
				else
				{
					sender = new CommandSender() {

						public PermissionAttachment addAttachment(Plugin arg0) {return null;}

						public PermissionAttachment addAttachment(Plugin arg0,int arg1) {return null;}

						public PermissionAttachment addAttachment(Plugin arg0,String arg1, boolean arg2) {return null;}

						public PermissionAttachment addAttachment(Plugin arg0,String arg1, boolean arg2, int arg3) {return null;}

						public Set<PermissionAttachmentInfo> getEffectivePermissions() {return null;}

						public boolean hasPermission(String arg0) {return false;}

						public boolean hasPermission(Permission arg0) {return false;}

						public boolean isPermissionSet(String arg0) {return false;}

						public boolean isPermissionSet(Permission arg0) {return false;}

						public void recalculatePermissions() {}

						public void removeAttachment(PermissionAttachment arg0) {}

						public boolean isOp() {return false;}

						public void setOp(boolean arg0) {}

						public String getName() {return from;}

						public Server getServer() {return null;}

						public void sendMessage(String arg0) 
						{
							try 
							{
								throw new Exception("Cannot call send message to dummy sender!");
							} 
							catch (Exception e) {e.printStackTrace();}
						}

						public void sendMessage(String[] arg0) 
						{
							this.sendMessage("");
						}
					};
				}
				String uuid2 = s.split(" ")[1];
				long time = Long.parseLong(s.split(" ")[2]);
				String msg = s.split(" ", 4)[3];
			
				MsgData data = new MsgData(sender, Bukkit.getPlayer(UUID.fromString(uuid2)), time, msg);
				getMsgForPlayer(UUID.fromString(uuid2)).add(data);
			
				s = reader.readLine();
			}
			reader.close();
		}
		catch (IOException e) {e.printStackTrace();}
		
	}	
	
	public void save()
	{
		try
		{
			File file = new File(StrangeUtil.instance.getDataFolder(), "msgdb.dat");
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (UUID uuid : data.keySet())
			{
				for (MsgData d : data.get(uuid))
				{
					writer.write(d.from.getName() + " " + 
				d.to.getUniqueId().toString() + " " + 
							d.timestamp + " " + 
				d.msg);
					writer.newLine();
					writer.flush();
				}
			}
			writer.close();
		}
		catch (FileNotFoundException e) {} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public class MsgData
	{
		protected CommandSender from;
		protected OfflinePlayer to;
		protected long timestamp;
		protected String msg;
		protected boolean read = false;

		public MsgData(CommandSender from, OfflinePlayer to, long timestamp, String msg) 
		{
			this.from = from;
			this.to = to;
			this.timestamp = timestamp;
			this.msg = msg;
		}
	}
	
	public List<MsgData> getMsgForPlayer(UUID playeruuid)
	{
		if (!data.containsKey(playeruuid) || data.get(playeruuid) == null)
		{
			data.put(playeruuid, new ArrayList<MsgData>());
		}
		return data.get(playeruuid);
	}
	
	public void addMessage(CommandSender from, OfflinePlayer to, long timestamp, String msg)
	{
		MsgData data = new MsgData(from, to, timestamp, msg);
		this.getMsgForPlayer(to.getUniqueId()).add(data);
	}
}
