package io.github.nahkd123.tableschema.jdbc.schema.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

record JdbcIdentityType<T>(Class<T> type, String sqlType) implements JdbcFieldType<T> {
	public static final Map<Class<?>, JdbcIdentityType<?>> TYPES = Map.of(
		byte.class, new JdbcIdentityType<>(byte.class, "TINYINT"),
		short.class, new JdbcIdentityType<>(short.class, "SMALLINT"),
		int.class, new JdbcIdentityType<>(int.class, "INTEGER"),
		long.class, new JdbcIdentityType<>(long.class, "BIGINT"),
		float.class, new JdbcIdentityType<>(float.class, "REAL"),
		double.class, new JdbcIdentityType<>(double.class, "FLOAT"),
		String.class, new JdbcIdentityType<>(String.class, "NVARCHAR"));

	@Override
	public String valueToCode(T value) {
		if (value == null) return "NULL";
		if (value instanceof Number num) return num.toString();
		return "'%s'".formatted(value.toString().replaceAll("'", "''"));
	}

	@Override
	public void setTo(PreparedStatement statement, int index, T value) throws SQLException {
		if (type == byte.class) statement.setByte(index, (byte) value);
		if (type == short.class) statement.setShort(index, (short) value);
		if (type == int.class) statement.setInt(index, (int) value);
		if (type == long.class) statement.setLong(index, (long) value);
		if (type == float.class) statement.setFloat(index, (float) value);
		if (type == double.class) statement.setDouble(index, (double) value);
		if (type == String.class) statement.setString(index, (String) value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getFrom(ResultSet set, int index) throws SQLException {
		Object ret = null;
		if (type == byte.class) ret = set.getByte(index);
		if (type == short.class) ret = set.getShort(index);
		if (type == int.class) ret = set.getInt(index);
		if (type == long.class) ret = set.getLong(index);
		if (type == float.class) ret = set.getFloat(index);
		if (type == double.class) ret = set.getDouble(index);
		if (type == String.class) ret = set.getString(index);
		return set.wasNull() ? null : (T) ret;
	}
}
