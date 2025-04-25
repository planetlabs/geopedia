package java.util.concurrent.atomic;

public class AtomicInteger extends Number implements java.io.Serializable {
    private int value;

    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    public AtomicInteger() {
    }

    public final int get() {
        return value;
    }

    public final void set(int newValue) {
        value = newValue;
    }

    public final void lazySet(int newValue) {
        value = newValue;
    }

    public final int getAndSet(int newValue) {
    	int ret = value;
    	value = newValue;
    	return ret;
    }

    public final boolean compareAndSet(int expect, int update) {
    	if (value == expect) {
    		value = update;
    		return true;
    	}
    	return false;
    }

    public final boolean weakCompareAndSet(int expect, int update) {
        return compareAndSet(expect, update);
    }

    public final int getAndIncrement() {
    	return getAndSet(value+1);
    }

    public final int getAndDecrement() {
    	return getAndSet(value-1);
    }

    public final int getAndAdd(int delta) {
    	return getAndSet(value + delta);
    }

    public final int incrementAndGet() {
    	return addAndGet(1);
    }

    public final int decrementAndGet() {
    	return addAndGet(-1);
    }

    public final int addAndGet(int delta) {
    	value += delta;
    	return value;
    }

    public String toString() {
        return String.valueOf(value);
    }


    public int intValue() {
        return get();
    }

    public long longValue() {
        return (long)get();
    }

    public float floatValue() {
        return (float)get();
    }

    public double doubleValue() {
        return (double)get();
    }

}
