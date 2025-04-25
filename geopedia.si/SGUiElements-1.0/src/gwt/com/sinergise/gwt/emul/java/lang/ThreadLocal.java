package java.lang;

public class ThreadLocal<T> {
	T val;
	
    protected T initialValue() {
        return null;
    }
    
    public T get() {
        if (val == null) val = initialValue();
        return val;
    }
    
    public void set(T value) {
    	val = value;
    }
    
    public void remove() {
    	val = null;
    }
}