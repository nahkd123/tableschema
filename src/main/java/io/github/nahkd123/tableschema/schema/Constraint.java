package io.github.nahkd123.tableschema.schema;

/**
 * <p>
 * Constraint limits values a field can accept.
 * </p>
 * 
 * @param <T> Type of value.
 * @see #notNull()
 * @see #unique()
 * @see #defaulted(Object)
 */
public interface Constraint<T> {
	/**
	 * <p>
	 * Value must not be {@code null}.
	 * </p>
	 */
	static <T> Constraint<T> notNull() {
		return new NotNull<>();
	}

	/**
	 * <p>
	 * Value must be <em>unique</em>. That is, when inserting or alternating the
	 * field of certain row, it must not present in other rows.
	 * </p>
	 */
	static <T> Constraint<T> unique() {
		return new Unique<>();
	}

	/**
	 * <p>
	 * Fallback to default value. This is mainly used during migration process,
	 * where new fields may be added but not previously present on other fields, and
	 * the field must not be {@code null}.
	 * </p>
	 */
	static <T> Constraint<T> defaulted(T value) {
		return new Defaulted<>(value);
	}

	record NotNull<T>() implements Constraint<T> {
	}

	record Unique<T>() implements Constraint<T> {
	}

	record Defaulted<T>(T value) implements Constraint<T> {
	}
}
