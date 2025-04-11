package io.github.nahkd123.tableschema.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.github.nahkd123.tableschema.schema.type.FieldType;

public record Field<R, T>(FieldType<T> type, String label, Function<R, T> getter, List<Constraint<T>> constraints) {
	public Field(FieldType<T> type, String label, Function<R, T> getter) {
		this(type, label, getter, List.of());
	}

	public Field<R, T> withConstraints(List<Constraint<T>> constraints) {
		return new Field<>(type, label, getter, constraints);
	}

	/**
	 * <p>
	 * Append constraint to this field.
	 * </p>
	 * 
	 * @param constraint The constraint to append.
	 * @return A new field with appended constraint.
	 */
	public Field<R, T> with(Constraint<T> constraint) {
		List<Constraint<T>> constraints = new ArrayList<>(this.constraints.size() + 1);
		constraints.addAll(this.constraints);
		constraints.add(constraint);
		return withConstraints(Collections.unmodifiableList(constraints));
	}
}
