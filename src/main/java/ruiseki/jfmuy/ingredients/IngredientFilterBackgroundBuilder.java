package ruiseki.jfmuy.ingredients;

import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.suffixtree.GeneralizedSuffixTree;
import ruiseki.okcore.datastructure.NonNullList;

public class IngredientFilterBackgroundBuilder {

    private final Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees;
    private final NonNullList<IIngredientListElement> elementList;

    public IngredientFilterBackgroundBuilder(Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees,
        NonNullList<IIngredientListElement> elementList) {
        this.prefixedSearchTrees = prefixedSearchTrees;
        this.elementList = elementList;
    }

    public void start() {
        boolean finished = run(10000);
        if (!finished) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.side == Side.CLIENT && Minecraft.getMinecraft().thePlayer != null) {
            boolean finished = run(20);
            if (!finished) {
                return;
            }
        }
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private boolean run(final int timeoutMs) {
        final long startTime = System.currentTimeMillis();
        for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.values()) {
            Config.SearchMode mode = prefixedTree.getMode();
            if (mode != Config.SearchMode.DISABLED) {
                PrefixedSearchTree.IStringsGetter stringsGetter = prefixedTree.getStringsGetter();
                GeneralizedSuffixTree tree = prefixedTree.getTree();
                for (int i = tree.getHighestIndex() + 1; i < this.elementList.size(); i++) {
                    IIngredientListElement element = elementList.get(i);
                    Collection<String> strings = stringsGetter.getStrings(element);
                    if (strings.isEmpty()) {
                        tree.put("", i);
                    } else {
                        for (String string : strings) {
                            tree.put(string, i);
                        }
                    }
                    if (System.currentTimeMillis() - startTime >= timeoutMs) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
