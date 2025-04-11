package io.github.nahkd123.tableschema.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A typed result mapped from query result set. Query result must be closed to
 * free the resource/handle (which is usually a cursor in database).
 * </p>
 * 
 * @param <T> Type of mapped object.
 * @see #nextRow()
 * @see #asList()
 * @see #first()
 */
public interface QueryResult<T> extends Iterable<T>, AutoCloseable {
	/**
	 * <p>
	 * Get next row (if any). Returns {@code null} if there are no more rows.
	 * </p>
	 * 
	 * @return Next row or {@code null}.
	 */
	T nextRow();

	/**
	 * <p>
	 * Collect leftover rows into a list. This is <em>terminal</em> operation, which
	 * automatically close this query result.
	 * </p>
	 * 
	 * @return A list of leftover rows.
	 */
	default List<T> asList() {
		List<T> list = new ArrayList<>();
		T row;
		do {
			row = nextRow();
			if (row != null) list.add(row);
		} while (row != null);
		close();
		return Collections.unmodifiableList(list);
	}

	/**
	 * <p>
	 * Get the first row starting from this result's pointer. This is
	 * <em>terminal</em> operation, and it is the combination of {@link #nextRow()}
	 * and {@link #close()}.
	 * </p>
	 * 
	 * @return Row at pointer or {@code null}.
	 */
	default T first() {
		T result = nextRow();
		close();
		return result;
	}

	@Override
	void close();

	@Override
	default Iterator<T> iterator() {
		return new QueryResultIterator<>(this);
	}

	class QueryResultIterator<T> implements Iterator<T> {
		private QueryResult<T> result;
		private T holdingRow = null;
		private boolean ended = false;

		public QueryResultIterator(QueryResult<T> result) {
			this.result = result;
		}

		@Override
		public boolean hasNext() {
			if (ended) return false;
			if (holdingRow != null) return true;

			if (holdingRow == null) {
				holdingRow = result.nextRow();

				if (holdingRow == null) {
					ended = true;
					result.close();
				}
			}

			return !ended;
		}

		@Override
		public T next() {
			if (!hasNext()) throw new IndexOutOfBoundsException("No more elements");
			T row = holdingRow;
			holdingRow = null;
			return row;
		}
	}
}
