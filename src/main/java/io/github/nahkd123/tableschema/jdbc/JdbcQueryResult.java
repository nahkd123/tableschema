package io.github.nahkd123.tableschema.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.github.nahkd123.tableschema.jdbc.schema.JdbcSchema;
import io.github.nahkd123.tableschema.query.QueryResult;
import io.github.nahkd123.tableschema.schema.Schema;

record JdbcQueryResult<T>(JdbcSchema<?, T> schema, Statement statement, ResultSet set) implements QueryResult<T> {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public T nextRow() {
		try {
			if (!set.next()) return null;
			Object key = schema.primaryKey().type().getFrom(set, 1);
			Object[] fields = new Object[schema.fields().size()];
			for (int i = 0; i < fields.length; i++) fields[i] = schema.fields().get(i).type().getFrom(set, i + 2);
			return (T) ((Schema.Factory) schema.factory()).create(key, List.of(fields));
		} catch (SQLException e) {
			throw new RuntimeException("Error while advancing query result pointer", e);
		}
	}

	@Override
	public void close() {
		try {
			statement.close();
			set.close();
		} catch (SQLException e) {
			throw new RuntimeException("Error while closing query result", e);
		}
	}
}
