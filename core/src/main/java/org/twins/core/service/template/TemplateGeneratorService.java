package org.twins.core.service.template;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.template.generator.TemplateGeneratorEntity;
import org.twins.core.dao.template.generator.TemplateGeneratorRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.templator.Templator;
import org.twins.core.service.auth.AuthService;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class TemplateGeneratorService extends EntitySecureFindServiceImpl<TemplateGeneratorEntity> {
    private final TemplateGeneratorRepository templateGeneratorRepository;
    @Lazy
    private final AuthService authService;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<TemplateGeneratorEntity, UUID> entityRepository() {
        return templateGeneratorRepository;
    }

    @Override
    public Function<TemplateGeneratorEntity, UUID> entityGetIdFunction() {
        return TemplateGeneratorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TemplateGeneratorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(TemplateGeneratorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public String generate(TemplateGeneratorEntity templateGenerator, UUID templateI18nId, Map<String, String> templateVars) throws ServiceException {
        Templator templator = featurerService.getFeaturer(templateGenerator.getTemplaterFeaturerId(), Templator.class);
        return templator.generate(templateGenerator.getTemplaterParams(), templateI18nId, templateVars);
    }
}
