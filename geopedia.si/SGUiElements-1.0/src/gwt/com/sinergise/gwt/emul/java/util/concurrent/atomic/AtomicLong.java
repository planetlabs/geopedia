package java.util.concurrent.atomic;

public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    private volatile long value;

    public AtomicLong(long initialValue) {
        value = initialValue;
    }
    public AtomicLong() {
    }

    public final long get() {
        return value;
    }

    public final void set(long newValue) {
        value = newValue;
    }

    public final void lazySet(long newValue) {
        set(newValue);
    }

    public final long getAndSet(long newValue) {
    	long ret = value;
    	value = newValue;
    	return ret;
    }

    public final boolean compareAndSet(long expect, long update) {
    	if (value == expect) {
    		value = update;
    		return true;
    	}
        return false;
    }

    public final boolean weakCompareAndSet(long expect, long update) {
        return compareAndSet(expect, update);
    }

    public final long getAndIncrement() {
    	return getAndSet(value + 1);
    }

    public final long getAndDecrement() {
    	return getAndSet(value - 1);
    }

    public final long getAndAdd(long delta) {
    	return getAndSet(value + delta);
    }

    public final long incrementAndGet() {
    	return addAndGet(1);
    }

    public final long decrementAndGet() {
    	return addAndGet(-1);
    }

    public final long addAndGet(long delta) {
    	value += delta;
    	return value;
    }

    public String toString() {
        return Long.toString(get());
    }

    public int intValue() {
        return (int)get();
    }

    public long longValue() {
        return get();
    }

    public float floatValue() {
        return (float)get();
    }

    public double doubleValue() {
        return (double)get();
    }

}
