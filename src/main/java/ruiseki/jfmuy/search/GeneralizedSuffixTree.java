package ruiseki.jfmuy.search;

import java.io.PrintWriter;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import ruiseki.jfmuy.util.Substring;

public class GeneralizedSuffixTree<T> implements IPrintableSearchIndex<T> {

    private static final Logger LOGGER = LogManager.getLogger(GeneralizedSuffixTree.class);

    private final Node.Root<T> root = new Node.Root<>();
    private Node<T> activeLeaf = root;

    @Override
    public void getSearchResults(String word, Set<T> results) {
        if (word == null || word.isEmpty()) {
            LOGGER.warn("[GST] Token search bi rong hoac NULL!");
            return;
        }
        try {
            Node<T> tmpNode = searchNode(root, word);
            if (tmpNode == null) {
                return;
            }
            tmpNode.getData(results);
        } catch (Throwable t) {
            LOGGER.error("[GST] Loi trong qua trinh getSearchResults voi word='{}'", word, t);
            throw t;
        }
    }

    @Override
    public void getAllElements(Set<T> results) {
        try {
            root.getData(results);
        } catch (Throwable t) {
            LOGGER.error("[GST] Loi trong qua trinh getAllElements!", t);
            throw t;
        }
    }

    @Nullable
    private static <T> Node<T> searchNode(final Node<T> root, final String word) {
        Node<T> currentNode = root;
        Substring wordSubstring = new Substring(word);

        while (!wordSubstring.isEmpty()) {
            Node<T> currentEdge = currentNode.getEdge(wordSubstring);
            if (currentEdge == null) {
                return null;
            }

            int lenToMatch = Math.min(wordSubstring.length(), currentEdge.length());
            if (!currentEdge.regionMatches(wordSubstring, lenToMatch)) {
                return null;
            }
            if (lenToMatch == wordSubstring.length()) {
                return currentEdge;
            }

            currentNode = currentEdge;
            wordSubstring = wordSubstring.substring(lenToMatch);
        }

        return null;
    }

    @Override
    public void put(String key, T value) {
        if (key == null) {
            LOGGER.error("[GST] Key truyen vao put() bi NULL! Value: {}", value);
            return;
        }

        try {
            activeLeaf = root;
            Node<T> s = root;

            final Substring keyString = new Substring(key);
            Substring text = keyString.shorten(keyString.length());

            for (int i = 0; i < key.length(); i++) {
                Substring rest = keyString.substring(i);
                Pair<Node<T>, Substring> active = update(s, text, key.charAt(i), rest, value);
                s = active.getLeft();
                text = active.getRight();
            }

            if (null == activeLeaf.getSuffix() && activeLeaf != root && activeLeaf != s) {
                activeLeaf.setSuffix(s);
            }
        } catch (Throwable t) {
            LOGGER.error("[GST] CRASH / LOI khi dang put key='{}', value='{}'", key, value, t);
            throw t;
        }
    }

    private static <T> Pair<Boolean, Node<T>> testAndSplit(Node<T> startNode, Substring searchString, final char t,
        final Substring remainder, final T value) {

        if (remainder.isEmpty()) {
            LOGGER.error("[GST:testAndSplit] remainder bi rống! searchString='{}', char='{}'", searchString, t);
        }
        if (!remainder.isEmpty() && remainder.charAt(0) != t) {
            LOGGER.error("[GST:testAndSplit] remainder.charAt(0) [{}] != t [{}]!", remainder.charAt(0), t);
        }

        Pair<Node<T>, Substring> canonizeResult = canonize(startNode, searchString);
        startNode = canonizeResult.getLeft();
        searchString = canonizeResult.getRight();

        if (!searchString.isEmpty()) {
            Node<T> g = startNode.getEdge(searchString);
            if (g == null) {
                LOGGER.error("[GST:testAndSplit] Node g bi NULL voi searchString='{}'", searchString);
            } else {
                if (g.length() > searchString.length() && g.charAt(searchString.length()) == t) {
                    return Pair.of(true, startNode);
                }
                Node<T> newNode = splitNode(startNode, g, searchString);
                return Pair.of(false, newNode);
            }
        }

        Node<T> e = startNode.getEdge(remainder);
        if (e == null) {
            return Pair.of(false, startNode);
        }

        if (e.startsWith(remainder)) {
            if (e.length() == remainder.length()) {
                e.addRef(value);
                return Pair.of(true, startNode);
            } else {
                Node<T> newNode = splitNode(startNode, e, remainder);
                newNode.addRef(value);
                return Pair.of(false, startNode);
            }
        } else {
            return Pair.of(true, startNode);
        }
    }

    private static <T> Node<T> splitNode(Node<T> s, Node<T> e, Substring splitFirstPart) {
        Substring splitSecondPart = e.substring(splitFirstPart.length());

        Node<T> r = new Node<>(splitFirstPart);
        s.addEdge(r);
        e.set(splitSecondPart);
        r.addEdge(e);

        return r;
    }

    private static <T> Pair<Node<T>, Substring> canonize(final Node<T> s, final Substring input) {
        Node<T> currentNode = s;
        Substring remainder = input;

        while (!remainder.isEmpty()) {
            Node<T> nextEdge = currentNode.getEdge(remainder);
            if (nextEdge == null || !nextEdge.isPrefix(remainder)) {
                break;
            }
            currentNode = nextEdge;
            remainder = remainder.substring(nextEdge.length());
        }

        return Pair.of(currentNode, remainder);
    }

    private Pair<Node<T>, Substring> update(Node<T> s, final Substring stringPart, final char newChar,
        final Substring rest, final T value) {

        Substring k = stringPart.append(newChar);
        Node<T> oldRoot = root;

        Pair<Boolean, Node<T>> ret = testAndSplit(s, stringPart, newChar, rest, value);
        Node<T> r = ret.getRight();
        boolean endpoint = ret.getLeft();

        Node<T> leaf;
        while (!endpoint) {
            Node<T> tempEdge = r.getEdge(newChar);
            if (tempEdge != null) {
                leaf = tempEdge;
            } else {
                leaf = new Node<>(rest);
                leaf.addRef(value);
                r.addEdge(leaf);
            }

            if (activeLeaf != root) {
                activeLeaf.setSuffix(leaf);
            }
            activeLeaf = leaf;

            if (oldRoot != root) {
                oldRoot.setSuffix(r);
            }

            oldRoot = r;

            if (null == s.getSuffix()) {
                k = k.substring(1);
            } else {
                Pair<Node<T>, Substring> canonized = canonize(s.getSuffix(), safeCutLastChar(k));
                char nextChar = k.charAt(k.length() - 1);
                s = canonized.getLeft();
                k = canonized.getRight()
                    .append(nextChar);
            }

            ret = testAndSplit(s, safeCutLastChar(k), newChar, rest, value);
            endpoint = ret.getLeft();
            r = ret.getRight();
        }

        if (oldRoot != root) {
            oldRoot.setSuffix(r);
        }

        return canonize(s, k);
    }

    private static Substring safeCutLastChar(Substring subString) {
        if (subString.isEmpty()) {
            return subString;
        }
        return subString.shorten(1);
    }

    @Override
    public String statistics() {
        return "GeneralizedSuffixTree:" + "\nNode size stats: \n"
            + this.root.nodeSizeStats()
            + "\nNode edge stats: \n"
            + this.root.nodeEdgeStats();
    }

    @Override
    public void printTree(PrintWriter out, boolean includeSuffixLinks) {
        root.printTree(out, includeSuffixLinks);
    }
}
