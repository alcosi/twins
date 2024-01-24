package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.HistoryTypeDomainTemplateRepository;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryService extends EntitySecureFindServiceImpl<HistoryEntity> {
    @Lazy
    final TwinService twinService;
    final HistoryRepository historyRepository;
    final HistoryTypeDomainTemplateRepository historyTypeDomainTemplateRepository;
    final AuthService authService;
    final I18nService i18nService;

    @Override
    public CrudRepository<HistoryEntity, UUID> entityRepository() {
        return historyRepository;
    }

    @Override
    public boolean isEntityReadDenied(HistoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<HistoryEntity> findHistory(UUID twinId, int childDepth) throws ServiceException {
        UserEntity user = authService.getApiUser().getUser();
        List<HistoryEntity> list = null;
        if (childDepth == 0)
            list = historyRepository.findByTwinId(twinId);
        else //todo support different depth
            list = historyRepository.findByTwinIdIncludeFirstLevelChildren(twinId);
        list.forEach(h -> h //todo delete
                .setActorUser(user)
                .setActorUserId(user.getId()));
        return list;
    }

    public void saveHistory(TwinEntity twinEntity, HistoryType type, HistoryContext context) throws ServiceException {
        HistoryEntity historyEntity = createEntity(twinEntity, type, context, getActor());
        entitySmartService.save(historyEntity, historyRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public HistoryEntity createEntity(TwinEntity twinEntity, HistoryType type, HistoryContext context, UserEntity actor) throws ServiceException {
        HistoryEntity historyEntity = new HistoryEntity()
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setActorUser(actor)
                .setActorUserId(actor.getId())
                .setHistoryType(type)
                .setContext(context);
        fillSnapshotMessage(historyEntity);
        return historyEntity;
    }

    private UserEntity getActor() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UserEntity actor;
        if (apiUser != null && apiUser.isUserSpecified())
            actor = apiUser.getUser();
        else
            actor = null; //todo we can have changes not from users but from some system schedulers
        return actor;
    }

    public void saveHistory(TwinEntity twinEntity, HistoryCollector historyCollector) throws ServiceException {
        if (historyCollector == null || CollectionUtils.isEmpty(historyCollector.getHistoryList()))
            return;
        List<HistoryEntity> historyEntityList = new ArrayList<>();
        UserEntity actor = getActor();
        for (Pair<HistoryType, HistoryContext> pair : historyCollector.getHistoryList()) {
            HistoryEntity historyEntity = createEntity(twinEntity, pair.getKey(), pair.getValue(), actor);
            historyEntityList.add(historyEntity);
        }
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
    }

    public void saveHistory(MultiTwinHistoryCollector multiTwinHistoryCollector) throws ServiceException {
        if (MapUtils.isEmpty(multiTwinHistoryCollector.getMultiTwinHistory()))
            return;
        List<HistoryEntity> historyEntityList = new ArrayList<>();
        UserEntity actor = getActor();
        for (Pair<TwinEntity, HistoryCollector> twinHistory : multiTwinHistoryCollector.getMultiTwinHistory().values()) {
            if (twinHistory.getValue() == null || CollectionUtils.isEmpty(twinHistory.getValue().getHistoryList()))
                continue;
            for (Pair<HistoryType, HistoryContext> pair : twinHistory.getValue().getHistoryList()) {
                HistoryEntity historyEntity = createEntity(twinHistory.getKey(), pair.getKey(), pair.getValue(), actor);
                historyEntityList.add(historyEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
    }

    public void fillSnapshotMessage(HistoryEntity historyEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        String snapshotTemplate = getSnapshotMessageTemplate(historyEntity.getHistoryType(), apiUser.isDomainSpecified() ? apiUser.getDomain().getId() : null);
        if (StringUtils.isEmpty(snapshotTemplate)) {
            historyEntity.setSnapshotMessage("History message template is not configured");
            return;
        }
        Map<String, String> templateVars = prepareCommonTemplateVars(snapshotTemplate, historyEntity);
        if (historyEntity.getContext() != null)
            templateVars.putAll(historyEntity.getContext().getTemplateVars());
        historyEntity.setSnapshotMessage(org.cambium.common.util.StringUtils.replaceVariables(snapshotTemplate, templateVars));
    }

    public Map<String, String> prepareCommonTemplateVars(String template, HistoryEntity historyEntity) {
        Map<String, String> templateVars = new HashMap<>();
        if (historyEntity.getTwin().getTwinClass() != null) {
            templateVars.put("twin.class.key", historyEntity.getTwin().getTwinClass().getKey());
            if (template.indexOf("twin.class.name") > 0)
                templateVars.put("twin.class.name", i18nService.translateToLocale(historyEntity.getTwin().getTwinClass().getNameI18NId()));
        }
        return templateVars;
    }

    //todo cache it
    private String getSnapshotMessageTemplate(HistoryType historyType, UUID domainId) {
        String snapshotTemplate = null;
        if (domainId != null)
            snapshotTemplate = historyTypeDomainTemplateRepository.findSnapshotMessageTemplate(historyType, domainId);
        if (snapshotTemplate == null)
            snapshotTemplate = historyTypeDomainTemplateRepository.findSnapshotMessageTemplate(historyType.getId());
        return snapshotTemplate;
    }

    public String getChangeFreshestDescription(HistoryEntity historyEntity) {
        return historyEntity.getSnapshotMessage(); //todo
    }


}
