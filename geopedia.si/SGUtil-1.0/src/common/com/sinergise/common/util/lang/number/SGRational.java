package com.sinergise.common.util.lang.number;

public interface SGRational extends SGNumber {
	@Override
	public SGNumber abs();
	@Override
	public SGRational opposite();
	public SGRational plus(SGRational other);
	public SGRational times(SGRational other);
}