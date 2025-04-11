package io.github.nahkd123.tableschema.jdbc.schema.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

record JdbcFixedStringType(int maxLength) implements JdbcFieldType<String> {
	@Override
	public String sqlType() {
		return "NVARCHAR(%d)".formatted(maxLength);
	}

	@Override
	public String valueToCode(String value) {
		return value == null ? "NULL" : "'%s'".formatted(value.replace("'", "''"));
	}

	@Override
	public void setTo(PreparedStatement statement, int index, String value) throws SQLException {
		statement.setString(index, value);
	}

	@Override
	public String getFrom(ResultSet set, int index) throws SQLException {
		return set.getString(index);
	}
}
