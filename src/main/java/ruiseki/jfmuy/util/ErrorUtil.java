package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredientRegistry;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.ingredients.VanillaTypes;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.ingredients.Ingredients;
import ruiseki.jfmuy.startup.IModIdHelper;
import ruiseki.okcore.datastructure.NonNullList;

public final class ErrorUtil {

    @Nullable
    private static IModIdHelper modIdHelper;

    private ErrorUtil() {}

    public static void setModIdHelper(IModIdHelper modIdHelper) {
        ErrorUtil.modIdHelper = modIdHelper;
    }

    public static <T> String getInfoFromRecipe(T recipe, IRecipeWrapper recipeWrapper) {
        StringBuilder recipeInfoBuilder = new StringBuilder();
        String recipeName = getNameForRecipe(recipe);
        recipeInfoBuilder.append(recipeName);

        Ingredients ingredients = new Ingredients();

        try {
            recipeWrapper.getIngredients(ingredients);
        } catch (RuntimeException | LinkageError ignored) {
            recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
            return recipeInfoBuilder.toString();
        }

        recipeInfoBuilder.append("\nOutputs:");
        Set<IIngredientType> outputTypes = ingredients.getOutputIngredients()
            .keySet();
        for (IIngredientType<?> outputType : outputTypes) {
            String ingredientOutputInfo = getIngredientOutputInfo(outputType, ingredients);
            recipeInfoBuilder.append('\n')
                .append(
                    outputType.getIngredientClass()
                        .getName())
                .append(": ")
                .append(ingredientOutputInfo);
        }

        recipeInfoBuilder.append("\nInputs:");
        Set<IIngredientType> inputTypes = ingredients.getInputIngredients()
            .keySet();
        for (IIngredientType<?> inputType : inputTypes) {
            String ingredientInputInfo = getIngredientInputInfo(inputType, ingredients);
            recipeInfoBuilder.append('\n')
                .append(
                    inputType.getIngredientClass()
                        .getName())
                .append(": ")
                .append(ingredientInputInfo);
        }

        return recipeInfoBuilder.toString();
    }

