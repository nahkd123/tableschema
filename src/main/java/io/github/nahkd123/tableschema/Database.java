package io.github.nahkd123.tableschema;

import io.github.nahkd123.tableschema.jdbc.JdbcDatabase;
import io.github.nahkd123.tableschema.schema.Schema;

/**
 * <p>
 * An interface for accessing the database. Closing this database object will
 * also close the underlying resource/handle that is being used to process
 * queries.
 * </p>
 * 
 * @see JdbcDatabase
 */
public interface Database extends AutoCloseable {
	/**
	 * <p>
	 * Get table reference from this database. The table may or may not present in
	 * database.
	 * </p>
	 * 
	 * @param <K>    Type of primary key.
	 * @param <R>    Type of row object.
	 * @param name   The label/name of table.
	 * @param schema The schema for each row of table.
	 * @return A table reference.
	 * @see Table#migrate(boolean)
	 */
	<K, R> Table<K, R> table(String name, Schema<K, R> schema);

	/**
	 * <p>
	 * Close underlying resource/handle that is being used to process queries.
	 * </p>
	 */
	@Override
	void close();
}
