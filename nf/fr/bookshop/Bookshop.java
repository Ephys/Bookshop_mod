package nf.fr.bookshop;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = "ephys.bookshop.cmd", name = "Bookshop", version = "1.7.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class Bookshop {
    // config
    public Boolean giveBook;
    public static String mainApi_url;
    public static String wikiApi_url;
    
    public String bookshop_login;
    public String bookshop_token;
    
    private Configuration config;
    
    public static Map<String, String> modTranslation;
    
    public static CommandBookshop command;
	private boolean command_enabled;

	@Instance("ephys.bookshop.cmd")
	public static Bookshop instance;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		prepareConfig(event);
		prepareLanguage(event);
	}
	
	private void prepareConfig(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        
        // create basic config file
        Property prop;
        prop = config.get("download", "giveBook", false);
        prop.comment = "set to true if the command shall give you a book or to false if you need a book to use the command";
        this.giveBook = prop.getBoolean(false);
        
        prop = config.get("download", "api_url", "http://api.bookshop.fr.nf/");
        prop.comment = "The url of the API the mod is interacting with";
        Bookshop.mainApi_url = prop.getString();
        
        prop = config.get("download", "wikiApi_url", "http://www.minecraftwiki.net/api.php");
        prop.comment = "The url of the wikimedia API used by the mod (change subdomain to change language, i.g. http://fr.minecraftwiki.net/api.php for french)";
        Bookshop.wikiApi_url = prop.getString();
        
        prop = config.get("bookshop", "login", "");
        prop.comment = "Your http://mcnetwork.fr.nf/ username";
        this.bookshop_login = prop.getString();
        
        prop = config.get("bookshop", "api_token", "");
        prop.comment = "Your http://mcnetwork.fr.nf/ API Token";
        this.bookshop_token = prop.getString();

        this.command_enabled = config.get("enable", "command_line", true).getBoolean(true);
        
        config.save();
	}

	@Init
	public void init(FMLInitializationEvent event) {}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event) {
		if(this.command_enabled)
			event.registerServerCommand(new CommandBookshop(this));
	}

	@PostInit
	public static void postInit(FMLPostInitializationEvent event) {}
	
	public void prepareLanguage(FMLPreInitializationEvent event)
	{
        Configuration lang = new Configuration(new File(event.getModConfigurationDirectory() + "/bookshop.lang"));
        lang.load();
        
        Bookshop.modTranslation = new HashMap<String, String>();
        Bookshop.modTranslation.put("syntax", lang.get("syntax", "language", "Syntax").getString());
        Bookshop.modTranslation.put("info", lang.get("info", "language", "For more informations, visit").getString());
        Bookshop.modTranslation.put("loading", lang.get("loading", "language", "Loading...").getString());
        Bookshop.modTranslation.put("loaded", lang.get("loaded", "language", "Loaded.").getString());
        Bookshop.modTranslation.put("uploadComplete", lang.get("uploadComplete", "language", "Saved with id").getString());
        Bookshop.modTranslation.put("noBook", lang.get("noBook", "language", "You must have a book in hand.").getString());
        Bookshop.modTranslation.put("notFound", lang.get("notFound", "language", "No such book registered.").getString());
        Bookshop.modTranslation.put("empty", lang.get("empty", "language", "Empty book.").getString());
        Bookshop.modTranslation.put("bookListEmpty", lang.get("bookListEmpty", "language", "No (public) books in our database.").getString());
        Bookshop.modTranslation.put("searchListEmpty", lang.get("searchListEmpty", "language", "No Match.").getString());
        Bookshop.modTranslation.put("requestError", lang.get("requestError", "language", "Request Faillure.").getString());
        Bookshop.modTranslation.put("bookList", lang.get("bookList", "language", "Book List:").getString());
        Bookshop.modTranslation.put("titleList", lang.get("titleList", "language", "Titles matching").getString());
        Bookshop.modTranslation.put("denied", lang.get("denied", "language", "Access Denied").getString());
        
        lang.save();
	}

	public void storePass(String login, String pass) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
	        config.load();
	        
	        Property prop;
	        prop = config.get("login", "bookshop", login);
	        prop.comment = "Your http://mcnetwork.fr.nf/ username";
	        this.bookshop_login = prop.getString();
	        
	        prop = config.get("api_token", "bookshop", pass);
	        prop.comment = "Your http://mcnetwork.fr.nf/ API Token";
	        this.bookshop_token = prop.getString();
	
	        config.save();
        }
	}
}