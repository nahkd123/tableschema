package io.github.nahkd123.tableschema.schema.type;

import java.util.function.Function;

record MappedType<A, B>(FieldType<A> source, Function<A, B> forward, Function<B, A> backward) implements FieldType<B> {
	@Override
	public FieldType<?> root() {
		return source.root();
	}

	@Override
	public B mapFromRoot(Object root) {
		return forward.apply(source.mapFromRoot(root));
	}

	@Override
	public Object mapToRoot(B mapped) {
		return source.mapToRoot(backward.apply(mapped));
	}
}