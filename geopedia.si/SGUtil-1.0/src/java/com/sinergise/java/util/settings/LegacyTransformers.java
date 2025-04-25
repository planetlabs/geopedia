package com.sinergise.java.util.settings;

import com.sinergise.common.util.server.ServersCluster;
import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.ResolvedType;

public class LegacyTransformers {

	private static final class ServersClusterLegacyTransformer extends MapTransformer.TrGeneric<ServersCluster> {
		public ServersClusterLegacyTransformer() {
		}

		@Override
		public boolean canProcess(Class<?> type) {
			return ServersCluster.class.isAssignableFrom(type);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <C> ResolvedType<C> childType(ResolvedType<ServersCluster> parentType, String name, boolean complex) {
			if ("lastUsedServerIdx".equals(name) 
				|| "serversCount".equals(name)) {
				return (ResolvedType<C>)ResolvedType.create(String.class);
			}
			return super.childType(parentType, name, complex);
		}

		@Override
		protected void setFieldValue(ServersCluster parent, ResolvedType<ServersCluster> parentType, NamedTypedObject<?> value) {
			if ("lastUsedServerIdx".equals(value.name) 
				|| "serversCount".equals(value.name)) {
				return;
			}
			super.setFieldValue(parent, parentType, value);
		}
	}
	static {
		MapTransformer.registerMapTransformer(new ServersClusterLegacyTransformer());
	}
	
	public static void init() {
		//Force static init above
	}
	private LegacyTransformers() {
	}
}
