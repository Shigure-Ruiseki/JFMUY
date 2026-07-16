package ruiseki.jfmuy.search;

import java.util.Set;

import ruiseki.jfmuy.config.Config;

public interface ISearchable<T> {

    void getSearchResults(String token, Set<T> results);

    void getAllElements(Set<T> results);

    default Config.SearchMode getMode() {
        return Config.SearchMode.ENABLED;
    }
}
