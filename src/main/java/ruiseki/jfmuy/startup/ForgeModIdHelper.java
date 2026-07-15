package ruiseki.jfmuy.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import ruiseki.jfmuy.Reference;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.util.Log;

public class ForgeModIdHelper extends AbstractModIdHelper {

    private static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";
    @Nullable
    private static ForgeModIdHelper INSTANCE;

    public static IModIdHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeModIdHelper();
        }
        return INSTANCE;
    }

    private final Map<String, ModContainer> modMap;

    private ForgeModIdHelper() {
        this.modMap = Loader.instance()
            .getIndexedModList();
    }

    @Override
    public String getModNameForModId(String modId) {
        ModContainer modContainer = this.modMap.get(modId);
        if (modContainer == null) {
            return modId;
        }
        return modContainer.getName();
    }

    @Override
    public String getFormattedModNameForModId(String modId) {
        String modNameFormat = Config.getModNameFormat();
        if (modNameFormat.isEmpty()) {
            return null;
        }
        String modName = getModNameForModId(modId);
        modName = removeChatFormatting(modName); // some crazy mod has formatting in the name
        if (modNameFormat.contains(MOD_NAME_FORMAT_CODE)) {
            return StringUtils.replaceOnce(modNameFormat, MOD_NAME_FORMAT_CODE, modName);
        }
        return modNameFormat + modName;
    }

    private static String removeChatFormatting(String string) {
        String withoutFormattingCodes = EnumChatFormatting.getTextWithoutFormattingCodes(string);
        return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
    }

    @Nullable
    @Override
    public String getModNameTooltipFormatting() {
        try {
            ItemStack itemStack = new ItemStack(Items.apple);
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            List<String> tooltip = new ArrayList<>();
            tooltip.add("JEI Tooltip Testing for mod name formatting");
            ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, false);
            tooltip = tooltipEvent.toolTip;

            if (tooltip.size() > 1) {
                for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
                    String line = tooltip.get(lineNum);
                    if (line.contains(Reference.MINECRAFT_NAME)) {
                        String withoutFormatting = EnumChatFormatting.getTextWithoutFormattingCodes(line);
                        if (withoutFormatting != null) {
                            if (line.equals(withoutFormatting)) {
                                return "";
                            } else if (line.contains(withoutFormatting)) {
                                return StringUtils.replaceOnce(line, Reference.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
                            }
                        }
                    }
                }
            }
        } catch (LinkageError | RuntimeException e) {
            Log.get()
                .error("Error while Testing for mod name formatting", e);
        }
        return null;
    }

    @Override
    public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient,
        IIngredientHelper<T> ingredientHelper) {
        if (Config.isDebugModeEnabled() && Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            tooltip = new ArrayList<>(tooltip);
            tooltip.add(EnumChatFormatting.GRAY + "JEI Debug:");
            tooltip.add(EnumChatFormatting.GRAY + "info: " + ingredientHelper.getErrorInfo(ingredient));
            tooltip.add(EnumChatFormatting.GRAY + "uid: " + ingredientHelper.getUniqueId(ingredient));
        }
        if (Config.isModNameFormatOverrideActive() && ingredient instanceof ItemStack) {
            // we detected that another mod is adding the mod name already
            return tooltip;
        }
        return super.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
    }
}
