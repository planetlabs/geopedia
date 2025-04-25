package com.sinergise.common.util.collections.safe;

public class DefaultStringTypedMap extends StringTypedMap<StringTypedMapKey<?>, Object> {
	private static final long serialVersionUID = 1L;

	public <T> T get(StringTypedMapKey<T> key) {
		return internalGet(key);
	}
	
	public <T> T remove(StringTypedMapKey<T> key) {
		return internalRemove(key);
	}
	
	public <T> T put(StringTypedMapKey<T> key, T value) {
		return internalPut(key, value);
	}

	public Double put(StringTypedMapKey<Double> key, double value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Long put(StringTypedMapKey<Long> key, long value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Integer put(StringTypedMapKey<Integer> key, int value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Float put(StringTypedMapKey<Float> key, float value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Short put(StringTypedMapKey<Short> key, short value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Character put(StringTypedMapKey<Character> key, char value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}

	public Byte put(StringTypedMapKey<Byte> key, byte value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}
	
	public Boolean put(StringTypedMapKey<Boolean> key, boolean value) {
		return readVal(key, map.put(key.getKeyName(), String.valueOf(value)));
	}
}
