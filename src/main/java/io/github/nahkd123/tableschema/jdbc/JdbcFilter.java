package io.github.nahkd123.tableschema.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import io.github.nahkd123.tableschema.jdbc.schema.type.JdbcFieldType;
import io.github.nahkd123.tableschema.query.Filter;
import io.github.nahkd123.tableschema.schema.Field;

public record JdbcFilter(String sql, JdbcFieldType<?> valueType, Object value, List<JdbcFilter> children) {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static JdbcFilter map(String table, Filter<?> filter, boolean parametric) {
		return switch (filter) {
		case Filter.Compare(Field<?, ?> field, int op, Object value): {
			JdbcFieldType<?> type = JdbcFieldType.map(field.type());
			String cmpOp = switch (op) {
			case Filter.Compare.EQUALS -> "==";
			case Filter.Compare.LESS -> "<";
			case Filter.Compare.LESS_EQUALS -> "<=";
			case Filter.Compare.GREATER -> ">";
			case Filter.Compare.GREATER_EQUALS -> ">=";
			default -> throw new IllegalArgumentException("Unexpected value: " + op);
			};
			String cmpAgainst = parametric ? "?" : ((JdbcFieldType) type).valueToCode(value);
			String sql = table != null
				? "[%s].[%s] %s %s".formatted(table, field.label(), cmpOp, cmpAgainst)
				: "[%s] %s %s".formatted(field.label(), cmpOp, cmpAgainst);
			yield new JdbcFilter(sql, parametric ? type : null, parametric ? value : null, null);
		}
		case Filter.Not(Filter<?> child): {
			JdbcFilter childFilter = map(table, child, parametric);
			yield new JdbcFilter("NOT (%s)".formatted(childFilter.sql), null, null, List.of(childFilter));
		}
		case Filter.And(List<?> children): {
			List<JdbcFilter> cs = children.stream().map(child -> map(table, (Filter<?>) child, parametric)).toList();
			String sql = cs.stream().map(c -> c.sql).collect(Collectors.joining(" AND "));
			yield new JdbcFilter("(%s)".formatted(sql), null, null, cs);
		}
		case Filter.Or(List<?> children): {
			List<JdbcFilter> cs = children.stream().map(child -> map(table, (Filter<?>) child, parametric)).toList();
			String sql = cs.stream().map(c -> c.sql).collect(Collectors.joining(" OR "));
			yield new JdbcFilter("(%s)".formatted(sql), null, null, cs);
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + filter);
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int fillParameters(int baseIndex, Filler filler) throws SQLException {
		if (valueType != null) {
			filler.fill(baseIndex, (JdbcFieldType) valueType, value);
			baseIndex++;
		}

		if (children != null)
			for (JdbcFilter child : children) baseIndex = child.fillParameters(baseIndex, filler);
		return baseIndex;
	}

	public void fillParameters(Filler filler) throws SQLException {
		fillParameters(0, filler);
	}

	public static interface Filler {
		<T> void fill(int index, JdbcFieldType<T> valueType, T value) throws SQLException;
	}
}
