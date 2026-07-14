package ruiseki.jfmuy.api;

import javax.annotation.Nonnull;

public abstract class BlankModPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {

    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJFMUYRuntime jeiRuntime) {

    }
}
