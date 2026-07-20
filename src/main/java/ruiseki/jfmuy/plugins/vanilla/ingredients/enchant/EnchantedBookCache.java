package ruiseki.jfmuy.plugins.vanilla.ingredients.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.jfmuy.collect.Table;
import ruiseki.jfmuy.startup.PlayerJoinedWorldEvent;

public class EnchantedBookCache {

    private final Table<String, Integer, ItemStack> cache = Table.hashBasedTable();

    public ItemStack getEnchantedBook(EnchantmentData enchantmentData) {
        Enchantment enchantment = enchantmentData.enchantmentobj;
        String registryName = enchantment.getName();
        return cache.computeIfAbsent(
            registryName,
            enchantmentData.enchantmentLevel,
            () -> Items.enchanted_book.getEnchantedItemStack(enchantmentData));
    }

    @SubscribeEvent
    public void onPlayerJoinedWorldEvent(PlayerJoinedWorldEvent event) {
        cache.clear();
    }
}
