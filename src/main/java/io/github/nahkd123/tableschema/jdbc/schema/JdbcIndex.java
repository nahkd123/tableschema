package io.github.nahkd123.tableschema.jdbc.schema;

import java.util.List;
import java.util.stream.Collectors;

import io.github.nahkd123.tableschema.jdbc.JdbcFilter;
import io.github.nahkd123.tableschema.query.SortBy;
import io.github.nahkd123.tableschema.schema.Field;
import io.github.nahkd123.tableschema.schema.Index;

public record JdbcIndex<R>(String name, List<JdbcFilter> filters, List<SortBy<R>> ordering, List<String> fields) {
	public static <R> JdbcIndex<R> map(Index<R> index) {
		String name = index.name();
		List<JdbcFilter> filters = index.filters().stream().map(f -> JdbcFilter.map(null, f, false)).toList();
		List<SortBy<R>> ordering = index.ordering();
		List<String> fields = index.fields().stream().map(Field::label).toList();
		return new JdbcIndex<>(name, filters, ordering, fields);
	}

	public String createIndexCode(String table) {
		return "CREATE INDEX \"%s:indexes:%s\" ON \"%s\" (%s)".formatted(
			table, name, table,
			List.of(
				filters.stream().map(JdbcFilter::sql),
				ordering.stream().map(o -> "\"%s\" %s".formatted(o.field().label(), switch (o.order()) {
				case ASCENDING -> "ASC";
				case DESCENDING -> "DESC";
				})),
				fields.stream().map(f -> "\"%s\"".formatted(f)))
				.stream().flatMap(s -> s).collect(Collectors.joining(", ")));
	}
}
