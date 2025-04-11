package io.github.nahkd123.tableschema.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nahkd123.tableschema.query.Filter;
import io.github.nahkd123.tableschema.query.SortBy;

/**
 * <p>
 * Indexes are used to speedup query speed. Each index consists one or more of
 * filter, ordering and field. Empty indexes are not allowed. Primary key fields
 * are always indexed.
 * </p>
 * 
 * @param <R> Type of value in table/table row.
 * @see Index#Index(String)
 * @see #appendField(Field)
 * @see #appendFilter(Filter)
 * @see #appendOrdering(SortBy)
 */
public record Index<R>(String name, List<Filter<R>> filters, List<SortBy<R>> ordering, List<Field<R, ?>> fields) {
	public Index(String name) {
		this(name, List.of(), List.of(), List.of());
	}

	public Index<R> withFilters(List<Filter<R>> filters) {
		return new Index<>(name, filters, ordering, fields);
	}

	/**
	 * <p>
	 * Append filter to index. Value in filter must be <em>constant</em> - it must
	 * not be supplied by user or "during runtime".
	 * </p>
	 * 
	 * @param filter The filter.
	 * @return The new index with appended filter.
	 */
	public Index<R> appendFilter(Filter<R> filter) {
		List<Filter<R>> filters = new ArrayList<>(this.filters.size() + 1);
		filters.addAll(this.filters);
		filters.add(filter);
		return withFilters(Collections.unmodifiableList(filters));
	}

	public Index<R> withOrdering(List<SortBy<R>> ordering) {
		return new Index<>(name, filters, ordering, fields);
	}

	/**
	 * <p>
	 * Append sorting info to index for faster sorting of data.
	 * </p>
	 * 
	 * @param o The sort info.
	 * @return The new index with appended sorting info.
	 */
	public Index<R> appendOrdering(SortBy<R> o) {
		List<SortBy<R>> ordering = new ArrayList<>(this.ordering.size() + 1);
		ordering.addAll(this.ordering);
		ordering.add(o);
		return withOrdering(Collections.unmodifiableList(ordering));
	}

	public Index<R> withFields(List<Field<R, ?>> fields) {
		return new Index<>(name, filters, ordering, fields);
	}

	/**
	 * <p>
	 * Append field to index for faster querying.
	 * </p>
	 * 
	 * @param field The field.
	 * @return The new index with appended field.
	 */
	public Index<R> appendField(Field<R, ?> field) {
		List<Field<R, ?>> fields = new ArrayList<>(this.fields.size() + 1);
		fields.addAll(this.fields);
		fields.add(field);
		return withFields(Collections.unmodifiableList(fields));
	}
}
