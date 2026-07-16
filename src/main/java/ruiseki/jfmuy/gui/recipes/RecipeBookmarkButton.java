package ruiseki.jfmuy.gui.recipes;

import net.minecraft.client.Minecraft;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.gui.IDrawable;
import ruiseki.jfmuy.api.recipe.IRecipeCategory;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkGroup;
import ruiseki.jfmuy.autocrafting.RecipeBookmarkItem;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.TooltipRenderer;
import ruiseki.jfmuy.gui.elements.GuiIconButtonSmall;
import ruiseki.jfmuy.util.Translator;

public class RecipeBookmarkButton extends GuiIconButtonSmall {

    private final IRecipeCategory<?> category;
    private final IRecipeWrapper recipe;
    private RecipeLayout recipeLayout;

    public RecipeBookmarkButton(int id, int width, int height, IDrawable icon, IRecipeCategory<?> category,
        IRecipeWrapper recipe, RecipeLayout recipeLayout) {
        super(id, 0, 0, width, height, icon);
        this.category = category;
        this.recipe = recipe;
        this.recipeLayout = recipeLayout;
    }

    public void init(RecipeLayout recipeLayout) {
        this.recipeLayout = recipeLayout;
        // Propagates the state of the favorite button if there are no outputs to the recipe.
        this.enabled = this.visible = recipeLayout.getRecipeFavoriteButton().enabled;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        visible = enabled && Config.areRecipeBookmarksEnabled();
        super.drawButton(mc, mouseX, mouseY);
    }

    public void drawToolTip(Minecraft mc, int mouseX, int mouseY) {
        if (field_146123_n && visible) {
            String tooltipTransfer = Translator.translateToLocal("jfmuy.tooltip.recipe_bookmark");
            TooltipRenderer.drawHoveringText(mc, tooltipTransfer, mouseX, mouseY);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!super.mousePressed(mc, mouseX, mouseY)) {
            return false;
        }
        if (!Config.isBookmarkOverlayEnabled()) {
            Config.toggleBookmarkEnabled();
        }
        BookmarkList bookmarkList = Internal.getBookmarkList();
        RecipeBookmarkGroup group = new RecipeBookmarkGroup(bookmarkList.nextId());
        RecipeBookmarkItem<?> recipeBookmarkItem = new RecipeBookmarkItem<>(
            recipeLayout.getRecipeFavoriteButton()
                .getDisplayedIngredient());
        recipeBookmarkItem.setGroup(group); // Do this early so that the dummy items are also added.
        recipeBookmarkItem.populateWith(recipe, category);
        group.addItem(recipeBookmarkItem); // Do this late so that the recipe isn't overwritten.
        group.update();
        return bookmarkList.add(group);
    }
}
