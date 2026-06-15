package org.twins.core.service;

import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;
import java.util.function.Function;

public abstract class EntityExportService<E> {
    @Autowired
    protected I18nExportService i18nExportService;

    @Autowired
    protected I18nService i18nService;

    @Autowired
    protected SqlBuilder sqlBuilder;

    public abstract String exportCollectionToSql(Collection<E> entitiesCollection) throws ServiceException;

    public String exportToSql(E entity) throws ServiceException {
        return exportCollectionToSql(Collections.singletonList(entity));
    }

    @FunctionalInterface
    protected interface Exporter<E> {
        String apply(List<E> entities) throws ServiceException;
    }

    protected <F, CHE> void exportChildren(
            boolean enabled,
            Collection<F> parents,
            Function<F, Collection<CHE>> childExtractor,
            Exporter<CHE> exporter,
            StringList sqlParts) throws ServiceException {

        if (!enabled) {
            return;
        }
        List<CHE> entities = new ArrayList<>();
        for (F parent : parents) {
            entities.addAll(childExtractor.apply(parent));
        }
        if (entities.isEmpty()) {
            return;
        }
        sqlParts.addNotBlank(exporter.apply(entities));
    }

    protected <F, CHE> void exportChildrenKit(
            boolean enabled,
            Collection<F> parents,
            Function<F, Kit<CHE, UUID>> childExtractor,
            Exporter<CHE> exporter,
            StringList sqlParts) throws ServiceException {

        if (!enabled) {
            return;
        }
        List<CHE> entities = new ArrayList<>();
        for (F parent : parents) {
            entities.addAll(childExtractor.apply(parent).getCollection());
        }
        if (entities.isEmpty()) {
            return;
        }
        sqlParts.addNotBlank(exporter.apply(entities));
    }
}
