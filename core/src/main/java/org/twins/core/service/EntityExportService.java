package org.twins.core.service;

import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public abstract class EntityExportService {
    @Autowired
    protected I18nExportService i18nExportService;

    @Autowired
    protected I18nService i18nService;

    @Autowired
    protected SqlBuilder sqlBuilder;

    @FunctionalInterface
    protected interface Exporter<E> {
        String apply(List<E> entities) throws ServiceException;
    }

    protected  <F, E> void exportChildren(
            boolean enabled,
            Collection<F> parents,
            Function<F, Collection<E>> childExtractor,
            Exporter<E> exporter,
            StringList sqlParts) throws ServiceException {

        if (!enabled) {
            return;
        }
        List<E> entities = new ArrayList<>();
        for (F parent : parents) {
            entities.addAll(childExtractor.apply(parent));
        }
        if (entities.isEmpty()) {
            return;
        }
        sqlParts.addNotBlank(exporter.apply(entities));
    }

    protected  <F, E> void exportChildrenKit(
            boolean enabled,
            Collection<F> parents,
            Function<F, Kit<E, UUID>> childExtractor,
            Exporter<E> exporter,
            StringList sqlParts) throws ServiceException {

        if (!enabled) {
            return;
        }
        List<E> entities = new ArrayList<>();
        for (F parent : parents) {
            entities.addAll(childExtractor.apply(parent));
        }
        if (entities.isEmpty()) {
            return;
        }
        sqlParts.addNotBlank(exporter.apply(entities));
    }
}
