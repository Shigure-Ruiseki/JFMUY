package ruiseki.jfmuy.gui.ghost;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.input.IShowsRecipeFocuses;

public interface IGhostIngredientDragSource extends IShowsRecipeFocuses {

    IIngredientListElement getElementUnderMouse();
}
