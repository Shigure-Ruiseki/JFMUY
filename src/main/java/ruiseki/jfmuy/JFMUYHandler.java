package ruiseki.jfmuy;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import ruiseki.jfmuy.transfer.BasicRecipeTransferHandlerServer;

public class JFMUYHandler {

    public static final JFMUYHandler INSTANCE = new JFMUYHandler();

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        BasicRecipeTransferHandlerServer.itemsCrafted += event.crafting.stackSize;
    }
}
