package ruiseki.jfmuy;

/** For JEI internal use only, these are normally accessed from the API. */
public class Internal {

    private static JFMUYHelpers helpers;
    private static RecipeRegistry recipeRegistry;
    private static ItemRegistry itemRegistry;

    private Internal() {

    }

    public static JFMUYHelpers getHelpers() {
        return helpers;
    }

    public static void setHelpers(JFMUYHelpers helpers) {
        Internal.helpers = helpers;
    }

    public static RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    public static void setRecipeRegistry(RecipeRegistry recipeRegistry) {
        Internal.recipeRegistry = recipeRegistry;
    }

    public static ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public static void setItemRegistry(ItemRegistry itemRegistry) {
        Internal.itemRegistry = itemRegistry;
    }
}
