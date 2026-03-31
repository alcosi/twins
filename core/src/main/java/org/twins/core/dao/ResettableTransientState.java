package org.twins.core.dao;

public interface ResettableTransientState<T> {
    T resetTransientState();
}
