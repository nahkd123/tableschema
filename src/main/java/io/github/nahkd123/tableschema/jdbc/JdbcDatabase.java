package io.github.nahkd123.tableschema.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import io.github.nahkd123.tableschema.Database;
import io.github.nahkd123.tableschema.Table;
import io.github.nahkd123.tableschema.jdbc.schema.JdbcSchema;
import io.github.nahkd123.tableschema.schema.Schema;

/**
 * <p>
 * The TableSchema JDBC wrapper for JDBC connections. Closing the database will
 * also close the connection that was supplied to constructor of this class.
 * </p>
 * 
 * @see Database
 */
public class JdbcDatabase implements Database {
	private Connection sql;

	public JdbcDatabase(Connection sql) {
		this.sql = sql;
	}

	@Override
	public <K, R> Table<K, R> table(String name, Schema<K, R> schema) {
		return new JdbcTable<>(sql, name, schema.primaryKey(), JdbcSchema.map(schema));
	}

	@Override
	public void close() {
		try {
			sql.close();
		} catch (SQLException e) {
			throw new RuntimeException("Error while closing database connection", e);
		}
	}
}
