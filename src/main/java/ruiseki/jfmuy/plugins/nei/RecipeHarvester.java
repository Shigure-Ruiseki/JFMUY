package ruiseki.jfmuy.plugins.nei;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ruiseki.jfmuy.api.IGuiHelper;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.recipe.IRecipeCategoryRegistration;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public class RecipeHarvester {

    private static final List<String> DEFAULT_BLACKLIST = Arrays.asList(
        "codechicken.nei.recipe.ShapedRecipeHandler",
        "codechicken.nei.recipe.ShapelessRecipeHandler",
        "codechicken.nei.recipe.FurnaceRecipeHandler",
        "codechicken.nei.recipe.BrewingRecipeHandler",
        "codechicken.nei.recipe.FuelRecipeHandler",
        "codechicken.nei.recipe.RepairRecipeHandler");

    private static final Set<String> BLACKLISTED_CLASSES = ConcurrentHashMap.newKeySet();

    static {
        BLACKLISTED_CLASSES.addAll(DEFAULT_BLACKLIST);
    }

    private final TemplateRecipeHandler baseHandler;
    private final Set<CategoryInfo> registeredCategories = ConcurrentHashMap.newKeySet();

    private static class CategoryInfo {

        final String categoryUid;
        final String rawRecipeId;

        CategoryInfo(String categoryUid, String rawRecipeId) {
            this.categoryUid = categoryUid;
            this.rawRecipeId = rawRecipeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryInfo info = (CategoryInfo) o;
            return Objects.equals(categoryUid, info.categoryUid) && Objects.equals(rawRecipeId, info.rawRecipeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categoryUid, rawRecipeId);
        }
    }

    public RecipeHarvester(TemplateRecipeHandler baseHandler) {
        this.baseHandler = baseHandler;
    }

    public static boolean addBlacklistedClass(String className) {
        return className != null && !className.trim()
            .isEmpty() && BLACKLISTED_CLASSES.add(className.trim());
    }

    public static boolean addBlacklistedClass(Class<?> clazz) {
        return clazz != null && addBlacklistedClass(clazz.getName());
    }

    public static void addBlacklistedClasses(Collection<String> classNames) {
        if (classNames != null) {
            for (String name : classNames) {
                addBlacklistedClass(name);
            }
        }
    }

    public static void resetBlacklistToDefaults() {
        BLACKLISTED_CLASSES.clear();
        BLACKLISTED_CLASSES.addAll(DEFAULT_BLACKLIST);
    }

    public static Set<String> getBlacklistedClasses() {
        return BLACKLISTED_CLASSES;
    }

    public Set<String> extractRecipeIds() {
        Set<String> ids = ConcurrentHashMap.newKeySet();
        if (baseHandler == null || BLACKLISTED_CLASSES.contains(
            baseHandler.getClass()
                .getName())) {
            return ids;
        }

        // 1. Overlay Identifier
        try {
            String overlayId = baseHandler.getOverlayIdentifier();
            if (overlayId != null && !overlayId.trim()
                .isEmpty()) {
                ids.add(overlayId);
            }
        } catch (Throwable ignored) {}

        // 2. Transfer Rectangles
        try {
            TemplateRecipeHandler tempHandler = baseHandler.newInstance();
            tempHandler.loadTransferRects();

            if (tempHandler.transferRects != null) {
                Field outputIdField = TemplateRecipeHandler.RecipeTransferRect.class.getDeclaredField("outputId");
                outputIdField.setAccessible(true);

                for (TemplateRecipeHandler.RecipeTransferRect rect : tempHandler.transferRects) {
                    if (rect != null) {
                        String id = (String) outputIdField.get(rect);
                        if (id != null && !id.trim()
                            .isEmpty()) {
                            ids.add(id);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 3. Fallback to Class Name
        if (ids.isEmpty()) {
            ids.add(
                baseHandler.getClass()
                    .getName());
        }

        return ids;
    }

    public void register(IRecipeCategoryRegistration categoryRegistry, Set<String> recipeIds,
        Set<String> globalRegisteredUids) {
        IGuiHelper guiHelper = categoryRegistry.getJFMUYHelpers()
            .getGuiHelper();

        for (String rawRecipeId : recipeIds) {
            String targetCategoryUid = normalizeCategoryUid(rawRecipeId);

            if (globalRegisteredUids.add(targetCategoryUid)) {
                try {
                    TemplateRecipeHandler handler = baseHandler.newInstance();
                    loadHandlerRecipes(handler, rawRecipeId);

                    NEITemplateCategory category = new NEITemplateCategory(guiHelper, handler, targetCategoryUid);
                    categoryRegistry.addRecipeCategories(category);
                } catch (Throwable ignored) {}
            }
            registeredCategories.add(new CategoryInfo(targetCategoryUid, rawRecipeId));
        }
    }

    public void initialize(IModRegistry registry, Set<String> globalHarvestedCatalystUids) {
        for (CategoryInfo info : registeredCategories) {
            TemplateRecipeHandler handler = baseHandler.newInstance();

            loadHandlerRecipes(handler, info.rawRecipeId);

            int numRecipes = handler.numRecipes();
            if (numRecipes <= 0) continue;

            // Convert to Wrappers
            List<IRecipeWrapper> wrappers = new ArrayList<>(numRecipes);
            for (int i = 0; i < numRecipes; i++) {
                wrappers.add(new NEITemplateWrapper(handler, i));
            }

            try {
                registry.addRecipes(wrappers, info.categoryUid);
            } catch (Throwable ignored) {}

            if (globalHarvestedCatalystUids.add(info.categoryUid)) {
                registerCatalysts(registry, info.categoryUid, info.rawRecipeId);
            }
        }

        registeredCategories.clear();
    }

    private void loadHandlerRecipes(TemplateRecipeHandler handler, String rawRecipeId) {
        // 1. Load by Raw Recipe ID
        try {
            handler.loadCraftingRecipes(rawRecipeId);
        } catch (Throwable ignored) {}

        // 2. Load by Overlay Identifier
        if (handler.numRecipes() <= 0) {
            try {
                String overlay = baseHandler.getOverlayIdentifier();
                if (overlay != null && !overlay.isEmpty()) {
                    handler.loadCraftingRecipes(overlay);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Load by Class Name
        if (handler.numRecipes() <= 0) {
            try {
                handler.loadCraftingRecipes(
                    baseHandler.getClass()
                        .getName());
            } catch (Throwable ignored) {}
        }
    }

    private String normalizeCategoryUid(String id) {
        if (id == null) return "";
        return "nei." + id;
    }

    private void registerCatalysts(IModRegistry registry, String categoryUid, String rawRecipeId) {
        List<PositionedStack> catalysts = new ArrayList<>();

        if (rawRecipeId != null && !rawRecipeId.isEmpty()) {
            try {
                catalysts.addAll(RecipeCatalysts.getRecipeCatalysts(rawRecipeId));
            } catch (Throwable ignored) {}
        }

        if (catalysts.isEmpty()) {
            try {
                catalysts.addAll(
                    RecipeCatalysts.getRecipeCatalysts(
                        baseHandler.getClass()
                            .getName()));
            } catch (Throwable ignored) {}
        }

        if (catalysts.isEmpty()) {
            try {
                Class<?> guiClass = baseHandler.getGuiClass();
                if (guiClass != null) {
                    catalysts.addAll(RecipeCatalysts.getRecipeCatalysts(guiClass.getName()));
                }
            } catch (Throwable ignored) {}
        }

        if (catalysts.isEmpty()) return;

        List<ItemStack> uniqueCatalysts = new ArrayList<>();

        catalysts.stream()
            .filter(Objects::nonNull)
            .flatMap(pStack -> {
                if (pStack.items != null && pStack.items.length > 0) {
                    return Arrays.stream(pStack.items);
                } else if (pStack.item != null) {
                    return Stream.of(pStack.item);
                }
                return Stream.empty();
            })
            .filter(Objects::nonNull)
            .forEach(stack -> {
                boolean exists = uniqueCatalysts.stream()
                    .anyMatch(
                        existing -> ItemStack.areItemStacksEqual(existing, stack)
                            && ItemStack.areItemStackTagsEqual(existing, stack));
                if (!exists) {
                    uniqueCatalysts.add(stack);
                }
            });

        uniqueCatalysts.forEach(catalystStack -> {
            try {
                registry.addRecipeCatalyst(catalystStack, categoryUid);
            } catch (Throwable ignored) {}
        });
    }
}
