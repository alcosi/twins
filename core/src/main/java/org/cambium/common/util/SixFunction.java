package org.cambium.common.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface SixFunction<T, U, V, X, Y, Z, R> {
    default <W> SixFunction<T, U, V, X, Y, Z, W> andThen(Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (t, u, v,x,y,z) -> after.apply(this.apply(t, u, v,x,y,z));
    }

    R apply(T var1, U var2, V var3, X var4, Y var5, Z var6);
}
