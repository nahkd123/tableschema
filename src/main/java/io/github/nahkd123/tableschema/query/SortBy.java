package io.github.nahkd123.tableschema.query;

import io.github.nahkd123.tableschema.SortOrder;
import io.github.nahkd123.tableschema.schema.Field;

public record SortBy<R>(Field<R, ?> field, SortOrder order) {
}
