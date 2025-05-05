package io.github.nahkd123.tableschema.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.github.nahkd123.tableschema.Table;
import io.github.nahkd123.tableschema.jdbc.schema.JdbcIndex;
import io.github.nahkd123.tableschema.jdbc.schema.JdbcSchema;
import io.github.nahkd123.tableschema.jdbc.schema.type.JdbcFieldType;
import io.github.nahkd123.tableschema.query.Filter;
import io.github.nahkd123.tableschema.query.QueryResult;
import io.github.nahkd123.tableschema.query.SortBy;
import io.github.nahkd123.tableschema.schema.Field;

class JdbcTable<K, R> implements Table<K, R> {
	private Connection sql;
	private Field<R, K> primaryKey;
	private String table, activeTable;
	private JdbcSchema<K, R> schema;

	public JdbcTable(Connection sql, String table, Field<R, K> primaryKey, JdbcSchema<K, R> schema) {
		this.sql = sql;
		this.table = table;
		this.primaryKey = primaryKey;
		this.schema = schema;
		this.activeTable = schema.version() == -1 ? table : "%s:v%d".formatted(table, schema.version());
	}

	@Override
	public Table.MigrationReport migrate(boolean backup) {
		try {
			String migrateFrom = null;
			int fromVersion = -1;

			try (var set = sql.getMetaData().getTables(null, null, null, null)) {
				while (set.next()) {
					String tableType = set.getString("TABLE_TYPE");
					if (!tableType.equals("TABLE")) continue;

					String tableName = set.getString("TABLE_NAME");
					if (tableName.equals(activeTable)) {
						if (schema.version() == -1) {
							// -1 always perform migration
							migrateFrom = tableName;
							break;
						} else {
							// Our database have table with target version
							return null;
						}
					}

					if (schema.version() == -1) continue;
					String[] split = tableName.split(":v", 2);

					if (split.length == 2 && split[0].equals(table)) {
						int sourceVersion = Integer.parseInt(split[1]);

						if (sourceVersion > schema.version()) {
							throw new RuntimeException("Schema version is currently %d but found %d in database"
								.formatted(schema.version(), sourceVersion));
						} else if (sourceVersion == schema.version()) return null;

						fromVersion = sourceVersion;
						migrateFrom = tableName;
						break;
					}
				}
			}

			if (migrateFrom == null) {
				try (var s = sql.createStatement()) {
					s.execute(schema.createTableCode(activeTable));
				}
			} else {
				String destination = migrateFrom.equals(activeTable)
					? "migrate:%s".formatted(activeTable)
					: activeTable;
				Set<String> existingColumns = new HashSet<>();

				try (var set = sql.getMetaData().getColumns(null, null, migrateFrom, null)) {
					while (set.next()) existingColumns.add(set.getString("COLUMN_NAME"));
				}

				try (var s = sql.createStatement()) {
					s.execute(schema.createTableCode(destination));
					s.execute(schema.migrateCode(migrateFrom, destination, existingColumns));

					if (backup) {
						String backupName = "%s:backup:%s".formatted(migrateFrom, LocalDateTime.now());
						s.execute("ALTER TABLE \"%s\" RENAME TO \"%s\"".formatted(migrateFrom, backupName));
					} else {
						s.execute("DROP TABLE \"%s\"".formatted(migrateFrom));
					}

					if (!destination.equals(activeTable))
						s.execute("ALTER TABLE \"%s\" RENAME TO \"%s\"".formatted(destination, activeTable));
				}
			}

			try (var s = sql.createStatement()) {
				for (JdbcIndex<R> index : schema.indexes()) s.execute(index.createIndexCode(activeTable));
			}

			return new MigrationReport(backup ? migrateFrom : null, fromVersion, schema.version());
		} catch (SQLException e) {
			throw new RuntimeException("Error while migrating table", e);
		}
	}

	@Override
	public void drop() {
		try (var s = sql.createStatement()) {
			s.execute("DROP TABLE \"%s\"".formatted(activeTable));
		} catch (SQLException e) {
			throw new RuntimeException("Error while dropping table", e);
		}
	}

	@Override
	public Field<R, K> primaryKey() {
		return primaryKey;
	}

	@Override
	public int insert(Collection<R> values) {
		try (var s = sql.prepareStatement(schema.parameterizedInsertCode(activeTable))) {
			int count = 0;

			for (R value : values) {
				schema.setTo(s, 1, value);
				count += s.executeUpdate();
			}

			return count;
		} catch (SQLException e) {
			throw new RuntimeException("Error while inserting to table", e);
		}
	}

	@Override
	public QueryResult<R> query(Filter<R> filter, SortBy<R> ordering) {
		JdbcFilter jdbcFilter = filter != null ? JdbcFilter.map(activeTable, filter, true) : null;
		String sql = "SELECT %s FROM \"%s\"".formatted(schema.columnNamesCode(null), activeTable);
		if (jdbcFilter != null) sql += " WHERE %s".formatted(jdbcFilter.sql());
		if (ordering != null)
			sql += " ORDER BY \"%s\" %s".formatted(ordering.field().label(), switch (ordering.order()) {
			case ASCENDING -> "ASC";
			case DESCENDING -> "DESC";
			});

		try {
			PreparedStatement s = this.sql.prepareStatement(sql);
			if (jdbcFilter != null) jdbcFilter.fillParameters(new JdbcFilter.Filler() {
				@Override
				public <T> void fill(int index, JdbcFieldType<T> valueType, T value) throws SQLException {
					valueType.setTo(s, index + 1, value);
				}
			});

			return new JdbcQueryResult<>(schema, s, s.executeQuery());
		} catch (SQLException e) {
			throw new RuntimeException("Error while querying table", e);
		}
	}

	@Override
	public int update(Collection<R> values) {
		try (PreparedStatement s = sql.prepareStatement(schema.parameterizedUpdateCode(activeTable))) {
			int count = 0;

			for (R value : values) {
				schema.setTo(s, 1, value);
				JdbcSchema.setToPreparedStatement(s, schema.fields().size() + 2, schema.primaryKey(), value);
				count += s.executeUpdate();
			}

			return count;
		} catch (SQLException e) {
			throw new RuntimeException("Error while updating entries in table", e);
		}
	}

	@Override
	public int delete(Filter<R> filter) {
		JdbcFilter jdbcFilter = filter != null ? JdbcFilter.map(activeTable, filter, true) : null;
		String sql = "DELETE FROM \"%s\"".formatted(activeTable);
		if (jdbcFilter != null) sql += " WHERE %s".formatted(jdbcFilter.sql());

		try (PreparedStatement s = this.sql.prepareStatement(sql)) {
			if (jdbcFilter != null) jdbcFilter.fillParameters(new JdbcFilter.Filler() {
				@Override
				public <T> void fill(int index, JdbcFieldType<T> valueType, T value) throws SQLException {
					valueType.setTo(s, index + 1, value);
				}
			});

			return s.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error while querying table", e);
		}
	}
}
