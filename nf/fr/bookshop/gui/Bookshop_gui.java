package nf.fr.bookshop.gui;

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
import nf.fr.bookshop.gui.BlockBookstore;
import nf.fr.bookshop.gui.BookshopGuiHandler;
import nf.fr.bookshop.gui.TileEntityBookstore;


@Mod(modid = "ephys.bookshop.gui", name = "Bookshop", version = "1.0.0", dependencies = "required-after:ephys.bookshop.cmd")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels={"ephys.bookshop"}, packetHandler = PacketHandler.class)
public class Bookshop_gui {
    private Configuration config;
 
    public static Block Bookstore;
    private int bookstore_id;

	@Instance("ephys.bookshop.gui")
	public static Bookshop_gui instance;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		prepareConfig(event);
	}
	
	private void prepareConfig(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        
        this.bookstore_id = config.get("blocks_id", "bookstore", 1994).getInt(1994);
        
        config.save();
	}

	@Init
	public void init(FMLInitializationEvent event) {
		Bookstore = new BlockBookstore(this.bookstore_id, Material.wood);
		MinecraftForge.setBlockHarvestLevel(Bookstore, "axe", 1);
		GameRegistry.registerBlock(Bookstore, "Bookstore");
		LanguageRegistry.addName(Bookstore, "Bookstore");
		
        GameRegistry.registerTileEntity(TileEntityBookstore.class, "containerBookstore");
        NetworkRegistry.instance().registerGuiHandler(this, new BookshopGuiHandler());
        
        //ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(Bookstore, 1), new Object[] {
		//	"XXX", "ABC", "XXX", 'X', "woodPlank", 'C', "dyeBlack", 'B',
		//	Item.feather, 'A', Item.book });
        ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(Bookstore, 1), new Object[] {
			"XXX", "ABC", "XXX", 'X', Block.planks, 'C', Item.dyePowder, 'B',
			Item.feather, 'A', Item.book });
        GameRegistry.addRecipe(recipe);
	}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event) {}

	@PostInit
	public static void postInit(FMLPostInitializationEvent event) {}
}