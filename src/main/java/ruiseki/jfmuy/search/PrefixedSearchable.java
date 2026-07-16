package ruiseki.jfmuy.search;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import cpw.mods.fml.common.ProgressManager;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.ingredients.IIngredientListElement;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.util.LoggedTimer;
import ruiseki.okcore.datastructure.NonNullList;

public class PrefixedSearchable implements ISearchable<IIngredientListElement<?>>, IBuildable {

    protected final ISearchStorage<IIngredientListElement<?>> searchStorage;
    protected final PrefixInfo prefixInfo;
    protected LoggedTimer timer;

    public PrefixedSearchable(ISearchStorage<IIngredientListElement<?>> searchStorage, PrefixInfo prefixInfo) {
        this.searchStorage = searchStorage;
        this.prefixInfo = prefixInfo;
    }

    public ISearchStorage<IIngredientListElement<?>> getSearchStorage() {
        return searchStorage;
    }

    public Collection<String> getStrings(IIngredientListElement<?> element) {
        return prefixInfo.getStrings(element);
    }

    @Override
    public Config.SearchMode getMode() {
        return prefixInfo.getMode();
    }

    @Override
    public void submit(IIngredientListElement<?> ingredient) {
        Collection<String> strings = prefixInfo.getStrings(ingredient);
        for (String string : strings) {
            searchStorage.put(string, ingredient);
        }
    }

    @Override
    public void submitAll(NonNullList<IIngredientListElement> ingredients) {
        if (IngredientFilter.firstBuild) {
            start();
            ProgressManager.ProgressBar progressBar = null;
            if (!IngredientFilter.rebuild) {
                long modNameCount = ingredients.stream()
                    .map(IIngredientListElement::getModNameForSorting)
                    .distinct()
                    .count();
                progressBar = ProgressManager.push("Indexing ingredients", (int) modNameCount);
            }
            String currentModName = null;
            for (IIngredientListElement ingredient : ingredients) {
                String modname = ingredient.getModNameForSorting();
                if (!Objects.equals(currentModName, modname)) {
                    currentModName = modname;
                    progressBar.step(modname);
                }
                submit(ingredient);
            }
            if (progressBar != null) {
                ProgressManager.pop(progressBar);
            }
            stop();
        } else {
            ProgressManager.ProgressBar progressBar = ProgressManager
                .push("Adding ingredients at runtime", ingredients.size());
            for (IIngredientListElement ingredient : ingredients) {
                progressBar.step(ingredient.getDisplayName());
                submit(ingredient);
            }
        }
    }

    @Override
    public void getSearchResults(String token, Set<IIngredientListElement<?>> results) {
        searchStorage.getSearchResults(token, results);
    }

    @Override
    public void getAllElements(Set<IIngredientListElement<?>> results) {
        searchStorage.getAllElements(results);
    }

    @Override
    public void start() {
        this.timer = new LoggedTimer();
        this.timer.start("Building [" + prefixInfo.getDesc() + "] search tree");
    }

    @Override
    public void stop() {
        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;
        }
    }
}
