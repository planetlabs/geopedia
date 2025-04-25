package com.sinergise.common.util.geom;


public interface HasEnvelope {
	public static class Util {
		private Util() {}
		public static Envelope getMBR(Iterable<? extends HasEnvelope> items) {
			EnvelopeBuilder eb = new EnvelopeBuilder();
			for (HasEnvelope item : items) {
				eb.expandToInclude(item.getEnvelope());
			}
			return eb.getEnvelope();
		}
	}
	Envelope getEnvelope();

}
