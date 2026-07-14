package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.registry.GameData;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.ingredients.IIngredients;
import ruiseki.jfmuy.api.recipe.IRecipeHandler;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;

public class ErrorUtil {

    public static <T> String getInfoFromRecipe(T recipe, IRecipeHandler<T> recipeHandler) {
        StringBuilder recipeInfoBuilder = new StringBuilder();
        try {
            recipeInfoBuilder.append(recipe);
        } catch (RuntimeException e) {
            Log.error("Failed recipe.toString", e);
            recipeInfoBuilder.append(recipe.getClass());
        }

        IRecipeWrapper recipeWrapper;

        try {
            recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
        } catch (RuntimeException ignored) {
            recipeInfoBuilder.append("\nFailed to create recipe wrapper");
            return recipeInfoBuilder.toString();
        } catch (LinkageError ignored) {
            recipeInfoBuilder.append("\nFailed to create recipe wrapper");
            return recipeInfoBuilder.toString();
        }

        Ingredients ingredients = new Ingredients();

        try {
            recipeWrapper.getIngredients(ingredients);
        } catch (RuntimeException ignored) {
            recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
            return recipeInfoBuilder.toString();
        } catch (LinkageError ignored) {
            recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
            return recipeInfoBuilder.toString();
        }

        recipeInfoBuilder.append("\nOutputs:");
        Set<Class> outputClasses = ingredients.getOutputIngredients()
            .keySet();
        for (Class<?> outputClass : outputClasses) {
            List<String> ingredientOutputInfo = getIngredientOutputInfo(outputClass, ingredients);
            recipeInfoBuilder.append('\n')
                .append(outputClass.getName())
                .append(": ")
                .append(ingredientOutputInfo);
        }

        recipeInfoBuilder.append("\nInputs:");
        Set<Class> inputClasses = ingredients.getInputIngredients()
            .keySet();
        for (Class<?> inputClass : inputClasses) {
            List<String> ingredientInputInfo = getIngredientInputInfo(inputClass, ingredients);
            recipeInfoBuilder.append('\n')
                .append(inputClass.getName())
                .append(": ")
                .append(ingredientInputInfo);
        }

        return recipeInfoBuilder.toString();
    }

    private static <T> List<String> getIngredientOutputInfo(Class<T> ingredientClass, IIngredients ingredients) {
        List<T> outputs = ingredients.getOutputs(ingredientClass);
        List<List<T>> outputLists = new ArrayList<List<T>>();
        for (T output : outputs) {
            outputLists.add(Collections.singletonList(output));
        }
        return getIngredientInfo(ingredientClass, outputLists);
    }

    private static <T> List<String> getIngredientInputInfo(Class<T> ingredientClass, IIngredients ingredients) {
        List<List<T>> inputs = ingredients.getInputs(ingredientClass);
        return getIngredientInfo(ingredientClass, inputs);
    }

    public static <T> String getInfoFromBrokenCraftingRecipe(T recipe, List inputs, @Nullable ItemStack output) {
        StringBuilder recipeInfoBuilder = new StringBuilder();
        try {
            recipeInfoBuilder.append(recipe);
        } catch (RuntimeException e) {
            Log.error("Failed recipe.toString", e);
            recipeInfoBuilder.append(recipe.getClass());
        }

        recipeInfoBuilder.append("\nOutputs:");
        List<List<ItemStack>> outputs = Collections.singletonList(Collections.singletonList(output));
        List<String> ingredientOutputInfo = getIngredientInfo(ItemStack.class, outputs);
        recipeInfoBuilder.append('\n')
            .append(ItemStack.class.getName())
            .append(": ")
            .append(ingredientOutputInfo);

        recipeInfoBuilder.append("\nInputs:");
        List<List<ItemStack>> inputLists = Internal.getStackHelper()
            .expandRecipeItemStackInputs(inputs);
        List<String> ingredientInputInfo = getIngredientInfo(ItemStack.class, inputLists);
        recipeInfoBuilder.append('\n')
            .append(ItemStack.class.getName())
            .append(": ")
            .append(ingredientInputInfo);

        return recipeInfoBuilder.toString();
    }

    public static <T> List<String> getIngredientInfo(Class<T> ingredientClass, List<List<T>> ingredients) {
        IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry()
            .getIngredientHelper(ingredientClass);
        List<String> allInfos = new ArrayList<String>(ingredients.size());

        for (List<T> inputList : ingredients) {
            List<String> infos = new ArrayList<String>(inputList.size());
            for (T input : inputList) {
                String errorInfo = ingredientHelper.getErrorInfo(input);
                infos.add(errorInfo);
            }
            allInfos.add(infos.toString());
        }

        return allInfos;
    }

    @Nullable
    public static List<String> getItemStackIngredientsInfo(@Nullable List list) {
        if (list == null) {
            return null;
        }
        StackHelper stackHelper = Internal.getStackHelper();

        List<String> ingredientsInfo = new ArrayList<String>();
        for (Object ingredient : list) {
            List<String> ingredientInfo = new ArrayList<String>();

            List<ItemStack> stacks = null;
            try {
                stacks = stackHelper.toItemStackList(ingredient);
            } catch (RuntimeException ignored) {
                ingredientInfo.add("too broken to get info");
            } catch (LinkageError ignored) {
                ingredientInfo.add("too broken to get info");
            }

            if (stacks != null) {
                String oreDict = stackHelper.getOreDictEquivalent(stacks);
                if (oreDict != null) {
                    ingredientInfo.add("OreDict: " + oreDict);
                }

                for (ItemStack stack : stacks) {
                    String itemStackInfo = getItemStackInfo(stack);
                    ingredientInfo.add(itemStackInfo);
                }
            }

            ingredientsInfo.add(ingredientInfo.toString() + "\n");
        }
        return ingredientsInfo;
    }

    public static String getItemStackInfo(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return "null";
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return itemStack.stackSize + "x (null)";
        }

        String itemName = GameData.getItemRegistry()
            .getNameForObject(item);
        if (!itemName.isEmpty()) {
            if (item instanceof ItemBlock) {
                String blockName;
                Block block = ((ItemBlock) item).field_150939_a;
                if (block == null) {
                    blockName = "null";
                } else {
                    blockName = GameData.getBlockRegistry()
                        .getNameForObject(item);
                    if (blockName.isEmpty()) {
                        blockName = block.getClass()
                            .getName();
                    }
                }
                itemName = "ItemBlock(" + blockName + ")";
            }
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
}
