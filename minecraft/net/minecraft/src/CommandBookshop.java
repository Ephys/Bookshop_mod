package net.minecraft.src;

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

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class CommandBookshop extends CommandBase
{
	private boolean giveBook = false;
	private mod_bookshop bookshop;
	static private String API_URL;
	static private String WIKI_API_URL;
	private Map<String, String> commandList = new HashMap<String, String>();
	
	public CommandBookshop(mod_bookshop bookshop) {
		this.bookshop = bookshop;
		this.API_URL = bookshop.API_URL;
		this.WIKI_API_URL = bookshop.getStringProp("MCWiki.API.URL");
		this.giveBook = bookshop.getBooleanProp("download.giveBook");
		
		this.commandList.put("download", "/bookshop download|dl <id>");
		this.commandList.put("upload", "/bookshop upload|up <token> [username]");
		this.commandList.put("list", "/bookshop list [author]");
		this.commandList.put("search", "/bookshop search <title>");
		this.commandList.put("lastest", "/bookshop lastest");
		this.commandList.put("random", "/bookshop random");
		this.commandList.put("top", "/bookshop top|best");
		this.commandList.put("wiki", "/bookshop wiki <page> (/!\\ experimental)");
		this.commandList.put("help", "\u00a72[bookshop] "+this.bookshop.getTranslation("language.info")+" bookshop.fr.nf");
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

    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
    	EntityPlayer player = getCommandSenderAsPlayer(par1ICommandSender);
    	ItemStack inHand = player.getCurrentEquippedItem();
    	if(par2ArrayOfStr.length < 1) {
    		getCommandUsage(par1ICommandSender);
    		return;
    	}
    	
    	// traitement de la commande "dl" || "download"
    	if(par2ArrayOfStr[0].equalsIgnoreCase("dl") || par2ArrayOfStr[0].equalsIgnoreCase("download")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(this.bookshop.getTranslation("language.syntax")+" : "+this.commandList.get("download"));
    			return;
    		}
    		
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		if(!this.giveBook) {
    			if(inHand == null) {
    				player.sendChatToPlayer(this.bookshop.getTranslation("language.error.noBook"));
	    			return;
    			} else if(inHand.itemID != Item.book.shiftedIndex && inHand.itemID != Item.writableBook.shiftedIndex && inHand.itemID != Item.writtenBook.shiftedIndex) {
	    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.noBook"));
	    			return;
    			}
    		}
    		
    		JSONObject book_json = loadBook(par2ArrayOfStr[1]);
    		if(book_json == null) {
    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
    			return;
    		} else {
    			if (book_json.isNull("error")) {
    	    		try {
    	    			List bookPages = new ArrayList();
    	    			String bookAuthor = book_json.getString("author");
    	    			String bookTitle = book_json.getString("title");
    	    			JSONArray ps = book_json.getJSONArray("pages");

    	    			if(ps.length() == 0) {
    	    				player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
    	    			} else {
    		    			for (int i = 0; i < ps.length(); i++)
    		    			{
    							bookPages.add(ps.getString(i));
    		    			}
    		    			
    		    			if(!this.giveBook)
    		    				createBook(bookTitle, bookPages, bookAuthor, inHand);
    		    			else
    		    				createBook(bookTitle, bookPages, bookAuthor, player);
    		    			 player.sendChatToPlayer(bookshop.getTranslation("language.loaded"));
    	    			}
    	    		}
    	    		catch (JSONException e) { 
    	    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
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
    	// traitement de la commande "up" || "upload"
    	else if(par2ArrayOfStr[0].equalsIgnoreCase("up") || par2ArrayOfStr[0].equalsIgnoreCase("upload")) {
    		if(inHand == null) {
				player.sendChatToPlayer(this.bookshop.getTranslation("language.error.noBook"));
    			return;
			} else if(inHand.itemID != Item.book.shiftedIndex && inHand.itemID != Item.writableBook.shiftedIndex && inHand.itemID != Item.writtenBook.shiftedIndex) {
    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.noBook"));
    			return;
			}
    		
    		if(par2ArrayOfStr.length < 2) {
    			par1ICommandSender.sendChatToPlayer(this.bookshop.getTranslation("language.syntax")+" : "+this.commandList.get("upload"));
    			return;
    		}
    		par1ICommandSender.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		if(par2ArrayOfStr.length == 2)
    			uploadBook(player, inHand, par2ArrayOfStr[1]);
    		else
    			uploadBook(player, inHand, par2ArrayOfStr[1], par2ArrayOfStr[2], this.bookshop);
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("list")) {
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		if(par2ArrayOfStr.length != 2)
    			loadUserBooks(player, player.username);
    		else
    			loadUserBooks(player, par2ArrayOfStr[1]);
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("search")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(this.bookshop.getTranslation("language.syntax")+" : "+this.commandList.get("search"));
    			return;
    		}
    		
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		searchTitle(player, par2ArrayOfStr[1]);
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("lastest")) {
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		loadBookList(player, "lastest");
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("random")) {
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		loadBookList(player, "random");
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("top") || par2ArrayOfStr[0].equalsIgnoreCase("best")) {
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
    		loadBookList(player, "best");
    	} else if(par2ArrayOfStr[0].equalsIgnoreCase("wiki")) {
    		if(par2ArrayOfStr.length != 2) {
    			par1ICommandSender.sendChatToPlayer(this.bookshop.getTranslation("language.syntax")+" : "+this.commandList.get("wiki"));
    			return;
    		}
    		
    		player.sendChatToPlayer(this.bookshop.getTranslation("language.loading"));
   
    		JSONObject book_json = loadWikiBook(par2ArrayOfStr[1]);
    		//System.out.println(book_json);
    		if(book_json == null) {
    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
    			return;
    		} else {
	    		try {
	    			List bookPages = new ArrayList();
	    			String bookTitle = book_json.getString("title");
	    			JSONArray ps = book_json.getJSONArray("pages");
	    			
	    			if(ps.length() == 0) {
	    				player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
	    			} else {
		    			for (int i = 0; i < ps.length(); i++)
		    			{
							bookPages.add(ps.getString(i));
		    			}
		    			
		    			if(!this.giveBook)
		    				createBook(bookTitle, bookPages, "Minecraftwiki.net", inHand);
		    			else
		    				createBook(bookTitle, bookPages, "Minecraftwiki.net", player);
		    			
		    			 player.sendChatToPlayer(this.bookshop.getTranslation("language.loaded"));
	    			}
	    		}
	    		catch (JSONException e) { 
	    			player.sendChatToPlayer(this.bookshop.getTranslation("language.error.notFound"));
	    		}
    		}
    	} else {
    		getCommandUsage(par1ICommandSender);
    	}
    }
    
    private JSONObject loadWikiBook(final String pageName) {
    	String request_url = this.WIKI_API_URL+"?redirects=true&format=json&action=parse&page="+pageName+"&prop=wikitext";

    	try {
    		InputStream is = new URL(request_url).openStream();
    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
    		return parseWikiBook(new JSONObject(br.readLine()));
        } catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    private JSONObject parseWikiBook(JSONObject json) {
    	JSONObject output = new JSONObject();
    	String pages;
    	ArrayList pagesArray = new ArrayList();
    	
		try {
			pages = json.getJSONObject("parse").getJSONObject("wikitext").getString("*");
			
			// parsage des pages
			String usualChars = "[\\w#\\?\\&_\\- \\(\\)\"']";
			
			pages = pages.replaceAll("\\[\\[("+usualChars+"*?):("+usualChars+"*?)\\|(["+usualChars+"]*?)\\]\\]", "$3");
			pages = pages.replaceAll("\\[\\[("+usualChars+"*?):("+usualChars+"*?)\\]\\]", "$2");
			pages = pages.replaceAll("\\[\\[("+usualChars+"*?)\\|("+usualChars+"*?)\\]\\]", "$2");
	    	pages = pages.replaceAll("\\[\\[(.*?)\\]\\]", "$1");
	    	
	    	pages = pages.replaceAll("\\{\\{("+usualChars+"*?)\\|("+usualChars+"*?)\\|("+usualChars+"*?)\\|("+usualChars+"*?)\\}\\}", "$2");
	    	pages = pages.replaceAll("\\{\\{("+usualChars+"*?)\\|\\|("+usualChars+"*?)\\|("+usualChars+"*?)\\}\\}", "$2 : $3");
	    	pages = pages.replaceAll("\\{\\{("+usualChars+"*?)\\|("+usualChars+"*?)\\|("+usualChars+"*?)\\}\\}", "$2");
	    	pages = pages.replaceAll("\\{\\{("+usualChars+"*?)\\|("+usualChars+"*?)\\}\\}", "$2");
	    	pages = pages.replaceAll("(?s)\\{\\{(.*?)\\}\\}", "");
	    	
	    	pages = pages.replaceAll("(?s)\\{\\|(.*?)\\|\\}", "");
	    	pages = pages.replaceAll("'''(.*?)'''", "\\§o$1\\§r");
	    	pages = pages.replaceAll("''(.*?)''", "$1");
	    	pages = pages.replaceAll("'(.*?)'", "$1");
	    	pages = pages.replaceAll("(?s)\n{2,}", "\n");
	    	pages = pages.replaceAll("(?s)==([ "+usualChars+"]*?)==([ \t\r\n]*?)==([ "+usualChars+"]*?)==", "== $3 ==");
	    	pages = pages.replaceAll("== ("+usualChars+"*?) ==", "\\§l== $1 ==\\§r");
	    	pages = pages.replaceAll("(?s)<[^>]*>(.+?)<\\/[^>]*>", "");
	    	pages = pages.replaceAll("(?s)<[^>]*>", "");
			
			pagesArray = split_string_to_mcpages(pages);
			
			output.put("title", json.getJSONObject("parse").getString("title"));
			output.put("pages", pagesArray);
			
			return output;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
    }
    
    private ArrayList split_string_to_mcpages(String pages) {
		ArrayList output = new ArrayList();
		String[] pagesArray = pages.split("\n");
		
    	int MAXLINES = 13;
    	int MAXCHAR = 220;
    	
    	int nCounter = 0;
		String formating_page = "";
		
		for(int i = 0; i < pagesArray.length; i++) {
    		// si on dépasse le nombre de lignes maximales
    		// si on dépasse le nombre de caractères maximaux
    		// si la prochaine page est un titre:
    		// --> enregistrer la page actuelle et démarer la suivante
			if(nCounter > MAXLINES || (formating_page.length() >= MAXCHAR) || pagesArray[i].startsWith("==")) {
				String nextSentence = "";
				if(formating_page.replaceAll("(?s)[ \n\t\r]", "").length() != 0 && formating_page.length() < MAXCHAR) {
					output.add(formating_page);
				} else if(formating_page.length() > MAXCHAR) { // découper en chaines de 250 caractères et séparer par les espaces
					String[] words = formating_page.split(" ");
					String newSentence = "";
					Boolean isOverflowed= false;
					for(int j = 0; j < words.length; j++) {
						if(newSentence.length()+words[j].length() > MAXCHAR)
							isOverflowed = true;
						
						if(isOverflowed) {
							nextSentence += words[j]+" ";
						} else {
							newSentence += words[j]+" ";
						}
					}
					output.add(newSentence);
				}
				
				nCounter = 0;
				formating_page = nextSentence;
			}
			if(formating_page.length() < MAXCHAR) {
				formating_page = formating_page.concat(pagesArray[i]+"\n");
				nCounter++;
			}
    	}
		return output;
    }

	private JSONObject loadBook(final String id) {
    	String request_url = API_URL+"api.php?id="+id;

    	if(Integer.parseInt(id) < 1)
    		return null;
    	
    	try
        {
    		InputStream is = new URL(request_url).openStream();

    		BufferedReader br = new BufferedReader(new InputStreamReader(is));

    		String l = br.readLine();

    		return new JSONObject(l);
        }
		catch (Exception e)
		{
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    // version on modifie le contenu du livre
    private void createBook(final String title, final List pagesArray, final String author, final ItemStack inHand) {
    	new Thread() {
	      public void run() {
	    	  Minecraft mc = Minecraft.getMinecraft();
	    	  String s = "MC|BEdit";
	    	  if(inHand.itemID == Item.book.shiftedIndex) {
	    		  inHand.itemID = Item.writtenBook.shiftedIndex;
	    	  } else if(inHand.itemID != Item.writableBook.shiftedIndex && inHand.itemID != Item.writtenBook.shiftedIndex) {
	    		  return;
	    	  }
	    	  
	    	  NBTTagCompound nbttagcompound = inHand.getTagCompound();
	        
	    	  NBTTagList pages_tag = new NBTTagList("pages");
	    	  for (int i = 0; i < pagesArray.size(); i++) {
	    		  pages_tag.appendTag(new NBTTagString(i + 1 + "", ((String)pagesArray.get(i)).replaceAll("\r", "")));
	    	  }

	    	  inHand.func_77983_a("pages", pages_tag);
	    	  if(inHand.itemID == Item.writtenBook.shiftedIndex) {
	    		  s = "MC|BSign";
	    		  inHand.func_77983_a("author", new NBTTagString("author", author.trim()));
	    		  inHand.func_77983_a("title", new NBTTagString("title", title.trim()));
	    	  }

	    	  ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	    	  DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
	    	  try
	    	  {
	    		  Packet.writeItemStack(inHand, dataoutputstream);
	    		  mc.getSendQueue().addToSendQueue(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
	    	  }
	    	  catch (Exception exception)
	    	  {
	    		  exception.printStackTrace();
	    	  }
	      }
	    }
	    .start();
    }
    
    // version on /give le book
    private void createBook(final String title, final List pagesArray, final String author, final EntityPlayer player) {
    	new Thread() {
	      public void run() {
	        String s = "MC|BSign";
	        Minecraft mc = Minecraft.getMinecraft();
	        ItemStack itemstackBook = new ItemStack(Item.writtenBook, 1);
	        NBTTagCompound nbttagcompound = itemstackBook.getTagCompound();
	        
	        NBTTagList pages_tag = new NBTTagList("pages");
	        for (int i = 0; i < pagesArray.size(); i++) {
	        	pages_tag.appendTag(new NBTTagString(i + 1 + "", ((String)pagesArray.get(i)).replaceAll("\r", "")));
	        }

	        itemstackBook.func_77983_a("author", new NBTTagString("author", author.trim()));
	        itemstackBook.func_77983_a("title", new NBTTagString("title", title.trim()));
	        itemstackBook.func_77983_a("pages", pages_tag);
	        player.dropPlayerItem(itemstackBook);

	        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
	        try
	        {
                Packet.writeItemStack(itemstackBook, dataoutputstream);
                mc.getSendQueue().addToSendQueue(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
	        }
	        catch (Exception exception)
	        {
	        	exception.printStackTrace();
	        }
	      }
	    }
	    .start();
    }
    
    private void uploadBook(EntityPlayer player, ItemStack inHand, final String token) {
		uploadBook(player, inHand, token, player.username, this.bookshop);
	}
    
    private void uploadBook(final EntityPlayer player, final ItemStack book, final String token, final String username, final mod_bookshop bookshop) {
	    new Thread()
	    {
	      public void run() {
	        NBTTagCompound nbttagcompound = book.getTagCompound();
	        NBTTagList tag_pages = nbttagcompound.getTagList("pages");
	        int page_number = 0;
	        if (tag_pages != null) {
	          tag_pages = (NBTTagList)tag_pages.copy();
	          page_number = tag_pages.tagCount();
	
	          if (page_number < 1) {
	        	  page_number = 0;
	          }
	        }
	
	        ArrayList list = new ArrayList();
	        for (int i = 0; i < tag_pages.tagCount(); i++) {
	        	NBTTagString page = (NBTTagString)tag_pages.tagAt(i);
	        	list.add(page.toString());
	        }
	
	        try {
	          JSONObject jso = new JSONObject();
	          jso.put("pages", list);
	          if(book.itemID == Item.writtenBook.shiftedIndex) {
	        	  jso.put("title", nbttagcompound.getString("title"));
	        	  jso.put("author", nbttagcompound.getString("author"));
	          } else if(book.itemID == Item.writableBook.shiftedIndex) {
	        	  jso.put("title", "");
	        	  jso.put("author", player.username);
	          } else {
	        	  return;
	          }
	
	          String jsonEncodedBook = jso.toString();
	          jsonEncodedBook = URLEncoder.encode(jsonEncodedBook, "UTF-8");
	
	          String url = API_URL+"upload.php?username=" + username + "&token=" + token + "&data=" + jsonEncodedBook;
	          
	          InputStream is = new URL(url).openStream();
	
	          BufferedReader br = new BufferedReader(new InputStreamReader(is));
	
	          String retour = br.readLine();
	
	          JSONObject json_data = new JSONObject(retour);
	          String messageR;
	          if (json_data.isNull("error"))
	            messageR = bookshop.getTranslation("language.uploadComplete")+" " + json_data.getString("success");
	          else {
	            messageR = json_data.getString("error");
	          }
	          player.sendChatToPlayer(messageR);
	        }
	        catch (JSONException e) {
	          e.printStackTrace();
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	      }
	    }
	    .start();
	}
    
    private void loadUserBooks(EntityPlayer player, String username) {
		try {
			JSONObject json_data = apiRequest(player, "api.php?user=" + username);
			if(json_data.isNull("error")) {
				List<String> keys = getJSONListKeys(json_data);
				
			    if(keys.size() < 3) // key 0 = author, key 1 = type
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.error.bookListEmpty"));
			    else {
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.bookList"));
				    for(String key : keys){
				    	if(key.equalsIgnoreCase("author") || key.equalsIgnoreCase("type"))
				    		continue;

				    	JSONObject book = json_data.getJSONObject(key);
				    	player.sendChatToPlayer("- "+book.getString("title") + " (" + book.getString("id") + ")");
				    }	
			    }
			} else {
				player.sendChatToPlayer(json_data.getString("error"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private void loadBookList(EntityPlayer player, final String listType) {
		try {
			JSONObject json_data = apiRequest(player, "api.php?"+listType+"=true");
			
			if(json_data.isNull("error")) {
				List<String> keys = getJSONListKeys(json_data);
				
			    if(keys.size() < 2)
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.error.bookListEmpty"));
			    else {
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.bookList"));
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
			e.printStackTrace();
		}
    }
    
    private void searchTitle(EntityPlayer player, String pattern) {
		try {
			JSONObject json_data = apiRequest(player, "api.php?title=" + pattern);
			
			if(json_data.isNull("error")) {
				List<String> keys = getJSONListKeys(json_data);
				
			    if(keys.size() < 3)
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.error.titleListEmpty"));
			    else {
			    	player.sendChatToPlayer(this.bookshop.getTranslation("language.titleList")+" '"+pattern+"'");
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
			e.printStackTrace();
		}
    }
    
    public List<String> getJSONListKeys(JSONObject jsonList) {
    	Iterator<String> myIter = jsonList.keys();
		List<String> keys = new ArrayList<String>();

	    while(myIter.hasNext()){
	    	keys.add(myIter.next());
	    }
	    
	    Collections.sort(keys);
	    
	    return keys;
    }
    
    public JSONObject apiRequest(EntityPlayer player, String request) {
    	String url = API_URL+request;
    	InputStream is;
 		try {
 			is = new URL(url).openStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 	        JSONObject json_data;
 			try {
 				return new JSONObject(br.readLine());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		player.sendChatToPlayer(this.bookshop.getTranslation("language.error.request"));
		return null;
    }
}
