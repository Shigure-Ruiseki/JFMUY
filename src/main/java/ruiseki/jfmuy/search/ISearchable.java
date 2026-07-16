package ruiseki.jfmuy.search;

import it.unimi.dsi.fastutil.ints.IntSet;
import ruiseki.jfmuy.config.Config.SearchMode;

public interface ISearchable {

    IntSet search(String word);

    default SearchMode getMode() {
        return SearchMode.ENABLED;
    }
}
