package ruiseki.jfmuy.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.startup.StackHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import ruiseki.jfmuy.api.IJFMUYHelpers;
import ruiseki.jfmuy.api.recipe.IRecipeWrapper;
import ruiseki.jfmuy.api.recipe.IRecipeWrapperFactory;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;

public final class CraftingRecipeChecker {
    private CraftingRecipeChecker() {
    }

    @SuppressWarnings("unchecked")
    public static List<IRecipeWrapper> getValidRecipes(final IJFMUYHelpers jfmuyHelpers) {
        CraftingRecipeValidator<ShapedOreRecipe> shapedOreRecipeValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapedOreRecipeWrapper(jfmuyHelpers, recipe)
        );
        CraftingRecipeValidator<ShapedRecipes> shapedRecipesValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapedRecipesWrapper(jfmuyHelpers, recipe)
        );
        CraftingRecipeValidator<ShapelessOreRecipe> shapelessOreRecipeValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapelessRecipeWrapper<>(jfmuyHelpers, recipe)
        );
        CraftingRecipeValidator<ShapelessRecipes> shapelessRecipesValidator = new CraftingRecipeValidator<>(
            recipe -> new ShapelessRecipeWrapper<>(jfmuyHelpers, recipe)
        );

        StackHelper stackHelper = Internal.getStackHelper();
        List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
        List<IRecipeWrapper> validRecipeWrappers = new ArrayList<>();

        for (IRecipe recipe : recipeList) {
            if (recipe == null) {
                continue;
            }

            if (recipe instanceof ShapedOreRecipe shapedOreRecipe) {
                if (shapedOreRecipeValidator.isRecipeValid(shapedOreRecipe, stackHelper)) {
                    validRecipeWrappers.add(shapedOreRecipeValidator.getRecipeWrapper(shapedOreRecipe));
                }
            } else if (recipe instanceof ShapedRecipes shapedRecipes) {
                if (shapedRecipesValidator.isRecipeValid(shapedRecipes, stackHelper)) {
                    validRecipeWrappers.add(shapedRecipesValidator.getRecipeWrapper(shapedRecipes));
                }
            } else if (recipe instanceof ShapelessOreRecipe shapelessOreRecipe) {
                if (shapelessOreRecipeValidator.isRecipeValid(shapelessOreRecipe, stackHelper)) {
                    validRecipeWrappers.add(shapelessOreRecipeValidator.getRecipeWrapper(shapelessOreRecipe));
                }
            } else if (recipe instanceof ShapelessRecipes shapelessRecipes) {
                if (shapelessRecipesValidator.isRecipeValid(shapelessRecipes, stackHelper)) {
                    validRecipeWrappers.add(shapelessRecipesValidator.getRecipeWrapper(shapelessRecipes));
                }
            }
        }
        return validRecipeWrappers;
    }

    private static final class CraftingRecipeValidator<T extends IRecipe> {
        private static final int INVALID_COUNT = -1;
        private final IRecipeWrapperFactory<T> recipeWrapperFactory;

        public CraftingRecipeValidator(IRecipeWrapperFactory<T> recipeWrapperFactory) {
            this.recipeWrapperFactory = recipeWrapperFactory;
        }

        public IRecipeWrapper getRecipeWrapper(T recipe) {
            return this.recipeWrapperFactory.getRecipeWrapper(recipe);
        }

        public boolean isRecipeValid(T recipe, StackHelper stackHelper) {
            ItemStack recipeOutput = recipe.getRecipeOutput();
            if (recipeOutput == null || recipeOutput.getItem() == null) {
                String recipeInfo = getInfo(recipe);
                Log.get().error("Recipe has no output. {}", recipeInfo);
                return false;
            }

            int inputCount = getInputCount(recipe, stackHelper);
            if (inputCount == INVALID_COUNT) {
                return false;
            } else if (inputCount > 9) {
                String recipeInfo = getInfo(recipe);
                Log.get().error("Recipe has too many inputs. {}", recipeInfo);
                return false;
            } else if (inputCount == 0) {
                String recipeInfo = getInfo(recipe);
                Log.get().error("Recipe has no inputs. {}", recipeInfo);
                return false;
            }
            return true;
        }

        private String getInfo(T recipe) {
            IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe);
            return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
        }

        private static int getInputCount(IRecipe recipe, StackHelper stackHelper) {
            Object[] inputs = null;

            if (recipe instanceof ShapedRecipes) {
                inputs = ((ShapedRecipes) recipe).recipeItems;
            } else if (recipe instanceof ShapelessRecipes) {
                List<?> list = ((ShapelessRecipes) recipe).recipeItems;
                if (list != null) {
                    inputs = list.toArray();
                }
            } else if (recipe instanceof ShapedOreRecipe) {
                inputs = ((ShapedOreRecipe) recipe).getInput();
            } else if (recipe instanceof ShapelessOreRecipe) {
                List<?> list = ((ShapelessOreRecipe) recipe).getInput();
                if (list != null) {
                    inputs = list.toArray();
                }
            }

            if (inputs == null) {
                return INVALID_COUNT;
            }

            int inputCount = 0;
            for (Object input : inputs) {
                if (input != null) {
                    if (input instanceof List) {
                        if (((List<?>) input).isEmpty()) {
                            return INVALID_COUNT;
                        }
                    }
                    inputCount++;
                }
            }
            return inputCount;
        }
    }
}
