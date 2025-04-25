package com.sinergise.common.util.server;

import java.io.Serializable;

public interface IsObjectURLProvider extends Serializable {
	public String getObjectURL(String objectName);
}
