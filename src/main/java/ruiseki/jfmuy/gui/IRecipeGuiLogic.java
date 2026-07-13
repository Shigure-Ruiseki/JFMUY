package ruiseki.jfmuy.gui;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ruiseki.jfmuy.api.recipe.IRecipeCategory;

public interface IRecipeGuiLogic {

    @Nonnull
    String getPageString();

    void setRecipesPerPage(int recipesPerPage);

    boolean hasMultipleCategories();

    void previousRecipeCategory();

    void nextRecipeCategory();

    boolean hasMultiplePages();

    void previousPage();

    void nextPage();

    boolean setFocus(@Nonnull Focus focus);

    boolean back();

    boolean setCategoryFocus();

    @Nullable
    Focus getFocus();

    @Nullable
    IRecipeCategory getRecipeCategory();

    @Nonnull
    List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY);
}
