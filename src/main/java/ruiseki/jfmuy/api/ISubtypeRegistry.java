package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Tell JFMUY how to interpret NBT tags and capabilities when comparing and looking up items.
 * Some items have subtypes, most of them use meta values for this and JFMUY handles them by default.
 * If your item has subtypes that depend on NBT or capabilities instead or meta, use this interface so JFMUY can tell
 * those subtypes apart.
 * Get the instance from {@link IJFMUYHelpers#getSubtypeRegistry()}
 */
public interface ISubtypeRegistry {

    /**
     * Tells JFMUY to treat all NBT as relevant to these items' subtypes.
     */
    void useNbtForSubtypes(@Nonnull Item... items);

    /**
     * Add an interpreter to compare item subtypes.
     *
     * @param item        the item that has subtypes.
     * @param interpreter the interpreter for the item.
     */
    void registerNbtInterpreter(@Nonnull Item item, @Nonnull ISubtypeInterpreter interpreter);

    /**
     * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
     * Returns null if the itemStack has information used for subtypes.
     */
    @Nullable
    String getSubtypeInfo(@Nonnull ItemStack itemStack);

    interface ISubtypeInterpreter {

        /**
         * Get the data from an itemStack that is relevant to telling subtypes apart.
         * Returns null if there is no data used for subtypes.
         */
        @Nullable
        String getSubtypeInfo(@Nonnull ItemStack itemStack);
    }
}
