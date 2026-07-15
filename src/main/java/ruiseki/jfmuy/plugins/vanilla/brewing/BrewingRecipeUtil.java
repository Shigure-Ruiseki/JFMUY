package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.collect.SetMultiMap;

public class BrewingRecipeUtil {

    public static final ItemStack WATER_BOTTLE = new ItemStack(Items.potionitem, 1, 0);

    private final Map<String, Integer> brewingStepCache = new HashMap<>();
    private final SetMultiMap<String, String> potionMap = new SetMultiMap<>();

    public BrewingRecipeUtil() {
        clearCache();
    }

    public void addRecipe(ItemStack inputPotion, ItemStack outputPotion) {
        String potionInputUid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(inputPotion);
        String potionOutputUid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(outputPotion);
        potionMap.put(potionOutputUid, potionInputUid);
        clearCache();
    }

    public int getBrewingSteps(ItemStack outputPotion) {
        String potionInputUid = Internal.getStackHelper()
            .getUniqueIdentifierForStack(outputPotion);
        return getBrewingSteps(potionInputUid, new HashSet<>());
    }

    private void clearCache() {
        if (brewingStepCache.size() != 1) {
            brewingStepCache.clear();
            String waterBottleUid = Internal.getStackHelper()
                .getUniqueIdentifierForStack(WATER_BOTTLE);
            brewingStepCache.put(waterBottleUid, 0);
        }
    }

    private int getBrewingSteps(String potionOutputUid, Set<String> previousSteps) {
        Integer brewingSteps = brewingStepCache.get(potionOutputUid);
        if (brewingSteps == null) {
            previousSteps.add(potionOutputUid);
            Collection<String> prevPotions = potionMap.get(potionOutputUid);
            if (!prevPotions.isEmpty()) {
                int minPrevSteps = Integer.MAX_VALUE;
                for (String prevPotion : prevPotions) {
                    if (!previousSteps.contains(prevPotion)) {
                        int prevSteps = getBrewingSteps(prevPotion, previousSteps);
                        minPrevSteps = Math.min(minPrevSteps, prevSteps);
                    }
                }
                if (minPrevSteps < Integer.MAX_VALUE) {
                    brewingSteps = minPrevSteps + 1;
                    brewingStepCache.put(potionOutputUid, brewingSteps);
                }
            }
        }

        if (brewingSteps == null) {
            return Integer.MAX_VALUE;
        } else {
            return brewingSteps;
        }
    }
}
