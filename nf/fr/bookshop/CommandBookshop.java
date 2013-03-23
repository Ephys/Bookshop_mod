package nf.fr.bookshop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Property;

public class CommandBookshop extends CommandBase
{
	protected Bookshop master;
	private Map<String, String> commandList = new HashMap<String, String>();
	
	public CommandBookshop(Bookshop master) {
		this.master = master;
		this.commandList.put("download", "/bookshop download|dl <id>");
		this.commandList.put("login", "/bookshop login <token> [username]");
		this.commandList.put("upload", "/bookshop upload|up [token] [username]");
		this.commandList.put("list", "/bookshop list [author]");
		this.commandList.put("search", "/bookshop search <title>");
		this.commandList.put("lastest", "/bookshop lastest");
		this.commandList.put("random", "/bookshop random");
		this.commandList.put("top", "/bookshop top|best");
		this.commandList.put("wiki", "/bookshop wiki <page> (/!\\ experimental)");
		this.commandList.put("help", "\u00a72[bookshop] "+master.modTranslation.get("info")+" bookshop.fr.nf");
	}
	
    public String getCommandName()
    {
        return "bookshop";
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
    	EntityPlayer player = getCommandSenderAsPlayer(par1ICommandSender);
    	for(Map.Entry<String, String> e : this.commandList.entrySet()) {
    		player.sendChatToPlayer(e.getValue());
    	}
        return "";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
    @Override
	public boolean canCommandSenderUseCommand(ICommandSender var1) {
		return true;
	}

	@Override
    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
    	EntityPlayerMP player = getCommandSenderAsPlayer(par1ICommandSender);
    	ItemStack inHand = player.getCurrentEquippedItem();
    	if(par2ArrayOfStr.length < 1) {
    		getCommandUsage(par1ICommandSender);
    		return;
    	}
    	
    	// traitement de la commande "dl" || "download"
    	if(par2ArrayOfStr[0].equalsIgnoreCase("dl") || par2ArrayOfStr[0].equalsIgnoreCase("download")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(master.modTranslation.get("syntax")+" : "+this.commandList.get("download"));
    			return;
    		}
    		
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		if(!master.giveBook) {
    			if(inHand == null) {
    				player.sendChatToPlayer(master.modTranslation.get("noBook"));
	    			return;
    			} else if(inHand.itemID != Item.book.itemID && inHand.itemID != Item.writableBook.itemID && inHand.itemID != Item.writtenBook.itemID) {
	    			player.sendChatToPlayer(master.modTranslation.get("noBook"));
	    			return;
    			}
    		}
    		
    		JSONObject book_json = Bookshop_requests.loadBook(par2ArrayOfStr[1]);
    		if(book_json == null) {
    			player.sendChatToPlayer(master.modTranslation.get("notFound"));
    			return;
    		} else {
    			if (book_json.isNull("error")) {
    	    		try {
    	    			List bookPages = new ArrayList();
    	    			String bookAuthor = book_json.getString("author");
    	    			String bookTitle = book_json.getString("title");
    	    			JSONArray ps = book_json.getJSONArray("pages");

    	    			if(ps.length() == 0) {
    	    				player.sendChatToPlayer(master.modTranslation.get("notFound"));
    	    			} else {
    		    			for (int i = 0; i < ps.length(); i++)
    		    			{
    							bookPages.add(ps.getString(i));
    		    			}
    		    			
    		    			if(!master.giveBook)
    		    				Bookshop_requests.createBook(bookTitle, bookPages, bookAuthor, inHand, player);
    		    			else
    		    				createBook(bookTitle, bookPages, bookAuthor, player);
    		    			 player.sendChatToPlayer(master.modTranslation.get("loaded"));
    	    			}
    	    		}
    	    		catch (JSONException e) { 
    	    			player.sendChatToPlayer(master.modTranslation.get("notFound"));
    	    		}
    			} else {
    	    		try {
    	    			player.sendChatToPlayer(book_json.getString("error"));
    	    		}
    	    		catch (JSONException e) { }
    	    		return;
    	    	}	
    		}
    	}
    	// connection
    	else if(par2ArrayOfStr[0].equalsIgnoreCase("login")) {
    		storePass(par2ArrayOfStr, player);
    	}
    	// traitement de la commande "up" || "upload"
    	else if(par2ArrayOfStr[0].equalsIgnoreCase("up") || par2ArrayOfStr[0].equalsIgnoreCase("upload")) {
    		if(inHand == null) {
				player.sendChatToPlayer(master.modTranslation.get("noBook"));
    			return;
			} else if(inHand.itemID != Item.book.itemID && inHand.itemID != Item.writableBook.itemID && inHand.itemID != Item.writtenBook.itemID) {
    			player.sendChatToPlayer(master.modTranslation.get("noBook"));
    			return;
			}
    		
    		try {
	    		JSONObject upload_result;
	    		if(par2ArrayOfStr.length == 1)
	    			upload_result = uploadBook(inHand);
	    		else if(par2ArrayOfStr.length == 2)
	    			upload_result = Bookshop_requests.uploadBook(player, inHand, par2ArrayOfStr[1]);
	    		else
	    			upload_result = Bookshop_requests.uploadBook(inHand, par2ArrayOfStr[1], par2ArrayOfStr[2]);
	    		
	    		if(upload_result == null)
	    			player.sendChatToPlayer(master.modTranslation.get("requestError"));
	    		else if (upload_result.isNull("error"))
	    			player.sendChatToPlayer(master.modTranslation.get("uploadComplete")+" " + upload_result.getString("success"));
	    		else {
	    			player.sendChatToPlayer(upload_result.getString("error"));
	    		}
    		} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	// traitement "list"
    	else if(par2ArrayOfStr[0].equalsIgnoreCase("list")) {
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		if(par2ArrayOfStr.length != 2)
    			parseBookList_user(player, Bookshop_requests.loadUserBooks(player.username));
    		else
    			parseBookList_user(player, Bookshop_requests.loadUserBooks(par2ArrayOfStr[1]));
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("search")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(master.modTranslation.get("syntax")+" : "+this.commandList.get("search"));
    			return;
    		}
    		
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		parseBookList_title(player, Bookshop_requests.loadTitleList(par2ArrayOfStr[1]));
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("lastest")) {
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		parseBookList_general(player, Bookshop_requests.loadBookList("lastest"));
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("random")) {
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		parseBookList_general(player, Bookshop_requests.loadBookList("random"));
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("top") || par2ArrayOfStr[0].equalsIgnoreCase("best")) {
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
    		parseBookList_general(player, Bookshop_requests.loadBookList("best"));
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("wiki")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(master.modTranslation.get("syntax")+" : "+this.commandList.get("wiki"));
    			return;
    		}
    		
