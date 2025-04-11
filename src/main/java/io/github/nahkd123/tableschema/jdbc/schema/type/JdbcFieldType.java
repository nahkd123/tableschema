package io.github.nahkd123.tableschema.jdbc.schema.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.github.nahkd123.tableschema.schema.type.FieldType;
import io.github.nahkd123.tableschema.schema.type.FixedStringType;
import io.github.nahkd123.tableschema.schema.type.IdentityType;

public interface JdbcFieldType<T> {
	/**
	 * <p>
	 * Get the SQL type name of this field type.
	 * </p>
	 */
	String sqlType();

	/**
	 * <p>
	 * Convert a value to SQL code that can be used in SQL statements. This method
	 * should only be used for default values (which is not controlled by user). For
	 * values coming from user, use parameterized statement to avoid injection
	 * attack.
	 * </p>
	 * <p>
	 * This will be used for specifying default value for columns.
	 * </p>
	 * 
	 * @param value The value.
	 * @return The value represented as SQL code.
	 */
	String valueToCode(T value);

	/**
	 * <p>
	 * Set value to {@link PreparedStatement} at specified parameter index.
	 * </p>
	 * 
	 * @param statement The prepared statement.
	 * @param index     The parameter index in statement.
	 * @param value     The value to set in statement's parameter.
	 */
	void setTo(PreparedStatement statement, int index, T value) throws SQLException;

	/**
	 * <p>
	 * Get value returned from {@link ResultSet}.
	 * </p>
	 * 
	 * @param set   The result set.
	 * @param index The column index in result set.
	 * @return The value obtained from result set.
	 */
	T getFrom(ResultSet set, int index) throws SQLException;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> JdbcFieldType<T> map(FieldType<T> type) {
		JdbcFieldType<?> root = map0(type.root());
		// @formatter:off
		return new JdbcMappedType(
			root,
			rootVal -> type.mapFromRoot(rootVal),
			javaVal -> ((FieldType) type).mapToRoot(javaVal));
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private static <T> JdbcFieldType<T> map0(FieldType<T> rootType) {
		return switch (rootType) {
		case IdentityType(Class<T> dataType) -> (JdbcFieldType<T>) JdbcIdentityType.TYPES.get(dataType);
		case FixedStringType(int n) -> (JdbcFieldType<T>) new JdbcFixedStringType(n);
		default -> throw new IllegalArgumentException("Unexpected value: " + rootType);
		};
	}
}
