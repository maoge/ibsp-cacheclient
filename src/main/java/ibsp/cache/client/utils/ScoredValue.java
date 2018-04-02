package ibsp.cache.client.utils;

/**
 *
 * @author ThinkPad
 */
public class ScoredValue<V> {
    public final double score;
    public final V value;

    public ScoredValue(double score, V value) {
        this.score = score;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScoredValue<?> that = (ScoredValue<?>) o;
        return Double.compare(that.score, score) == 0 && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        long temp = score != +0.0d ? Double.doubleToLongBits(score) : 0L;
        int result = (int) (temp ^ (temp >>> 32));
        return 31 * result + (value != null ? value.hashCode() : 0);
    }

    @Override
    public String toString() {
        return String.format("(%f, %s)", score, value);
    }

	public double getScore() {
		return score;
	}

	public V getValue() {
		return value;
	}
        
}
