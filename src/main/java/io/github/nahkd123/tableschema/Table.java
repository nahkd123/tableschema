package io.github.nahkd123.tableschema;

import java.util.Collection;
import java.util.Collections;

import io.github.nahkd123.tableschema.query.Filter;
import io.github.nahkd123.tableschema.query.QueryResult;
import io.github.nahkd123.tableschema.query.SortBy;
import io.github.nahkd123.tableschema.schema.Field;

/**
 * <p>
 * Table is a collection of objects represented in a tabular data structure
 * (like SQL or CSV for example). Each object have its own unique primary key,
 * which will be used as reference for querying objects as well as update and
 * delete.
 * </p>
 * 
 * @param <K> Type of object's key.
 * @param <R> Type of object.
 */
public interface Table<K, R> {
	/**
	 * <p>
	 * Auto-migrate the table to new schema if needed. This will reconfigure the
	 * columns, constraints and indexes. If the table is not yet existed, it will
	 * create a new one.
	 * </p>
	 * 
	 * @param backup Whether to retain a backup of old table.
	 * @return Migration report, or {@code null} if migration is skipped.
	 */
	MigrationReport migrate(boolean backup);

	/**
	 * <p>
	 * Table migration report. This may be used to rollback to previous version in
	 * case something went wrong.
	 * </p>
	 */
	record MigrationReport(String backupName, int fromVersion, int toVersion) {
	}

	/**
	 * <p>
	 * Drop this table.
	 * </p>
	 */
	void drop();

	/**
	 * <p>
	 * Get the field for primary key of this table.
	 * </p>
	 * 
	 * @return The field of primary key.
	 */
	Field<R, K> primaryKey();

	/**
	 * <p>
	 * Insert multiple rows to this table.
	 * </p>
	 * 
	 * @param values A collection of rows to insert.
	 * @return Number of rows actually inserted.
	 */
	int insert(Collection<R> values);

	/**
	 * <p>
	 * Insert a single row to this table.
	 * </p>
	 * 
	 * @param value A single row.
	 * @return Whether the row is actually inserted.
	 */
	default boolean insert(R value) {
		return insert(Collections.singleton(value)) == 1;
	}

	/**
	 * <p>
	 * Query rows in this table. Use {@code null} on both parameters to query entire
	 * table.
	 * </p>
	 * 
	 * @param filter   Field filter. Use {@code null} to accept all rows.
	 * @param ordering Ordering of queried rows. Use {@code null} to use table's
	 *                 natural ordering.
	 * @return Query result.
	 */
	QueryResult<R> query(Filter<R> filter, SortBy<R> ordering);

	/**
	 * <p>
	 * Query row(s) with specified primary key in this table.
	 * </p>
	 * 
	 * @param key The key to select exact row with this key.
	 * @return Query result.
	 */
	default QueryResult<R> query(K key) {
		return query(Filter.eq(primaryKey(), key), null);
	}

	/**
	 * <p>
	 * Bulk update multiple rows in this table. Rows whose primary key isn't stored
	 * in this table will be ignored.
	 * </p>
	 * 
	 * @param values The rows with new values.
	 * @return Number of rows actually updated.
	 */
	int update(Collection<R> values);

	/**
	 * <p>
	 * Update a single row with specific primary key in this table. If the primary
	 * key of specified row is not present in table, update will be ignored.
	 * </p>
	 * 
	 * @param value The row data with matching primary key.
	 * @return Whether the row is actually updated.
	 */
	default boolean update(R value) {
		return update(Collections.singleton(value)) == 1;
	}

	/**
	 * <p>
	 * Delete multiple rows from this table.
	 * </p>
	 * 
	 * @param filter Field filter. Use {@code null} to accept all rows (which nuke
	 *               entire table!).
	 * @return Number of rows actually deleted.
	 */
	int delete(Filter<R> filter);

	default boolean delete(K key) {
		return delete(Filter.eq(primaryKey(), key)) == 1;
	}

	default boolean deleteRow(R value) {
		return delete(primaryKey().getter().apply(value));
	}

	default int delete(Collection<K> keys) {
		return delete(new Filter.Or<>(keys.stream()
			.map(key -> Filter.eq(primaryKey(), key))
			.toList()));
	}

	default int deleteRows(Collection<R> values) {
		return delete(values.stream().map(primaryKey().getter()::apply).toList());
	}
}
