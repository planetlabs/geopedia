package com.sinergise.geopedia.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBExecutor<T> {
	T execute(Connection conn) throws SQLException ;
}
