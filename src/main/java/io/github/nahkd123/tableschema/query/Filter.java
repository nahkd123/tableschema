package io.github.nahkd123.tableschema.query;

import java.util.List;

import io.github.nahkd123.tableschema.schema.Field;

/**
 * <p>
 * Filtering out entries based on certain criteria. Includes basic boolean
 * operations ({@code AND}, {@code OR} and {@code NOT}) and value comparison
 * (==, &gt;, &ge;, &lt; and &le;). Only {@code ==} can be used with
 * non-numerical types.
 * </p>
 * 
 * @param <R> Type of table row.
 * @see #allOf(Filter...)
 * @see #anyOf(Filter...)
 * @see #not(Filter)
 * @see #and(Filter)
 * @see #or(Filter)
 * @see #eq(Field, Object)
 * @see #lt(Field, Object)
 * @see #lq(Field, Object)
 * @see #gt(Field, Object)
 * @see #gq(Field, Object)
 */
public interface Filter<R> {
	/**
	 * <p>
	 * Apply AND operator on all children.
	 * </p>
	 * 
	 * @param <R>      Type of table row.
	 * @param children A varargs list of filters.
	 * @return A new filter with AND operator on all provided filters.
	 */
	@SafeVarargs
	static <R> Filter<R> allOf(Filter<R>... children) {
		return new And<R>(List.of(children));
	}

	/**
	 * <p>
	 * Apply OR operator on all children.
	 * </p>
	 * 
	 * @param <R>      Type of table row.
	 * @param children A varargs list of filters.
	 * @return A new filter with OR operator on all provided filters.
	 */
	@SafeVarargs
	static <R> Filter<R> anyOf(Filter<R>... children) {
		return new Or<R>(List.of(children));
	}

	/**
	 * <p>
	 * Negate the effect of filter.
	 * </p>
	 * 
	 * @param <R> Type of table row.
	 * @param of  The filter to negate.
	 * @return A new filter that have negated effect of provided filter.
	 */
	static <R> Filter<R> not(Filter<R> of) {
		return of instanceof Not(Filter<R> child) ? child : new Not<R>(of);
	}

	/**
	 * <p>
	 * Check if value of field is equals to value. This works for all known types.
	 * </p>
	 * 
	 * @param <R>   Type of table row.
	 * @param <T>   Type of value.
	 * @param field The field in table.
	 * @param value The value to compare against.
	 * @return A new comparison filter.
	 */
	static <R, T> Filter<R> eq(Field<R, T> field, T value) {
		return new Compare<R, T>(field, Compare.EQUALS, value);
	}

	/**
	 * <p>
	 * Check if value of field is smaller than provided value.
	 * </p>
	 * 
	 * @param <R>   Type of table row.
	 * @param <T>   Type of value.
	 * @param field The field in table.
	 * @param value The value to compare against.
	 * @return A new comparison filter.
	 */
	static <R, T> Filter<R> lt(Field<R, T> field, T value) {
		return new Compare<R, T>(field, Compare.LESS, value);
	}

	/**
	 * <p>
	 * Check if value of field is larger than provided value.
	 * </p>
	 * 
	 * @param <R>   Type of table row.
	 * @param <T>   Type of value.
	 * @param field The field in table.
	 * @param value The value to compare against.
	 * @return A new comparison filter.
	 */
	static <R, T> Filter<R> gt(Field<R, T> field, T value) {
		return new Compare<R, T>(field, Compare.GREATER, value);
	}

	/**
	 * <p>
	 * Check if value of field is smaller than or equals to provided value.
	 * </p>
	 * 
	 * @param <R>   Type of table row.
	 * @param <T>   Type of value.
	 * @param field The field in table.
	 * @param value The value to compare against.
	 * @return A new comparison filter.
	 */
	static <R, T> Filter<R> lq(Field<R, T> field, T value) {
		return new Compare<R, T>(field, Compare.LESS_EQUALS, value);
	}

	/**
	 * <p>
	 * Check if value of field is larger than or equals to provided value.
	 * </p>
	 * 
	 * @param <R>   Type of table row.
	 * @param <T>   Type of value.
	 * @param field The field in table.
	 * @param value The value to compare against.
	 * @return A new comparison filter.
	 */
	static <R, T> Filter<R> gq(Field<R, T> field, T value) {
		return new Compare<R, T>(field, Compare.GREATER_EQUALS, value);
	}

	/**
	 * <p>
	 * Concatenate this filter and another filter with AND operator.
	 * </p>
	 * 
	 * @param another Another filter.
	 * @return A new filter.
	 */
	default Filter<R> and(Filter<R> another) {
		return new And<R>(List.of(this, another));
	}

	/**
	 * <p>
	 * Concatenate this filter and another filter with OR operator.
	 * </p>
	 * 
	 * @param another Another filter.
	 * @return A new filter.
	 */
	default Filter<R> or(Filter<R> another) {
		return new Or<R>(List.of(this, another));
	}

	record And<R>(List<Filter<R>> children) implements Filter<R> {
		@SuppressWarnings("unchecked")
		@Override
		public Filter<R> and(Filter<R> another) {
			Filter<R>[] out = new Filter[children.size() + 1];
			for (int i = 0; i < children.size(); i++) out[i] = children.get(i);
			out[children.size()] = another;
			return new And<R>(List.of(out));
		}
	}

	record Or<R>(List<Filter<R>> children) implements Filter<R> {
		@SuppressWarnings("unchecked")
		@Override
		public Filter<R> or(Filter<R> another) {
			Filter<R>[] out = new Filter[children.size() + 1];
			for (int i = 0; i < children.size(); i++) out[i] = children.get(i);
			out[children.size()] = another;
			return new Or<R>(List.of(out));
		}
	}

	record Not<R>(Filter<R> child) implements Filter<R> {
	}

	record Compare<R, T>(Field<R, T> field, int flags, T value) implements Filter<R> {
		public static final int EQUALS = 1;
		public static final int GREATER = 2;
		public static final int LESS = 4;
		public static final int GREATER_EQUALS = GREATER | EQUALS;
		public static final int LESS_EQUALS = LESS | EQUALS;
	}
}
