package ruiseki.okbase;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.helper.LangHelpers;

public class OKCreativeTab extends CreativeTabs {

    public static final OKCreativeTab INSTANCE = new OKCreativeTab();

    public OKCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public Item getTabIconItem() {
        return Items.apple;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return LangHelpers.localize("creativetab." + getTabLabel());
    }
}
