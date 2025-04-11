package io.github.nahkd123.tableschema.jdbc.schema.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

record JdbcMappedType<A, B>(JdbcFieldType<A> root, Function<A, B> forward, Function<B, A> backward) implements JdbcFieldType<B> {
	@Override
	public String sqlType() {
		return root.sqlType();
	}

	@Override
	public String valueToCode(B value) {
		return root.valueToCode(backward.apply(value));
	}

	@Override
	public void setTo(PreparedStatement statement, int index, B value) throws SQLException {
		root.setTo(statement, index, backward.apply(value));
	}

	@Override
	public B getFrom(ResultSet set, int index) throws SQLException {
		return forward.apply(root.getFrom(set, index));
	}
}
