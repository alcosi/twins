package org.twins.core.featurer.templator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3701,
        name = "Simple",
        description = "")
public class TemplatorSimple extends Templator {
    @Override
    protected String generate(Properties properties, String templateOrTemplateId, Map<String, String> templateVar) throws ServiceException {
        return StringUtils.replaceVariables(templateOrTemplateId, templateVar);
    }
}
