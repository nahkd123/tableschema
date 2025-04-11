package io.github.nahkd123.tableschema.schema.type;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface FieldType<T> {
	/**
	 * <p>
	 * The first type in the type map chain. This will be used for deriving database
	 * field type.
	 * </p>
	 * 
	 * @return The root type.
	 */
	FieldType<?> root();

	@SuppressWarnings("unchecked")
	default T mapFromRoot(Object root) {
		return (T) root;
	}

	default Object mapToRoot(T mapped) {
		return mapped;
	}

	default <B> FieldType<B> map(Function<T, B> forward, Function<B, T> backward) {
		return new MappedType<>(this, forward, backward);
	}

	static FieldType<String> fixedString(int maxLength) {
		return new FixedStringType(maxLength);
	}

	static <T> FieldType<T> ofEnum(T[] values, Function<T, String> deriveId) {
		Map<String, T> forwardMap = new HashMap<>();
		for (T value : values) forwardMap.put(deriveId.apply(value), value);
		int maxLength = deriveId.apply(values[0]).length();
		for (int i = 1; i < values.length; i++) maxLength = Math.max(maxLength, deriveId.apply(values[i]).length());
		return fixedString(maxLength).map(forwardMap::get, deriveId);
	}

	static <T> FieldType<T> ofEnum(T[] values) {
		return ofEnum(values, e -> e.toString());
	}

	FieldType<Byte> BYTE = new IdentityType<>(byte.class);
	FieldType<Short> SHORT = new IdentityType<>(short.class);
	FieldType<Integer> INT = new IdentityType<>(int.class);
	FieldType<Long> LONG = new IdentityType<>(long.class);
	FieldType<Float> FLOAT = new IdentityType<>(float.class);
	FieldType<Double> DOUBLE = new IdentityType<>(double.class);
	FieldType<String> BIGTEXT = new IdentityType<>(String.class);
	FieldType<UUID> UUID = fixedString(36).map(java.util.UUID::fromString, java.util.UUID::toString);
}
