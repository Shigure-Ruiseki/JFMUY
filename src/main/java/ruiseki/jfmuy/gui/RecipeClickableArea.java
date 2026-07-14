package ruiseki.jfmuy.gui;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.client.config.HoverChecker;

public class RecipeClickableArea extends HoverChecker {

    @NotNull
    private final List<String> recipeCategoryUids;

    public RecipeClickableArea(int top, int bottom, int left, int right, @NotNull String... recipeCategoryUids) {
        super(top, bottom, left, right, 0);
        this.recipeCategoryUids = Arrays.asList(recipeCategoryUids);
    }

    @NotNull
    public List<String> getRecipeCategoryUids() {
        return recipeCategoryUids;
    }
}
