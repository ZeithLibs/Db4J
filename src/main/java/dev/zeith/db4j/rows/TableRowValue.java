package dev.zeith.db4j.rows;

import org.jetbrains.annotations.*;

public record TableRowValue<T>(@NotNull TableRow<T> row, @Nullable T data)
{
}