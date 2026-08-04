package ruiseki.jfmuy.search;

import java.io.PrintWriter;

import ruiseki.jfmuy.api.search.ISearchIndex;

interface IPrintableSearchIndex<T> extends ISearchIndex<T> {

    void printTree(PrintWriter out, boolean includeSuffixLinks);
}
