package ruiseki.jfmuy.gui;

import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ingredients.IIngredientHelper;
import ruiseki.jfmuy.api.recipe.IFocus;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.LegacyUtil;

public class Focus<V> implements IFocus<V> {

    private final Mode mode;
    private final V value;

    public Focus(Mode mode, V value) {
        this.mode = mode;
        IIngredientHelper<V> ingredientHelper = Internal.getIngredientRegistry()
            .getIngredientHelper(value);
        this.value = LegacyUtil.getIngredientCopy(value, ingredientHelper);
        checkInternal(this);
    }

    // INTERNAL USAGE ONLY
    protected Focus() {
        this.mode = null;
        this.value = null;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    /**
     * Make sure any IFocus coming in through API calls is validated and turned into JFMUY's Focus.
     */
    public static <V> Focus<V> check(IFocus<V> focus) {
        ErrorUtil.checkNotNull(focus, "focus");
        if (focus instanceof Focus) {
            checkInternal(focus);
            return (Focus<V>) focus;
        }
        return new Focus<>(focus.getMode(), focus.getValue());
    }

    private static void checkInternal(IFocus<?> focus) {
        ErrorUtil.checkNotNull(focus.getMode(), "focus mode");
        Object value = focus.getValue();
        ErrorUtil.checkIsValidIngredient(value, "focus value");
    }
}
