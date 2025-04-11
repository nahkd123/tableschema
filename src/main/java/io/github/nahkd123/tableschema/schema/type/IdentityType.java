package io.github.nahkd123.tableschema.schema.type;

public record IdentityType<T>(Class<T> dataType) implements FieldType<T> {
	@Override
	public FieldType<?> root() {
		return this;
	}
}