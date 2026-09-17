package ruiseki.jfmuy.api.ingredients;

import com.github.bsideup.jabel.Desugar;

import ruiseki.jfmuy.api.recipe.IIngredientType;

@Desugar
public record ITypedIngredient<T> (IIngredientType<T> type, T ingredient) {}
