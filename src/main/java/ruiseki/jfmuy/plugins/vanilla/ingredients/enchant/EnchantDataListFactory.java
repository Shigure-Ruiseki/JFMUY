package ruiseki.jfmuy.plugins.vanilla.ingredients.enchant;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;

public final class EnchantDataListFactory {

    private EnchantDataListFactory() {

    }

    public static List<EnchantmentData> create() {
        List<EnchantmentData> enchantData = new ArrayList<>();

        for (Enchantment enchant : Enchantment.enchantmentsList) {
            if (enchant != null) {
                for (int lvl = enchant.getMinLevel(); lvl <= enchant.getMaxLevel(); lvl++) {
                    enchantData.add(new EnchantmentData(enchant, lvl));
                }
            }
        }

        return enchantData;
    }
}
