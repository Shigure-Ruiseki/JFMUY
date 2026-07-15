package ruiseki.jfmuy.plugins.vanilla;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;

public class InventoryEffectRendererGuiHandler implements IAdvancedGuiHandler<InventoryEffectRenderer> {

    @Override
    public Class<InventoryEffectRenderer> getGuiContainerClass() {
        return InventoryEffectRenderer.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(InventoryEffectRenderer guiContainer) {
        int x = guiContainer.guiLeft - 124;
        int y = guiContainer.guiTop;
        Minecraft minecraft = guiContainer.mc;
        if (minecraft == null) {
            return Collections.emptyList();
        }
        EntityPlayerSP player = minecraft.thePlayer;
        if (player == null) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
        if (activePotionEffects == null || activePotionEffects.isEmpty()) {
            return Collections.emptyList();
        }

        List<Rectangle> areas = new ArrayList<>();
        int height = 33;
        if (activePotionEffects.size() > 5) {
            height = 132 / (activePotionEffects.size() - 1);
        }

        List<PotionEffect> sortedEffects = new ArrayList<>(activePotionEffects);
        sortedEffects.sort(Comparator.comparingInt(PotionEffect::getPotionID));
        for (PotionEffect potioneffect : sortedEffects) {
            Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
            if (potion != null) {
                areas.add(new Rectangle(x, y, 140, height));
                y += height;
            }
        }
        return areas;
    }
}
