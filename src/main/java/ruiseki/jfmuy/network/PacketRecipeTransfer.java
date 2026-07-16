package ruiseki.jfmuy.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ruiseki.jfmuy.transfer.BasicRecipeTransferHandlerServer;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.ExtendedBuffer;
import ruiseki.okcore.network.PacketCodec;

public class PacketRecipeTransfer extends PacketCodec {

    private static final Logger LOGGER = LogManager.getLogger();

    public Map<Integer, Integer> recipeMap;
    public Map<Integer, Integer> recipeCountMap;
    public List<Integer> craftingSlots;
    public List<Integer> inventorySlots;

    @CodecField
    private boolean maxTransfer;

    @CodecField
    private boolean requireCompleteSets;

    public PacketRecipeTransfer() {}

    public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots,
        List<Integer> inventorySlots, boolean maxTransfer, boolean requireCompleteSets) {
        this(
            recipeMap,
            createDefaultRecipeCountMap(recipeMap),
            craftingSlots,
            inventorySlots,
            maxTransfer,
            requireCompleteSets);
    }

    public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, Map<Integer, Integer> recipeCountMap,
        List<Integer> craftingSlots, List<Integer> inventorySlots, boolean maxTransfer, boolean requireCompleteSets) {
        this.recipeMap = recipeMap;
        this.recipeCountMap = recipeCountMap;
        this.craftingSlots = craftingSlots;
        this.inventorySlots = inventorySlots;
        this.maxTransfer = maxTransfer;
        this.requireCompleteSets = requireCompleteSets;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void decode(ExtendedBuffer input) {
        int recipeMapSize = input.readVarIntFromBuffer();
        this.recipeMap = new HashMap<>();
        this.recipeCountMap = new HashMap<>();
        for (int i = 0; i < recipeMapSize; i++) {
            int slotIndex = input.readVarIntFromBuffer();
            int recipeItem = input.readVarIntFromBuffer();
            int recipeItemCount = input.readVarIntFromBuffer();
            this.recipeMap.put(slotIndex, recipeItem);
            this.recipeCountMap.put(slotIndex, recipeItemCount);
        }

        int craftingSlotsSize = input.readVarIntFromBuffer();
        this.craftingSlots = new ArrayList<>();
        for (int i = 0; i < craftingSlotsSize; i++) {
            this.craftingSlots.add(input.readVarIntFromBuffer());
        }

        int inventorySlotsSize = input.readVarIntFromBuffer();
        this.inventorySlots = new ArrayList<>();
        for (int i = 0; i < inventorySlotsSize; i++) {
            this.inventorySlots.add(input.readVarIntFromBuffer());
        }

        super.decode(input);
    }

    @Override
    public void encode(ExtendedBuffer output) {
        output.writeVarIntToBuffer(recipeMap.size());
        for (Map.Entry<Integer, Integer> recipeMapEntry : recipeMap.entrySet()) {
            output.writeVarIntToBuffer(recipeMapEntry.getKey());
            output.writeVarIntToBuffer(recipeMapEntry.getValue());
            output.writeVarIntToBuffer(recipeCountMap.getOrDefault(recipeMapEntry.getKey(), 1));
        }

        output.writeVarIntToBuffer(craftingSlots.size());
        for (Integer craftingSlot : craftingSlots) {
            output.writeVarIntToBuffer(craftingSlot);
        }

        output.writeVarIntToBuffer(inventorySlots.size());
        for (Integer inventorySlot : inventorySlots) {
            output.writeVarIntToBuffer(inventorySlot);
        }

        super.encode(output);
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        Container container = player.openContainer;

        if (!isValidCollectionSize(container, this.recipeMap.size(), "recipe map")) {
            return;
        }

        for (Map.Entry<Integer, Integer> entry : this.recipeMap.entrySet()) {
            if (!isValidSlotIndex(container, entry.getValue(), "recipe item")) {
                return;
            }
            int count = this.recipeCountMap.getOrDefault(entry.getKey(), 0);
            if (count < 1) {
                LOGGER.error(
                    "Recipe transfer packet has invalid recipe item count {} for container {}",
                    count,
                    container.getClass());
                return;
            }
        }

        if (!isValidCollectionSize(container, this.craftingSlots.size(), "slot ids")) {
            return;
        }

        if (!validateRecipeMapCraftingSlots(this.recipeMap, this.craftingSlots)) {
            return;
        }

        if (!isValidCollectionSize(container, this.inventorySlots.size(), "slot ids")) {
            return;
        }

        for (Integer slotIndex : this.craftingSlots) {
            if (!isValidSlotIndex(container, slotIndex, "slot")) return;
        }
        for (Integer slotIndex : this.inventorySlots) {
            if (!isValidSlotIndex(container, slotIndex, "slot")) return;
        }

        BasicRecipeTransferHandlerServer.setItems(
            player,
            this.recipeMap,
            this.recipeCountMap,
            this.craftingSlots,
            this.inventorySlots,
            this.maxTransfer,
            this.requireCompleteSets);
    }

    private static boolean isValidCollectionSize(Container container, int slotCount, String collectionName) {
        if (slotCount < 0 || slotCount > container.inventorySlots.size()) {
            LOGGER.error(
                "Recipe transfer packet has invalid {} count {} for container {} with {} slots",
                collectionName,
                slotCount,
                container.getClass(),
                container.inventorySlots.size());
            return false;
        }
        return true;
    }

    private static boolean isValidSlotIndex(Container container, int slotIndex, String slotName) {
        if (slotIndex < 0 || slotIndex >= container.inventorySlots.size()) {
            LOGGER.error(
                "Recipe transfer packet has invalid {} id {} for container {}",
                slotName,
                slotIndex,
                container.getClass());
            return false;
        }
        return true;
    }

    private static boolean validateRecipeMapCraftingSlots(Map<Integer, Integer> recipeMap,
        List<Integer> craftingSlots) {
        int craftingSlotCount = craftingSlots.size();
        for (Integer craftingSlotNumber : recipeMap.keySet()) {
            if (craftingSlotNumber < 0 || craftingSlotNumber >= craftingSlotCount) {
                LOGGER.error(
                    "Recipe transfer packet has invalid crafting slot number {} for {} crafting slots",
                    craftingSlotNumber,
                    craftingSlotCount);
                return false;
            }
        }
        return true;
    }

    private static Map<Integer, Integer> createDefaultRecipeCountMap(Map<Integer, Integer> recipeMap) {
        Map<Integer, Integer> recipeCountMap = new HashMap<>(recipeMap.size());
        for (Integer key : recipeMap.keySet()) {
            recipeCountMap.put(key, 1);
        }
        return recipeCountMap;
    }
}
