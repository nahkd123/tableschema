package io.github.nahkd123.tableschema.schema;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>
 * Schema describe the fields to use for storing or querying in table, as well
 * as indexes for speeding up query speed (which is optional and heavily depends
 * on implementation).
 * </p>
 * 
 * @param <K> Type of key.
 * @param <R> Type of object.
 * @see #of(Field, Factory)
 * @see #of(Field, Field, Factory1F)
 */
public record Schema<K, R>(Field<R, K> primaryKey, List<Field<R, ?>> fields, Factory<K, R> factory, List<Index<R>> indexes, int version) {
	public Schema(Field<R, K> primaryKey, List<Field<R, ?>> fields, Factory<K, R> factory) {
		this(primaryKey, fields, factory, List.of(), -1);
	}

	public Schema<K, R> withIndexes(List<Index<R>> indexes) {
		return new Schema<>(primaryKey, fields, factory, indexes, version);
	}

	@SuppressWarnings("unchecked")
	public <T> Field<R, T> field(String fieldLabel) {
		if (fieldLabel.equals(primaryKey.label())) return (Field<R, T>) primaryKey;
		for (Field<R, ?> field : fields) if (field.label() == fieldLabel) return (Field<R, T>) field;
		throw new NoSuchElementException("No such field with label '%s'".formatted(fieldLabel));
	}

	/**
	 * <p>
	 * Specify a new version for schema. If the version number is {@code -1}, the
	 * database will always perform migration. You should always bump the version
	 * number by 1 on every schema update.
	 * </p>
	 * 
	 * @param version The version number.
	 */
	public Schema<K, R> withVersion(int version) {
		return new Schema<>(primaryKey, fields, factory, indexes, version);
	}

	public static interface Factory<K, R> {
		/**
		 * <p>
		 * Create a new object.
		 * </p>
		 * 
		 * @param key    The primary key of object.
		 * @param fields A list of field values that aren't primary key.
		 * @return A new object.
		 */
		R create(K key, List<?> fields);
	}

	public static <K, R> Schema<K, R> of(Field<R, K> k, Factory<K, R> factory) {
		return new Schema<>(k, Collections.emptyList(), factory);
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Factory1F<K, F1, R> factory) {
		return new Schema<>(k, List.of(f1), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Factory2F<K, F1, F2, R> factory) {
		return new Schema<>(k, List.of(f1, f2), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Factory3F<K, F1, F2, F3, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, F4, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Field<R, F4> f4, Factory4F<K, F1, F2, F3, F4, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3, f4), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2), (F4) fields.get(3));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, F4, F5, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Field<R, F4> f4, Field<R, F5> f5, Factory5F<K, F1, F2, F3, F4, F5, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3, f4, f5), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2), (F4) fields.get(3),
				(F5) fields.get(4));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, F4, F5, F6, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Field<R, F4> f4, Field<R, F5> f5, Field<R, F6> f6, Factory6F<K, F1, F2, F3, F4, F5, F6, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3, f4, f5, f6), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2), (F4) fields.get(3),
				(F5) fields.get(4), (F6) fields.get(5));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, F4, F5, F6, F7, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Field<R, F4> f4, Field<R, F5> f5, Field<R, F6> f6, Field<R, F7> f7, Factory7F<K, F1, F2, F3, F4, F5, F6, F7, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3, f4, f5, f6, f7), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2), (F4) fields.get(3),
				(F5) fields.get(4), (F6) fields.get(5), (F7) fields.get(6));
		});
	}

	@SuppressWarnings("unchecked")
	public static <K, F1, F2, F3, F4, F5, F6, F7, F8, R> Schema<K, R> of(Field<R, K> k, Field<R, F1> f1, Field<R, F2> f2, Field<R, F3> f3, Field<R, F4> f4, Field<R, F5> f5, Field<R, F6> f6, Field<R, F7> f7, Field<R, F8> f8, Factory8F<K, F1, F2, F3, F4, F5, F6, F7, F8, R> factory) {
		return new Schema<>(k, List.of(f1, f2, f3, f4, f5, f6, f7, f8), (key, fields) -> {
			return factory.create(key, (F1) fields.get(0), (F2) fields.get(1), (F3) fields.get(2), (F4) fields.get(3),
				(F5) fields.get(4), (F6) fields.get(5), (F7) fields.get(6), (F8) fields.get(7));
		});
	}

	@FunctionalInterface
	public static interface Factory1F<K, F1, R> {
		R create(K key, F1 f1);
	}

	@FunctionalInterface
	public static interface Factory2F<K, F1, F2, R> {
		R create(K key, F1 f1, F2 f2);
	}

	@FunctionalInterface
	public static interface Factory3F<K, F1, F2, F3, R> {
		R create(K key, F1 f1, F2 f2, F3 f3);
	}

	@FunctionalInterface
	public static interface Factory4F<K, F1, F2, F3, F4, R> {
		R create(K key, F1 f1, F2 f2, F3 f3, F4 f4);
	}

	@FunctionalInterface
	public static interface Factory5F<K, F1, F2, F3, F4, F5, R> {
		R create(K key, F1 f1, F2 f2, F3 f3, F4 f4, F5 f5);
	}

	@FunctionalInterface
	public static interface Factory6F<K, F1, F2, F3, F4, F5, F6, R> {
		R create(K key, F1 f1, F2 f2, F3 f3, F4 f4, F5 f5, F6 f6);
	}

	@FunctionalInterface
	public static interface Factory7F<K, F1, F2, F3, F4, F5, F6, F7, R> {
		R create(K key, F1 f1, F2 f2, F3 f3, F4 f4, F5 f5, F6 f6, F7 f7);
	}

	@FunctionalInterface
	public static interface Factory8F<K, F1, F2, F3, F4, F5, F6, F7, F8, R> {
		R create(K key, F1 f1, F2 f2, F3 f3, F4 f4, F5 f5, F6 f6, F7 f7, F8 f8);
	}
}
