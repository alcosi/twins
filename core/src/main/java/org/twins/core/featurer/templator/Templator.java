package org.twins.core.featurer.templator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.TYPE_37,
        name = "Templator",
        description = "Send email")
@Slf4j
public abstract class Templator extends FeaturerTwins {
    @Autowired
    private I18nService i18nService;
    public String generate(HashMap<String, String> templaterParams, UUID templateI18nId, Map<String, String> templateVars) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, templaterParams);
        return generate(properties, templateI18nId, templateVars);
    }

    protected String generate(Properties properties, UUID templateI18nId, Map<String, String> templateVar) throws ServiceException {
        String templateOrTemplateId = i18nService.translateToLocale(templateI18nId);
        return generate(properties, templateOrTemplateId, templateVar);
    }

    protected abstract String generate(Properties properties, String templateOrTemplateId, Map<String, String> templateVar) throws ServiceException;
}
