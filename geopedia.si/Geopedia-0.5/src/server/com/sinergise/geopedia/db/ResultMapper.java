package com.sinergise.geopedia.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultMapper<T> {

	
	T createNewTarget();
	void map(DB dbInstance, ResultSet from, T to) throws SQLException;
	
}