    private static <T> String getIngredientOutputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
        List<List<T>> outputs = ingredients.getOutputs(ingredientType);
        return getIngredientInfo(ingredientType, outputs);
    }

    private static <T> String getIngredientInputInfo(IIngredientType<T> ingredientType, IIngredients ingredients) {
        List<List<T>> inputs = ingredients.getInputs(ingredientType);
        return getIngredientInfo(ingredientType, inputs);
    }

    public static String getNameForRecipe(Object recipe) {
        // 1.7.10 registry name resolution fallback (No IForgeRegistryEntry in 1.7.10)
        String registryNameStr = null;
        if (recipe instanceof Item) {
            registryNameStr = GameData.getItemRegistry()
                .getNameForObject((Item) recipe);
        } else if (recipe instanceof Block) {
            registryNameStr = GameData.getBlockRegistry()
                .getNameForObject((Block) recipe);
        }

        if (registryNameStr != null) {
            if (modIdHelper != null) {
                // Extracts the domain/namespace part from "modid:name"
                int colonIndex = registryNameStr.indexOf(':');
                String modId = colonIndex != -1 ? registryNameStr.substring(0, colonIndex) : "minecraft";
                String modName = modIdHelper.getModNameForModId(modId);
                return modName + " " + registryNameStr + " " + recipe.getClass();
            }
            return registryNameStr + " " + recipe.getClass();
        }

        try {
            return recipe.toString();
        } catch (RuntimeException e) {
            Log.get()
                .error("Failed recipe.toString", e);
            return recipe.getClass()
                .toString();
        }
    }

    public static <T> String getInfoFromBrokenCraftingRecipe(T recipe, List inputs, ItemStack output) {
        StringBuilder recipeInfoBuilder = new StringBuilder();
        String recipeName = getNameForRecipe(recipe);
        recipeInfoBuilder.append(recipeName);

        recipeInfoBuilder.append("\nOutputs:");
        List<List<ItemStack>> outputs = Collections.singletonList(Collections.singletonList(output));
        String ingredientOutputInfo = getIngredientInfo(VanillaTypes.ITEM, outputs);
        recipeInfoBuilder.append('\n')
            .append(ItemStack.class.getName())
            .append(": ")
            .append(ingredientOutputInfo);

        recipeInfoBuilder.append("\nInputs:");
        List<List<ItemStack>> inputLists = Internal.getStackHelper()
            .expandRecipeItemStackInputs(inputs, false);
        String ingredientInputInfo = getIngredientInfo(VanillaTypes.ITEM, inputLists);
        recipeInfoBuilder.append('\n')
            .append(ItemStack.class.getName())
            .append(": ")
            .append(ingredientInputInfo);

        return recipeInfoBuilder.toString();
    }

    public static <T> String getIngredientInfo(T ingredient) {
        IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry()
            .getIngredientHelper(ingredient);
        return ingredientHelper.getErrorInfo(ingredient);
    }

    public static <T> String getIngredientInfo(IIngredientType<T> ingredientType, List<? extends List<T>> ingredients) {
        IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry()
            .getIngredientHelper(ingredientType);
        List<String> allInfos = new ArrayList<>(Math.min(ingredients.size() + 1, 101));

        int slotLimit = Math.min(ingredients.size(), 100);
        for (int i = 0; i < slotLimit; i++) {
            allInfos.add(getIngredientSlotInfo(ingredientHelper, ingredients.get(i)));
        }
        if (ingredients.size() > 100) {
            allInfos.add(String.format("<truncated to %s elements, skipped %s>", 100, ingredients.size() - 100));
        }

        return allInfos.toString();
    }

    private static <T> String getIngredientSlotInfo(IIngredientHelper<T> ingredientHelper, List<T> ingredients) {
        List<String> infos = new ArrayList<>(Math.min(ingredients.size() + 1, 11));

        int ingredientLimit = Math.min(ingredients.size(), 10);
        for (int i = 0; i < ingredientLimit; i++) {
            String errorInfo = ingredientHelper.getErrorInfo(ingredients.get(i));
            infos.add(errorInfo);
        }
        if (ingredients.size() > 10) {
            infos.add(String.format("<truncated to %s elements, skipped %s>", 10, ingredients.size() - 10));
        }

        return infos.toString();
    }

    public static String getItemStackInfo(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return "null";
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return itemStack.stackSize + "x (null)";
        }

        final String itemName;
        String registryName = GameData.getItemRegistry()
            .getNameForObject(item);
        if (registryName != null) {
            itemName = registryName;
        } else if (item instanceof ItemBlock) {
            final String blockName;
            Block block = ((ItemBlock) item).field_150939_a;
            if (block == null) {
                blockName = "null";
            } else {
                String blockRegistryName = GameData.getBlockRegistry()
                    .getNameForObject(block);
                if (blockRegistryName != null) {
                    blockName = blockRegistryName;
                } else {
                    blockName = block.getClass()
                        .getName();
                }
            }
            itemName = "ItemBlock(" + blockName + ")";
        } else {
            itemName = item.getClass()
                .getName();
        }

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt != null) {
            return itemStack + " " + itemName + " nbt:" + nbt;
        }
        return itemStack + " " + itemName;
    }

    public static void checkNotEmpty(@Nullable String string, String name) {
        if (string == null) {
            throw new NullPointerException(name + " must not be null.");
        } else if (string.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
    }

    // Helper check to replace 1.11+ ItemStack.isEmpty() logic
    private static boolean isStackEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    public static void checkNotEmpty(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("ItemStack must not be null.");
        } else if (isStackEmpty(itemStack)) {
            String info = getItemStackInfo(itemStack);
            throw new IllegalArgumentException("ItemStack value must not be empty. " + info);
        }
    }

    public static void checkNotEmpty(@Nullable ItemStack itemStack, String name) {
        if (itemStack == null) {
            throw new NullPointerException(name + " must not be null.");
        } else if (isStackEmpty(itemStack)) {
            String info = getItemStackInfo(itemStack);
            throw new IllegalArgumentException("ItemStack " + name + " must not be empty. " + info);
        }
    }

    public static <T> void checkNotEmpty(@Nullable T[] values, String name) {
        if (values == null) {
            throw new NullPointerException(name + " must not be null.");
        } else if (values.length <= 0) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        for (T value : values) {
            if (value == null) {
                throw new NullPointerException(name + " must not contain null values.");
            }
        }
    }

    public static void checkNotEmpty(@Nullable Collection values, String name) {
        if (values == null) {
            throw new NullPointerException(name + " must not be null.");
        } else if (values.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        } else if (!(values instanceof NonNullList<?>)) {
            for (Object value : values) {
                if (value == null) {
                    throw new NullPointerException(name + " must not contain null values.");
                }
            }
        }
    }

    public static <T> T checkNotNull(@Nullable T object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
        return object;
    }

    public static <T> void checkIsValidIngredient(@Nullable T ingredient, String name) {
        checkNotNull(ingredient, name);
        IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
        if (!ingredientHelper.isValidIngredient(ingredient)) {
            String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
            throw new IllegalArgumentException(
                "Invalid ingredient found. Parameter Name: " + name + " Ingredient Info: " + ingredientInfo);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void assertMainThread() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && !minecraft.func_152345_ab()) {
            Thread currentThread = Thread.currentThread();
            throw new IllegalStateException(
                "A JEI API method is being called by another mod from the wrong thread:\n" + currentThread
                    + "\n"
                    + "It must be called on the main thread by using Minecraft.addScheduledTask.");
        }
    }

    public static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
        IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
        IIngredientType<T> ingredientType = ingredientRegistry.getIngredientType(ingredient);
        IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);

        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
        CrashReportCategory ingredientCategory = crashreport.makeCategory("Ingredient being rendered");

        if (modIdHelper != null) {
            ingredientCategory.addCrashSectionCallable("Mod Name", new Callable<String>() {

                @Override
                public String call() {
                    String modId = ingredientHelper.getDisplayModId(ingredient);
                    return modIdHelper.getModNameForModId(modId);
                }
            });
        }
        ingredientCategory.addCrashSectionCallable("Registry Name", new Callable<String>() {

            @Override
            public String call() {
                String modId = ingredientHelper.getModId(ingredient);
                String resourceId = ingredientHelper.getResourceId(ingredient);
                return modId + ":" + resourceId;
            }
        });
        ingredientCategory.addCrashSectionCallable("Display Name", new Callable<String>() {

            @Override
            public String call() {
                return ingredientHelper.getDisplayName(ingredient);
            }
        });
        ingredientCategory.addCrashSectionCallable("String Name", new Callable<String>() {

            @Override
            public String call() {
                return ingredient.toString();
            }
        });

        CrashReportCategory jeiCategory = crashreport.makeCategory("JEI render details");
        jeiCategory.addCrashSectionCallable("Unique Id (for Blacklist)", new Callable<String>() {

            @Override
            public String call() {
                return ingredientHelper.getUniqueId(ingredient);
            }
        });
        jeiCategory.addCrashSectionCallable("Ingredient Type", new Callable<String>() {

            @Override
            public String call() {
                return ingredientType.getIngredientClass()
                    .toString();
            }
        });
        jeiCategory.addCrashSectionCallable("Error Info", new Callable<String>() {

            @Override
            public String call() {
                return ingredientHelper.getErrorInfo(ingredient);
            }
        });
        jeiCategory.addCrashSectionCallable("Filter Text", new Callable<String>() {

            @Override
            public String call() {
                return Config.getFilterText();
            }
        });
        jeiCategory.addCrashSectionCallable("Edit Mode Enabled", new Callable<String>() {

            @Override
            public String call() {
                return Boolean.toString(Config.isEditModeEnabled());
            }
        });
        jeiCategory.addCrashSectionCallable("Debug Mode Enabled", new Callable<String>() {

            @Override
            public String call() {
                return Boolean.toString(Config.isDebugModeEnabled());
            }
        });

        throw new ReportedException(crashreport);
    }
}
