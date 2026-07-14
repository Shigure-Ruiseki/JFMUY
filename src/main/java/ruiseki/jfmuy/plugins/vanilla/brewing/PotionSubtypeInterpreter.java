package ruiseki.jfmuy.plugins.vanilla.brewing;

import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.api.ISubtypeRegistry;

public class PotionSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {

    public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

    private PotionSubtypeInterpreter() {

    }

    @Nullable
    @Override
    public String getSubtypeInfo(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == null) {
            return null;
        }

        int meta = itemStack.getItemDamage();

        List<?> effects = Items.potionitem.getEffects(meta);

        if (effects == null || effects.isEmpty()) {
            return "empty_" + meta;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("meta_")
            .append(meta);

        for (Object obj : effects) {
            if (obj instanceof PotionEffect) {
                PotionEffect effect = (PotionEffect) obj;
                stringBuilder.append(";")
                    .append(effect.getPotionID())
                    .append("_")
                    .append(effect.getAmplifier())
                    .append("_")
                    .append(effect.getDuration());
            }
        }

        return stringBuilder.toString();
    }
}
