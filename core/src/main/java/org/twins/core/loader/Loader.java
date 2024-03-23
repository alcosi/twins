package org.twins.core.loader;

import java.util.Collection;

public abstract class Loader<E> {
    public abstract void load(E entity);

    public abstract void load(Collection<E> entityList);
}
