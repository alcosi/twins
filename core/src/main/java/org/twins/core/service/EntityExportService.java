package org.twins.core.service;

import org.cambium.common.sql.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

public abstract class EntityExportService {
    @Autowired
    protected I18nExportService i18nExportService;

    @Autowired
    protected I18nService i18nService;

    @Autowired
    protected SqlBuilder sqlBuilder;
}
