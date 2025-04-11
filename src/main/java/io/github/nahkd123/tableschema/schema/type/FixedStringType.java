package io.github.nahkd123.tableschema.schema.type;

public record FixedStringType(int maxLength) implements FieldType<String> {
	@Override
	public FieldType<?> root() {
		return this;
	}
}