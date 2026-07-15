package ruiseki.jfmuy.api.ingredients;

/**
 * Put this interface on your {@link net.minecraft.item.Item} to skip JEI's render optimizations.
 *
 * This is useful for baked models that use ASM and do not use {@link IBakedModel#isBuiltInRenderer}.
 * If your model does not use ASM it should work fine, please report a bug instead of using this interface.
 */
public interface ISlowRenderItem {
}
