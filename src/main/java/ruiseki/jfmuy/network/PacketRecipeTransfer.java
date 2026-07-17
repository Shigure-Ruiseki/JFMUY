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

    public Map<Integer, Integer> recipeMap = new HashMap<>();
    public List<Integer> craftingSlots = new ArrayList<>();
    public List<Integer> inventorySlots = new ArrayList<>();
    public Map<Integer, Integer> itemCounts = new HashMap<>();

    @CodecField
    public int outputSlot = -1;
    @CodecField
    private int maxTransfer = 0;
    @CodecField
    private boolean performRecipe = false;
    @CodecField
    private boolean requireCompleteSets = false;

    public PacketRecipeTransfer() {}

    public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots,
        List<Integer> inventorySlots, int maxTransfer, boolean performRecipe, boolean requireCompleteSets) {
        this(
            recipeMap,
            craftingSlots,
            inventorySlots,
            maxTransfer,
            performRecipe,
            requireCompleteSets,
            createDefaultRecipeCountMap(recipeMap));
    }

    public PacketRecipeTransfer(Map<Integer, Integer> recipeMap, List<Integer> craftingSlots,
        List<Integer> inventorySlots, int maxTransfer, boolean performRecipe, boolean requireCompleteSets,
        Map<Integer, Integer> itemCounts) {
        this.recipeMap = recipeMap;
        this.itemCounts = itemCounts;
        this.craftingSlots = craftingSlots;
        this.inventorySlots = inventorySlots;
        this.maxTransfer = maxTransfer;
        this.performRecipe = performRecipe;
        this.requireCompleteSets = requireCompleteSets;
    }

    public PacketRecipeTransfer setOutputSlot(int outputSlot) {
        this.outputSlot = outputSlot;
        return this;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void decode(ExtendedBuffer input) {
        super.decode(input);

        int recipeMapSize = input.readVarIntFromBuffer();
        this.recipeMap = new HashMap<>();
        this.itemCounts = new HashMap<>();
        for (int i = 0; i < recipeMapSize; i++) {
            int slotIndex = input.readVarIntFromBuffer();
            int recipeItem = input.readVarIntFromBuffer();
            int recipeItemCount = input.readVarIntFromBuffer();
            this.recipeMap.put(slotIndex, recipeItem);
            this.itemCounts.put(slotIndex, recipeItemCount);
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
    }

    @Override
    public void encode(ExtendedBuffer output) {
        super.encode(output);

        output.writeVarIntToBuffer(recipeMap.size());
        for (Map.Entry<Integer, Integer> recipeMapEntry : recipeMap.entrySet()) {
            output.writeVarIntToBuffer(recipeMapEntry.getKey());
            output.writeVarIntToBuffer(recipeMapEntry.getValue());
            output.writeVarIntToBuffer(itemCounts.getOrDefault(recipeMapEntry.getKey(), 1));
        }

        output.writeVarIntToBuffer(craftingSlots.size());
        for (Integer craftingSlot : craftingSlots) {
            output.writeVarIntToBuffer(craftingSlot);
        }

        output.writeVarIntToBuffer(inventorySlots.size());
        for (Integer inventorySlot : inventorySlots) {
            output.writeVarIntToBuffer(inventorySlot);
        }
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
            int count = this.itemCounts.getOrDefault(entry.getKey(), 0);
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
            this.craftingSlots,
            this.inventorySlots,
            this.maxTransfer,
            this.requireCompleteSets,
            this.itemCounts);
        if (performRecipe && outputSlot != -1) {
            BasicRecipeTransferHandlerServer.performRecipe(player, outputSlot);
        }
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
