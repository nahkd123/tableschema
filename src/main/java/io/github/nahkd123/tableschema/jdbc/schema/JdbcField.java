package io.github.nahkd123.tableschema.jdbc.schema;

import java.util.List;
import java.util.function.Function;

import io.github.nahkd123.tableschema.jdbc.schema.type.JdbcFieldType;
import io.github.nahkd123.tableschema.schema.Constraint;
import io.github.nahkd123.tableschema.schema.Field;

public record JdbcField<R, T>(JdbcFieldType<T> type, String label, Function<R, T> getter, List<Constraint<T>> constraints) {
	public static <R, T> JdbcField<R, T> map(Field<R, T> field) {
		return new JdbcField<>(JdbcFieldType.map(field.type()), field.label(), field.getter(), field.constraints());
	}

	public String columnDef(boolean isPrimaryKey) {
		String out = "\"%s\" %s".formatted(label, type.sqlType());
		if (isPrimaryKey) out += " PRIMARY KEY";

		for (Constraint<T> c : constraints) out += switch (c) {
		case Constraint.NotNull<T>() -> " NOT NULL";
		case Constraint.Unique<T>() -> " UNIQUE";
		case Constraint.Defaulted<T>(T v) -> " DEFAULT %s".formatted(type.valueToCode(v));
		default -> throw new IllegalArgumentException("Unexpected value: " + c);
		};

		return out;
	}
}
