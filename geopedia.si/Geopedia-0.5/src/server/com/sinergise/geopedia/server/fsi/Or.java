package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.db.QueryPreResult;

public class Or extends QueryCond {
	ArrayList<QueryCond> conds = new ArrayList<QueryCond>();

	public Or() {
		// 
	}

	public Or(QueryCond... conds) {
		for (QueryCond c : conds)
			this.conds.add(c);
	}

	public void add(QueryCond cmp) {
		conds.add(cmp);
	}

	public void getNeeded(HashSet<FieldPath> needed) {
		int n = conds.size();
		for (int a = 0; a < n; a++)
			conds.get(a).getNeeded(needed);
	}

	public void toSQL(QueryPreResult qpr, StringBuilder sb) {
		int size = conds.size();
		if (size == 0)
			throw new IllegalStateException("nothing to OR");
		if (size == 1) {
			conds.get(0).toSQL(qpr, sb);
			return;
		}
		sb.append("((");
		for (int a = 0; a < size; a++) {
			if (a > 0)
				sb.append(") OR (");
			conds.get(a).toSQL(qpr, sb);
		}
		sb.append("))");
	}

	public void setValues(PreparedStatement ps, int[] currIndex)
			throws SQLException {
		int size = conds.size();
		for (int a = 0; a < size; a++) {
			conds.get(a).setValues(ps, currIndex);
		}
	}

	public void setColumnValues(PreparedStatement ps, int[] currIndex)
			throws SQLException {
		int size = conds.size();
		for (int a = 0; a < size; a++) {
			conds.get(a).setColumnValues(ps, currIndex);
		}
	}
}
