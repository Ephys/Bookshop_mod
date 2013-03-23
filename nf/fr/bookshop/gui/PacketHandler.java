package nf.fr.bookshop.gui;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
        @Override
        public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player playerEntity) {
        	if (packet.channel.equals("ephys.bookshop")) {
                handleBookPacket(packet, (EntityPlayer)playerEntity);
        	}
        }

        private void handleBookPacket(Packet250CustomPayload packet, EntityPlayer player) {
        	DataInputStream datainputstream;
            ItemStack itemstack;
            ItemStack itemstack1;

            try {
	            datainputstream = new DataInputStream(new ByteArrayInputStream(packet.data));
	            itemstack = Packet.readItemStack(datainputstream);
	            
	            //System.out.println("received:" +itemstack.getTagCompound().getInteger("box_x"));
	            TileEntityBookstore te = (TileEntityBookstore)player.worldObj.getBlockTileEntity(itemstack.getTagCompound().getInteger("box_x"), 
	            															itemstack.getTagCompound().getInteger("box_y"), 
	            															itemstack.getTagCompound().getInteger("box_z"));
	            itemstack1 = te.inv[0];
	            
	            if(itemstack == null || itemstack1 == null)
	            	return;
	            
	            if (!itemstack.getTagCompound().hasKey("author"))
	            {
	                if (itemstack.itemID == Item.writableBook.itemID || itemstack.itemID == Item.writtenBook.itemID) {
	                    itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages"));
	                }
	            }
	            else
	            {
                    if (itemstack.itemID == Item.writtenBook.itemID || itemstack.itemID == Item.writtenBook.itemID) {
                        itemstack1.setTagInfo("author", new NBTTagString("author", itemstack.getTagCompound().getString("author")));
                        itemstack1.setTagInfo("title", new NBTTagString("title", itemstack.getTagCompound().getString("title")));
                        itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages"));
                    }
	            }
            } catch(Exception e) {
            	e.printStackTrace();
            }
        }
}