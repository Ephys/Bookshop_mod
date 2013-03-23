package nf.fr.bookshop.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import nf.fr.bookshop.Bookshop;
import nf.fr.bookshop.Bookshop_requests;

public class GuiBookstore extends GuiContainer {
	private TileEntityBookstore te;
	private InventoryPlayer ip;
	
    public GuiBookstore(InventoryPlayer inventoryPlayer, TileEntityBookstore tileEntity) {
    	super(new ContainerBookstore(inventoryPlayer, tileEntity));
    	te = tileEntity;
    	this.ip = inventoryPlayer;
    	
    	xSize = 221;
    	ySize = 190;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouse_x, int mouse_y) {
    	fontRenderer.drawString("Bookstore", 8, 6, 4210752);
    	fontRenderer.drawString(te.info, 215-fontRenderer.getStringWidth(te.info), 6, 4210752);
    	if(te.inv[0] != null) {
    		if(te.inv[0].itemID == Item.writableBook.itemID || te.inv[0].itemID == Item.writtenBook.itemID) {
    			if(te.loading)
    				te.info = Bookshop.modTranslation.get("loading");
    			else
    				te.info = "";
    			
    			int x = (width - xSize) / 2 - 90;
    	    	int y = (height - ySize) / 2;

    	    	int mx = mouse_x - x;
                int my = mouse_y - y;
    	    	
                int[] colorStr = new int[4];
                if(mx < 180 && mx > 100) {
                	for(int i = 0; i < 4; i++) {
                		if(my > 20+20*i && my < 30+20*i) {
                			colorStr[i] = 0xfffff;
                		} else
                			colorStr[i] = 0xffffee;
                	}
                } else {
                	colorStr[0] = 0xffffee;
                	colorStr[1] = 0xffffee;
                	colorStr[2] = 0xffffee;
                	colorStr[3] = 0xffffee;
                }
                
                if(te.selectedCath != -1)
                	colorStr[te.selectedCath] = 0xff;
                
    			fontRenderer.drawString("Random Books", 90-fontRenderer.getStringWidth("Random Books"), 24, colorStr[0]);
    			fontRenderer.drawString("Best Books", 90-fontRenderer.getStringWidth("Best Books"), 43, colorStr[1]);
    			fontRenderer.drawString("Lastest Books", 90-fontRenderer.getStringWidth("Lastest Books"), 62, colorStr[2]);
    			//fontRenderer.drawString("Your Books", 90-fontRenderer.getStringWidth("Your books"), 81, colorStr[3]);

    			if(te.bookList.size() != 0) {
                    colorStr = new int[4];
                    if(mx < 275 && mx > 185) {
                    	for(int i = 0; i < 4; i++) {
                    		if(my > 20+20*i && my < 30+20*i) {
                    			colorStr[i] = 0xfffff;
                    		} else
                    			colorStr[i] = 0xffffee;
                    	}
                    } else {
                    	colorStr[0] = 0xffffee;
                    	colorStr[1] = 0xffffee;
                    	colorStr[2] = 0xffffee;
                    	colorStr[3] = 0xffffee;
                    }
                    
                    int i = 0;
    				
    			    for(Entry<String, String> entry: te.bookList.entrySet()) {
    			    	if(i == 4)
    			    		break;
    			    	
    			    	if(entry.getValue().length() > 12)
    			    		fontRenderer.drawString(entry.getValue().substring(0, 12).concat("..."), 95, 24+19*i, colorStr[i]);
    			    	else
    			    		fontRenderer.drawString(entry.getValue(), 95, 24+19*i, colorStr[i]);
    			    	i++;
    			    }
    			}
    		}
    	} else {
        	te.info = "Place a writable book";
        }
    	fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 93, 4210752);
    }
    
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
        int width = (this.width - this.xSize) / 2;
        int height = (this.height - this.ySize) / 2;

        for (int i = 0; i < 4; ++i)
        {
            int k1 = par1 - (width - 10);
            int l1 = par2 - (height + 14 + 19 * i);
            
            if (k1 >= 0 && l1 >= 0 && k1 < 108 && l1 < 19) {
                loadCath(i);
            }
            
            if (k1 >= 104 && l1 >= 0 && k1 < 185 && l1 < 19) {
                loadBook(i);
            }
        }
    }
    
    private boolean loadBook(int num) {
    	int i = 0;
	    for(Entry<String, String> entry: te.bookList.entrySet()) {
	    	if(i == 4)
	    		break;
	    	
	    	if(i == num) {
	    		JSONObject book = Bookshop_requests.loadBook(entry.getKey());
	    		
	    		try {
		    		if(book == null) {
		    			te.info = Bookshop.modTranslation.get("notFound");
		    		} else {
		    			if (book.isNull("error")) {
	    	    			List bookPages = new ArrayList();
	    	    			String bookAuthor = book.getString("author");
	    	    			String bookTitle = book.getString("title");
	    	    			JSONArray ps = book.getJSONArray("pages");

	    	    			if(ps.length() == 0) {
	    	    				te.info = Bookshop.modTranslation.get("notFound");
	    	    			} else {
	    		    			for (int j = 0; j < ps.length(); j++)
	    		    			{
	    							bookPages.add(ps.getString(j));
	    		    			}
	    		    			
	    		    			int[] boxPos = {this.te.xCoord, this.te.yCoord, this.te.zCoord};
	    		    			
	    		    			Bookshop_requests.createBook(bookTitle, bookPages, bookAuthor, te.inv[0], ip.player, boxPos);
	    		    			te.info = Bookshop.modTranslation.get("loaded");
	    		    			
	    		    			return true;
	    	    			}
		    			} else {
		    	    		te.info = book.getString("error");
		    	    	}	
	    			}
	    		} catch (JSONException e) { 
	    			te.info = Bookshop.modTranslation.get("notFound");
	    		}
	    		break;
	    	}
	    	i++;
	    }
		return false;
    }
    
    private boolean loadCath(int num) {
    	te.selectedCath = num;

    	try {
    		te.loading = true;
        	JSONObject bookList = null;
        	if(num == 0)
        		bookList = Bookshop_requests.loadBookList("random");
        	else if(num == 1)
        		bookList = Bookshop_requests.loadBookList("best");
        	else if(num == 2)
        		bookList = Bookshop_requests.loadBookList("lastest");
        	else if(num == 3) {
        		te.loading = false;
        		return false;
        	}
        	
        	te.loading = false;
        	
			if(bookList.isNull("error")) {
				List<String> keys = Bookshop_requests.getJSONListKeys(bookList);
				
				te.bookList = new HashMap<String, String>();
				
				for(String key : keys) {
					if(key.equalsIgnoreCase("type"))
						continue;
					
					JSONObject book = bookList.getJSONObject(key);
					te.bookList.put(book.getString("id"), book.getString("title"));
			    }
			} else {
				te.info = bookList.getString("error");
				return false;
			}
			
			
			return true;
		} catch (Exception e) {
			te.info = Bookshop.modTranslation.get("requestError");
			te.loading = false;
			return false;
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	this.mc.renderEngine.bindTexture("/mods/Bookshop/textures/gui/bookstore.png");
    	int x = (width - xSize) / 2;
    	int y = (height - ySize) / 2;
    	this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

}