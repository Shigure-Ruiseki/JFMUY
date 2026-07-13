package ruiseki.okbase.common.init;

import ruiseki.okcore.init.IInitListener;

public class ModRecipes implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            blockRecipes();
            itemRecipes();
        }
    }

    public static void blockRecipes() {

    }

    public static void itemRecipes() {

    }
}
