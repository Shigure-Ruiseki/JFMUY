package ruiseki.jfmuy.input;

public interface IClickedIngredient<V> {

    V getValue();

    boolean allowsCheating();
}
