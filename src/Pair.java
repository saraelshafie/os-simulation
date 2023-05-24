public class Pair<K, V> {
    private K key;
    private V value;

    public Pair() {

    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public String toString() {
        return "(" + (key == null ? "null" : key.toString()) + ", " + (value == null ? "null" : value.toString()) + ")";
    }

    public boolean isReserved() {
        return this.key == null && this.value == null;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
