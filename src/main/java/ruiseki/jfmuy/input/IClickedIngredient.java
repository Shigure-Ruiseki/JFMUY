package ruiseki.jfmuy.input;

import java.awt.Rectangle;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IClickedIngredient<V> {

    V getValue();

    @Nullable
    Rectangle getArea();

    ItemStack getCheatItemStack();

    void onClickHandled();

    void setOnClickHandler(IOnClickHandler onClickHandler);

    @FunctionalInterface
    interface IOnClickHandler {

        void onClick();
    }
}
