package io.github.nahkd123.tableschema.jdbc.schema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.nahkd123.tableschema.schema.Field;
import io.github.nahkd123.tableschema.schema.Schema;
import io.github.nahkd123.tableschema.schema.Schema.Factory;

public record JdbcSchema<K, R>(JdbcField<R, ?> primaryKey, List<JdbcField<R, ?>> fields, Factory<K, R> factory, List<JdbcIndex<R>> indexes, int version) {
	public static <K, R> JdbcSchema<K, R> map(Schema<K, R> schema) {
		JdbcField<R, K> primaryKey = JdbcField.map(schema.primaryKey());
		List<JdbcField<R, ?>> fields = new ArrayList<>();
		for (Field<R, ?> field : schema.fields()) fields.add(JdbcField.map(field));
		Factory<K, R> factory = schema.factory();
		List<JdbcIndex<R>> indexes = schema.indexes().stream().map(JdbcIndex::map).toList();
		int version = schema.version();
		return new JdbcSchema<>(primaryKey, fields, factory, indexes, version);
	}

	public String createTableCode(String table) {
		if (fields.size() > 0) return "CREATE TABLE \"%s\" (%s, %s)".formatted(
			table,
			primaryKey.columnDef(true),
			fields.stream().map(f -> f.columnDef(false)).collect(Collectors.joining(", ")));
		return "CREATE TABLE \"%s\" (%s)".formatted(table, primaryKey.columnDef(true));
	}

	public String columnNamesCode(String table) {
		List<JdbcField<R, ?>> fields = new ArrayList<>();
		fields.add(this.primaryKey);
		fields.addAll(this.fields);
		return columnNamesCode(table, fields);
	}

	public static <R> String columnNamesCode(String table, List<JdbcField<R, ?>> fields) {
		return fields.stream().map(f -> table != null
			? "\"%s\".\"%s\"".formatted(table, f.label())
			: "\"%s\"".formatted(f.label()))
			.collect(Collectors.joining(", "));
	}

	public String parameterizedInsertCode(String table) {
		List<JdbcField<R, ?>> fields = new ArrayList<>();
		fields.add(this.primaryKey);
		fields.addAll(this.fields);
		return "INSERT INTO \"%s\" (%s) VALUES (%s)".formatted(
			table,
			columnNamesCode(null, fields),
			fields.stream().map(f -> "?").collect(Collectors.joining(", ")));
	}

	public String parameterizedUpdateCode(String table) {
		List<JdbcField<R, ?>> fields = new ArrayList<>();
		fields.add(this.primaryKey);
		fields.addAll(this.fields);
		return "UPDATE \"%s\" SET %s WHERE \"%s\".\"%s\" = ?".formatted(
			table,
			fields.stream().map(f -> "\"%s\" = ?".formatted(f.label())).collect(Collectors.joining(", ")),
			table,
			primaryKey.label());
	}

	public void setTo(PreparedStatement s, int baseIndex, R value) throws SQLException {
		setToPreparedStatement(s, baseIndex, primaryKey, value);
		for (int i = 0; i < fields.size(); i++)
			setToPreparedStatement(s, baseIndex + i + 1, fields.get(i), value);
	}

	public static <T, R> void setToPreparedStatement(PreparedStatement s, int index, JdbcField<R, T> field, R row) throws SQLException {
		T value = field.getter().apply(row);
		field.type().setTo(s, index, value);
	}

	public String migrateCode(String from, String to, Set<String> existingColumns) {
		if (!existingColumns.contains(primaryKey.label())) {
			throw new IllegalArgumentException("Existing columns does not have primary key column '%s'"
				.formatted(primaryKey.label()));
		}

		Set<String> columns = new HashSet<>();
		columns.add(primaryKey.label());
		columns.addAll(fields.stream().map(JdbcField::label).toList());
		columns.retainAll(existingColumns);

		return "INSERT INTO \"%s\" (%s) SELECT %s FROM \"%s\"".formatted(
			to, columns.stream().map(c -> "\"%s\"".formatted(c)).collect(Collectors.joining(", ")),
			columns.stream().map(c -> "\"%s\".\"%s\"".formatted(from, c)).collect(Collectors.joining(", ")), from);
	}
}
