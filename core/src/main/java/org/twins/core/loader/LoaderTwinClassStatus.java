package org.twins.core.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoaderTwinClassStatus<E> extends Loader<TwinClassEntity> {
    final
    @Override
    public void load(TwinClassEntity entity) {

    }

    @Override
    public void load(Collection<TwinClassEntity> entityList) {

    }
}
