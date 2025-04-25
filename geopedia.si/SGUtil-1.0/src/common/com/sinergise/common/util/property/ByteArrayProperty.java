package com.sinergise.common.util.property;


public class ByteArrayProperty extends ScalarPropertyImpl<byte[]> {
	private static final long serialVersionUID = 1L;

	public ByteArrayProperty() {
	}
	public ByteArrayProperty(byte[] value) {
		super(value);
	}
	
	@Override
	public void setValue(byte[] value) {
		super.setValue(value);
	}
}
