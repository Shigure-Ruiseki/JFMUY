package ruiseki.jfmuy.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;

import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;

import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.LoggedTimer;
import ruiseki.okcore.datastructure.NonNullList;

public class AsyncPrefixedSearchable extends PrefixedSearchable {

    private static ExecutorService service;

    public static void startService() {
        service = Executors.newSingleThreadExecutor();
    }

    public static void endService() {
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            service.shutdownNow();
            Thread.currentThread()
                .interrupt();
        }
        service = null;
    }

    private List<IIngredientListElement> leftovers;

    public AsyncPrefixedSearchable(ISearchStorage<IIngredientListElement<?>> searchStorage, PrefixInfo prefixInfo) {
        super(searchStorage, prefixInfo);
    }

    @Override
    public void submitAll(NonNullList<IIngredientListElement> ingredients) {
        service.submit(() -> {
            start();
            for (IIngredientListElement ingredient : ingredients) {
                try {
                    submit(ingredient);
                } catch (ConcurrentRuntimeException e) {
                    Log.get()
                        .error(prefixInfo + " building failed on ingredient: " + ingredient.getDisplayName(), e);
                    if (leftovers == null) {
                        this.leftovers = new ArrayList<>();
                    }
                    this.leftovers.add(ingredient);
                }
            }
            stop();
        });
    }

    @Override
    public void start() {
        this.timer = new LoggedTimer();
        this.timer.start("Building [" + prefixInfo.getDesc() + "] search tree in a separate thread");
    }

    @Override
    public void stop() {
        if (this.timer != null) {
            super.stop();
        }
        if (Minecraft.getMinecraft()
            .func_152345_ab() && this.leftovers != null
            && !this.leftovers.isEmpty()) {
            Log.get()
                .info(
                    "{} search tree had {} errors, moving onto the main thread to process these errors.",
                    prefixInfo,
                    this.leftovers.size());
            this.leftovers.forEach(this::submit);
            this.leftovers = null;
        }
    }

}
