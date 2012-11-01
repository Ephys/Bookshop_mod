package net.minecraft.src;

import java.io.BufferedReader;
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

import net.minecraft.client.Minecraft;

public class mod_bookshop extends BaseMod {
    public static LinkedProperties props;
    private static String propsLocation;
    public static LinkedProperties lang;
    private static String langLocation;
    public static String API_URL = "http://aperturelaboratories.eu/bookshop/api/";
    
    public mod_bookshop() {
    	//parseWiki(); // used for testing the pasing of the wikitags
    }

    @Override
    public String getVersion() {
        return "1.2.1";
    }
	
    public String Version() {
        return "Bookshop";
    }
    // http://puu.sh/1jP63
  
	public void load() {
		propsLocation = new StringBuilder().append(Minecraft.getMinecraftDir()).append("/mods/mod_bookshop.props").toString();
		try {
			props = loadProperties(propsLocation);
		} catch (Exception e) {
			//e.printStackTrace();
			try {
				createPropsFile(propsLocation);
				props = loadProperties(propsLocation);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		langLocation = new StringBuilder().append(Minecraft.getMinecraftDir()).append("/mods/mod_bookshop.lang").toString();
		try {
			lang = loadProperties(langLocation);
		} catch (Exception e) {
			//e.printStackTrace();
			try {
				createLangFile(langLocation);
				lang = loadProperties(langLocation);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		ModLoader.addCommand(new CommandBookshop(this));
	}
	
	public LinkedProperties loadProperties(String location) throws FileNotFoundException, IOException 
	{
		LinkedProperties props = new LinkedProperties();
		props.load(new FileInputStream(location));
		return props;
	}
	
	public void saveProperties(LinkedProperties props, String fileLocation, String comments) throws IOException 
	{
		try {
			OutputStream out = new FileOutputStream(fileLocation);
			props.store(out, comments);
			out.flush();
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createPropsFile(String location) throws IOException
	{
		props = new LinkedProperties();
		
		props.setProperty("download.giveBook", "false");
		props.setProperty("MCWiki.API.URL", "http://www.minecraftwiki.net/api.php");

		saveProperties(props, propsLocation, null);
	}
	
	public void createLangFile(String location) throws IOException
	{
		lang = new LinkedProperties();
		
		lang.setProperty("language.syntax", "Syntax");
		lang.setProperty("language.info", "For more informations, visit");
		lang.setProperty("language.loading", "Loading...");
		lang.setProperty("language.loaded", "Loaded.");
		lang.setProperty("language.uploadComplete", "Saved with id");
		lang.setProperty("language.error.noBook", "You must have a book in hand.");
		lang.setProperty("language.error.notFound", "No such book registered.");
		lang.setProperty("language.error.empty", "Empty book.");
		lang.setProperty("language.error.bookListEmpty", "No (public) books in our database.");
		lang.setProperty("language.error.titleListEmpty", "No Match.");
		lang.setProperty("language.error.request", "Request Faillure.");
		lang.setProperty("language.bookList", "Book List:");
		lang.setProperty("language.titleList", "Titles matching");

		saveProperties(lang, langLocation, null);
	}
	
	public static Boolean getBooleanProp(String prop)
	{
		return Boolean.parseBoolean((String)props.get(prop));
	}
	
	public static String getStringProp(String prop)
	{
		return (String)props.get(prop);
	}
	
	public static String getTranslation(String sentence) {
		return (String)lang.getProperty(sentence);
	}
	
	public static Integer getIntegerProp(String prop) {
		return Integer.parseInt((String)props.get(prop));
	}
	
    private void parseWiki() {
    	//String pages = "{{ItemEntity\n|title=Arrow\n|type=Weapons\n|renewable=Yes\n|stackable=Yes (64)\n|firstver=Classic 0.24 (August 31, 2009)\n|data=262\n|id=10\n|entityid=Arrow\n}}\n[[File:ArrowShotInTree.png|thumb|An arrow shot into a tree]]\n\n'''Arrows''' provide ammunition for [[bow]]s and [[dispenser]]s, and can either be crafted or found as a common drop from [[skeleton]]s. When fired, they will fly in a [[wikipedia:Trajectory of a projectile|ballistic trajectory]] affected by gravity and drag in [[air]] or [[water]] and will travel approximately 3 [[block]]s when fired parallel to a flat plane with no charge, 15 blocks average with medium charge, and 24 blocks average with maximum charge. Arrows in water get much more drag than in air, and leave a trail of bubbles in their wake. Arrows (with bow at full strength) can travel 120 blocks when fired from the optimal angle.\n\nThey deal {{healthbar|9|9px}} damage when fully charged (or, rarely, {{healthbar|10|9px}}); if they are charged to medium, they will deal {{healthbar|6|9px}}, and {{healthbar|1|9px}} if not charged at all. Arrows fired from dispensers deal {{healthbar|3|9px}} damage. Immediately after hitting the target, there is a cooldown during which the target will be immune to further [[damage]]. Arrows hitting the mob in this state will lose all speed and drop to the ground.\n\nArrows will also stick into objects they come in contact with and will remain there for exactly a minute before disappearing; the distance from the object and the angle determine how far into the target the arrow will go. Arrows that have been fired by the player and stuck into terrain are retrievable and never break. Arrows shot by skeletons are special entities and cannot be collected. If an arrow is stuck in a block and the block in which the arrow is stuck in is broken, then the arrow will fall, and the sound of an arrow hitting a block will be triggered again, even if the arrow just falls one block down.\n\nThey can bounce off entities like [[minecart]]s and damage immune mobs. In multiplayer, if PvP is on, a player can hurt themselves if they run forward while shooting arrows while the server is lagging. It is also possible for players to damage themselves with their own arrows by firing one directly up and standing still. Arrows fired directly upward using a fully charged bow will fly so high that they disappear from view, but eventually fall. If that arrow lands on a block, and the player is directly beneath that block, then the player breaks that block, the arrow will fall and do one heart of damage to the player.\n\nIf the block in which an arrow is stuck in is destroyed or disappears (e.g. [[leaves]]), the arrow will fall and can injure the player or a mob.\n\nArrows shot through [[lava]], ''but not [[fire]],'' will catch on fire and show an appropriate animation until they pass through water. They can set other entities they hit on fire.\n\nArrows shot at rails will stop the minecart from passing over that block until the arrow despawns or is collected.\n\nAn arrow shot at a storage minecart will cause the minecart to break, dropping a minecart, a chest, and the contents of the storage minecart.\n\n== Crafting ==\n\n{| class=\"wikitable\"\n! Ingredients\n! Input Output\n|-\n! [[Flint]] + [[Stick]] + [[Feather]]\n| {{Grid2/Crafting Table\n|A1=  |B1= Flint   |C1=\n|A2=  |B2= Stick   |C2=\n|A3=  |B3= Feather |C3=\n|Output = Arrow,4\n}}\n|}\n\n== Arrows and circuits ==\n\nWhile arrows shot at [[switch]]es will not trigger said inputs directly, workarounds can be used to trigger circuits from a distance. For instance, a [[minecart]] can be used in such a way that destroying it with arrows releases the Wooden [[Pressure Plate|pressure plate]] it is resting on, or a [[painting]] can be shot off a wall and fall onto a Wooden [[Pressure Plate|pressure plate]] to activate it. [[Boat]]s can be used as targets since a fully charged bow will break the boat in one shot. These methods can be bypassed using wooden buttons, which can be activated directly by arrows.\n\nAdditionally, a wooden [[Pressure Plate|pressure plate]] can be activated by firing an arrow at it directly. This, however, requires one to force the block occupied by the pressure plate to update after the arrow makes contact. This can be achieved in SSP by passing particles through the pressure plate (by placing a torch or source of fire nearby or dripping lava or water above) and in both SSP and SMP by placing a boat on an adjacent half block (so part is over the pressure plate but not touching it). Arrows can also activate a pressure plate if fired at the side of the block to which it is attached, although only if fired at the top half. This can be used to create completely invisible switches that can be activated at a distance.\n\nAn alternative implies shooting an arrow on the upper half of a block holding a [[rail]] (powered or not). The minecart should stop on this track instead of going on; the cart can either be blocked (or sent back if in the middle of a ramp), in combination with looped minecart circuits, this can be an effective trigger, and does not require particle updates\nYou can pick up arrows in survival mode when on the ground.\n\n== Video ==\n\n{{:Arrow/video}}\n\n== History ==\n\n{{History|classic}}\n{{History||0.24 (August 31, 2009)|Arrows were added and were fired by pressing {{Key|Tab}}.}}\n{{History|st}}\n{{History||0.26|Skeleton arrows were purple. The purple arrow texture can still be found in the [[minecraft.jar]] file, in the same image as the normal arrows, but it is not in use.}}\n{{History||0.29_01|Arrows were removed after the Survival Test.}}\n{{History|indev}}\n{{History||0.31 (January 22, 2010)|Arrows were re-added along with a [[bow]].}}\n{{History||0.31 (January 30, 2010)|Became craftable. The arrow sound effect was added in.}}\n{{History|a}}\n{{History||SF7|The tip of an arrow in crafting is now made from [[flint]] rather than [[Iron Bar|iron]].}}\n{{History|beta}}\n{{History||1.6|[[File:Arrow in Sugar Cane.png|thumb|Arrows sticking into sugar cane before 1.6]]Arrows used to stick in any block before this update, even some non-solid ones. For example, they could stick in [[torch]]es, [[Sugar Cane|sugarcane]], and [[portal]]s.}}\n{{History||1.8pre1|[[File:Stickyarrows.png|thumb|Example of arrows sticking into mobs in between Beta 1.8 Pre-release and 1.0.0]]Arrows can no longer be rapid-fired from bows. Arrows (with bow at full strength) can travel 120 blocks when fired from the optimal angle, and stick to mobs.}}\n{{History|r}}\n{{History||1.0.0|Arrows no longer stick to mobs.}}\n{{History||1.1|Arrows which are on fire now set entities they hit on fire. Before, arrows could be on fire (like other [[entity|entities]]), but they did not set what they hit on fire, and there was no [[enchantment]] to fire flaming arrows.}}\n{{History||12w23a|Arrows can activate the [[tripwire]] switch and wooden [[Pressure Plates|pressure plates]].}}\n{{History||12w34a|Arrows can activate the new wooden [[button]]s.}}\n{{History||12w34b|Arrows on fire can ignite [[TNT]].}}\n{{History|foot}}\n\n== See also ==\n* [[Weapon]]\n* [[Flint]]\n* [[Feather]]\n* [[Stick]]\n\n{{Items}}\n{{Entities}}\n\n[[Category:Items]]\n\n[[de:Pfeil]]\n[[nl:Pijl]]\n[[pl:Strza\\u0142a]]\n[[ru:\\u0421\\u0442\\u0440\\u0435\\u043b\\u0430]]";
    	String pages = "[[wikipedia:Portal (video game)#Plot|Portal]]";
    	String[] pagesArray;
    	
    	int MAXLINES = 13;
    	int MAXCHAR = 230;
    	
    	int nCounter = 0;
		ArrayList output = new ArrayList();
		String formating_page = "";
		
		System.out.println(pages);
		
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
    	
    	pagesArray = pages.split("\n");
    	
    	System.out.println("result:"+pages);

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
					for(int j = 0; j < words.length; j++) {
						if(newSentence.length()+words[j].length() > MAXCHAR) {
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
    }
}