package ruiseki.jfmuy.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import ruiseki.jfmuy.network.IPacketId;
import ruiseki.jfmuy.network.PacketIdServer;
import ruiseki.jfmuy.transfer.BasicRecipeTransferHandlerServer;

public class PacketRecipeTransfer extends PacketJFMUY {

    private Map<Integer, Integer> recipeMap;
    private List<Integer> craftingSlots;
    private List<Integer> inventorySlots;
    private boolean maxTransfer;
    private boolean requireCompleteSets;

    public PacketRecipeTransfer() {

    }

    public PacketRecipeTransfer(@Nonnull Map<Integer, Integer> recipeMap, @Nonnull List<Integer> craftingSlots,
        @Nonnull List<Integer> inventorySlots, boolean maxTransfer, boolean requireCompleteSets) {
        this.recipeMap = recipeMap;
        this.craftingSlots = craftingSlots;
        this.inventorySlots = inventorySlots;
        this.maxTransfer = maxTransfer;
        this.requireCompleteSets = requireCompleteSets;
    }

    @Override
    public IPacketId getPacketId() {
        return PacketIdServer.RECIPE_TRANSFER;
    }

    @Override
    public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
        int recipeMapSize = buf.readVarIntFromBuffer();
        recipeMap = new HashMap<>(recipeMapSize);
        for (int i = 0; i < recipeMapSize; i++) {
            int craftIndex = buf.readVarIntFromBuffer();
            int inventorySlotNumber = buf.readVarIntFromBuffer();
            recipeMap.put(craftIndex, inventorySlotNumber);
        }

        int craftingSlotsSize = buf.readVarIntFromBuffer();
        craftingSlots = new ArrayList<>(craftingSlotsSize);
        for (int i = 0; i < craftingSlotsSize; i++) {
            craftingSlots.add(buf.readVarIntFromBuffer());
        }

        int inventorySlotsSize = buf.readVarIntFromBuffer();
        inventorySlots = new ArrayList<>(inventorySlotsSize);
        for (int i = 0; i < inventorySlotsSize; i++) {
            inventorySlots.add(buf.readVarIntFromBuffer());
        }

        maxTransfer = buf.readBoolean();
        requireCompleteSets = buf.readBoolean();

        BasicRecipeTransferHandlerServer
            .setItems(player, recipeMap, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(recipeMap.size());
        for (Map.Entry<Integer, Integer> recipeMapEntry : recipeMap.entrySet()) {
            buf.writeVarIntToBuffer(recipeMapEntry.getKey());
            buf.writeVarIntToBuffer(recipeMapEntry.getValue());
        }

        buf.writeVarIntToBuffer(craftingSlots.size());
        for (Integer craftingSlot : craftingSlots) {
            buf.writeVarIntToBuffer(craftingSlot);
        }

        buf.writeVarIntToBuffer(inventorySlots.size());
        for (Integer inventorySlot : inventorySlots) {
            buf.writeVarIntToBuffer(inventorySlot);
        }

        buf.writeBoolean(maxTransfer);
        buf.writeBoolean(requireCompleteSets);
    }
}