    		player.sendChatToPlayer(master.modTranslation.get("loading"));
   
    		JSONObject book_json = Bookshop_requests.loadWikiBook(par2ArrayOfStr[1]);
    		//System.out.println(book_json);
    		if(book_json == null) {
    			player.sendChatToPlayer(master.modTranslation.get("notFound"));
    			return;
    		} else {
	    		try {
	    			List bookPages = new ArrayList();
	    			String bookTitle = book_json.getString("title");
	    			JSONArray ps = book_json.getJSONArray("pages");
	    			
	    			if(ps.length() == 0) {
	    				player.sendChatToPlayer(master.modTranslation.get("notFound"));
	    			} else {
		    			for (int i = 0; i < ps.length(); i++)
		    			{
							bookPages.add(ps.getString(i));
		    			}
		    			
		    			if(!master.giveBook)
		    				Bookshop_requests.createBook(bookTitle, bookPages, "Minecraftwiki.net", inHand, player);
		    			else
		    				createBook(bookTitle, bookPages, "Minecraftwiki.net", player);
		    			
		    			 player.sendChatToPlayer(master.modTranslation.get("loaded"));
	    			}
	    		}
	    		catch (JSONException e) { 
	    			player.sendChatToPlayer(master.modTranslation.get("notFound"));
	    		}
    		}
    	} else {
    		getCommandUsage(par1ICommandSender);
    	}
    }
    
    protected void storePass(String[] cmd, EntityPlayer player) {
    	if(player.worldObj.isRemote) {
    		player.sendChatToPlayer(master.modTranslation.get("denied"));
    		return;
    	}
    	if(cmd.length < 2) {
    		player.sendChatToPlayer(master.modTranslation.get("syntax")+" : "+this.commandList.get("login"));
			return;
		}
		
		String pass = cmd[1];
		String login = "";
		
		if(cmd.length == 2) {
			login = player.username;
		} else {
			login = cmd[2];
		}
		
		master.storePass(login, pass);
    }
    
    private JSONObject uploadBook(ItemStack inHand) {
		return Bookshop_requests.uploadBook(inHand, master.bookshop_token, master.bookshop_login);
	}
    
    // version on /give le book
    private static void createBook(final String title, final List pagesArray, final String author, final EntityPlayer player) {
    	new Thread() {
	      public void run() {
	        //String s = "BS|BSign";
	    	  String s = "ephys.bookshop";
	        ItemStack itemstackBook = new ItemStack(Item.writtenBook, 1);
	        NBTTagCompound nbttagcompound = itemstackBook.getTagCompound();
	        
	        NBTTagList pages_tag = new NBTTagList("pages");
	        for (int i = 0; i < pagesArray.size(); i++) {
	        	pages_tag.appendTag(new NBTTagString(i + 1 + "", ((String)pagesArray.get(i)).replaceAll("\r", "")));
	        }

	        itemstackBook.setTagInfo("author", new NBTTagString("author", author.trim()));
	        itemstackBook.setTagInfo("title", new NBTTagString("title", title.trim()));
	        itemstackBook.setTagInfo("pages", pages_tag);
	        player.dropPlayerItem(itemstackBook);

	        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
	        try 
	        {
                Packet.writeItemStack(itemstackBook, dataoutputstream);
                //FMLCommonHandler.instance().getSidedDelegate().sendPacket(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
	    		  Side side = FMLCommonHandler.instance().getEffectiveSide();
	              if (side == Side.CLIENT) {
	            	  EntityClientPlayerMP playerMP = (EntityClientPlayerMP) player;
	            	  playerMP.sendQueue.addToSendQueue(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
	              }
	        }
	        catch (Exception exception)
	        {
	        	exception.printStackTrace();
	        }
	      }
	    }
	    .start();
    }
    
    private void parseBookList_user(EntityPlayer player, JSONObject json_data) {
    	try {
			if(json_data.isNull("error")) {
				List<String> keys = Bookshop_requests.getJSONListKeys(json_data);
				
			    if(keys.size() < 3)
			    	player.sendChatToPlayer("["+json_data.getString("author")+"] "+master.modTranslation.get("bookListEmpty"));
			    else {
			    	player.sendChatToPlayer(master.modTranslation.get("bookList"));
				    for(String key : keys) {
				    	if(key.equalsIgnoreCase("author") || key.equalsIgnoreCase("type"))
				    		continue;
	
				    	JSONObject book = json_data.getJSONObject(key);
				    	player.sendChatToPlayer("- "+book.getString("title") + " (" + book.getString("id") + ")");
				    }
			    }
			} else {
				player.sendChatToPlayer(json_data.getString("error"));
			}
    	} catch(Exception e) {
    		player.sendChatToPlayer(master.modTranslation.get("requestError"));
    	}
    }
    
    private void parseBookList_general(EntityPlayer player, JSONObject json_data) {
    	try {
			if(json_data.isNull("error")) {
				List<String> keys = Bookshop_requests.getJSONListKeys(json_data);
				
			    if(keys.size() < 2)
			    	player.sendChatToPlayer(master.modTranslation.get("bookListEmpty"));
			    else {
			    	player.sendChatToPlayer(master.modTranslation.get("bookList"));
				    for(String key : keys) {
				    	if(key.equalsIgnoreCase("type"))
				    		continue;
				    	JSONObject book = json_data.getJSONObject(key);
				    	player.sendChatToPlayer("- "+book.getString("title") + " (" + book.getString("id") + ")");
				    }	
			    }
			} else {
				player.sendChatToPlayer(json_data.getString("error"));
			}
		} catch (JSONException e) {
			player.sendChatToPlayer(master.modTranslation.get("requestError"));
		}
    }
    
    private void parseBookList_title(EntityPlayer player, JSONObject json_data) {
		try {
			if(json_data.isNull("error")) {
				List<String> keys = Bookshop_requests.getJSONListKeys(json_data);
				
			    if(keys.size() < 3)
			    	player.sendChatToPlayer(master.modTranslation.get("searchListEmpty"));
			    else {
			    	player.sendChatToPlayer(master.modTranslation.get("titleList")+" '"+json_data.getString("title")+"'");
				    for(String key : keys){
				    	if(key.equalsIgnoreCase("title") || key.equalsIgnoreCase("type"))
				    		continue;
				    	
				    	JSONObject book = json_data.getJSONObject(key);
				    	player.sendChatToPlayer("- "+book.getString("title") + " (" + book.getString("id") + ")");
				    }
			    }
			} else {
				player.sendChatToPlayer(json_data.getString("error"));
			}
		} catch (JSONException e) {
			player.sendChatToPlayer(master.modTranslation.get("requestError"));
		}
    }
}

