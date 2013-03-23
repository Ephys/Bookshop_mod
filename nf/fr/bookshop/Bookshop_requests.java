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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import org.json.JSONException;
import org.json.JSONObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Bookshop_requests {
    public static JSONObject loadWikiBook(final String pageName) {
    	String request_url = Bookshop.wikiApi_url+"?redirects=true&format=json&action=parse&page="+pageName+"&prop=wikitext";

    	try {
    		InputStream is = new URL(request_url).openStream();
    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
    		return parseWikiBook(new JSONObject(br.readLine()));
        } catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    public static JSONObject parseWikiBook(JSONObject json) {
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
    
    public static ArrayList split_string_to_mcpages(String pages) {
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

	public static JSONObject loadBook(final String id) {
    	String request_url = Bookshop.mainApi_url+"api.php?id="+id;

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
	public static void createBook(final String title, final List pagesArray, final String author, final ItemStack inHand, final EntityPlayer playerEntity) {
		int[] boxPos = {0,0,0};
		createBook(title, pagesArray, author, inHand, playerEntity, boxPos);
	}
	
    public static void createBook(final String title, final List pagesArray, final String author, final ItemStack inHand, final EntityPlayer playerEntity, final int[] boxPos) {
    	new Thread() {
	      public void run() {
	    	 // String s = "BS|BEdit";
	    	  String s = "ephys.bookshop";
	    	  if(inHand.itemID == Item.book.itemID) {
	    		  inHand.itemID = Item.writtenBook.itemID;
	    	  } else if(inHand.itemID != Item.writableBook.itemID && inHand.itemID != Item.writtenBook.itemID) {
	    		  return;
	    	  }
	    	  
	    	  NBTTagCompound nbttagcompound = inHand.getTagCompound();
	        
	    	  NBTTagList pages_tag = new NBTTagList("pages");
	    	  for (int i = 0; i < pagesArray.size(); i++) {
	    		  pages_tag.appendTag(new NBTTagString(i + 1 + "", ((String)pagesArray.get(i)).replaceAll("\r", "")));
	    	  }

	    	  inHand.setTagInfo("pages", pages_tag);
	    	  if(inHand.itemID == Item.writtenBook.itemID) {
	    		  //s = "BS|BSign";
	    		  inHand.setTagInfo("author", new NBTTagString("author", author.trim()));
	    		  inHand.setTagInfo("title", new NBTTagString("title", title.trim()));
	    	  }
	    	  
	    	  Side side = FMLCommonHandler.instance().getEffectiveSide();
	    	  if (side == Side.CLIENT) {
		    	  ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		    	  DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
		    	  try
		    	  {
		    		  //System.out.println("Sending: "+boxPos[0]);
		    		  inHand.stackTagCompound.setInteger("box_x", boxPos[0]);
		    		  inHand.stackTagCompound.setInteger("box_y", boxPos[1]);
		    		  inHand.stackTagCompound.setInteger("box_z", boxPos[2]);
		    		  Packet.writeItemStack(inHand, dataoutputstream);
		    		  //FMLCommonHandler.instance().getSidedDelegate().sendPacket(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
		    		  
		              
		            	  EntityClientPlayerMP player = (EntityClientPlayerMP) playerEntity;
		            	  player.sendQueue.addToSendQueue(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
	              } catch (Exception exception) {
	            	  exception.printStackTrace();
	              }
	    	  }
	    	  
	      }
	    }
	    .start();
    }
    
    public static JSONObject uploadBook(EntityPlayer player, ItemStack inHand, final String token) {
		return uploadBook(inHand, token, player.username);
	}
    
    public static JSONObject uploadBook(final ItemStack book, final String token, final String username) {
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
			if(book.itemID == Item.writtenBook.itemID) {
				jso.put("title", nbttagcompound.getString("title"));
				jso.put("author", nbttagcompound.getString("author"));
			} else if(book.itemID == Item.writableBook.itemID) {
				jso.put("title", "");
				jso.put("author", username);
			} else {
				return null;
			}

			String jsonEncodedBook = jso.toString();
			jsonEncodedBook = URLEncoder.encode(jsonEncodedBook, "UTF-8");

			String url = Bookshop.mainApi_url+"upload.php?username=" + username + "&token=" + token + "&data=" + jsonEncodedBook;
      
			InputStream is = new URL(url).openStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String retour = br.readLine();

			JSONObject json_data = new JSONObject(retour);
			return json_data;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public static JSONObject loadUserBooks(final String username) {
		return apiRequest("public.php?user=" + username);
    }
    
    public static JSONObject loadBookList(final String listType) {
		return apiRequest("api.php?"+listType+"=true");
    }
    
    public static JSONObject loadTitleList(final String pattern) {
    	return apiRequest("api.php?title=" + pattern);
    }
    
    public static List<String> getJSONListKeys(JSONObject jsonList) {
    	Iterator<String> myIter = jsonList.keys();
		List<String> keys = new ArrayList<String>();

	    while(myIter.hasNext()){
	    	keys.add(myIter.next());
	    }
	    
	    Collections.sort(keys);
	    
	    return keys;
    }
    
    public static JSONObject apiRequest(String request) {
    	String url = Bookshop.mainApi_url+request;
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
		return null;
    }
}
