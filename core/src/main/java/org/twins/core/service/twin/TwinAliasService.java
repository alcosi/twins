package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twin.TwinAliasRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.twin.TwinAliasType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.twins.core.enums.twin.TwinAliasType.*;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinAliasService {
    private final TwinAliasRepository twinAliasRepository;
    private final AuthService authService;
    private final EntitySmartService entitySmartService;

    private final Lock counterLock = new ReentrantLock();

    public TwinAliasEntity findAlias(String twinAlias) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUserId();
        TwinAliasEntity twinAliasEntity = twinAliasRepository.findByAlias(twinAlias, domainId, businessAccountId, userId);
        if (twinAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_ALIAS_UNKNOWN, "unknown twin alias[" + twinAlias + "]");
        return twinAliasEntity;
    }

    public void loadAliases(TwinEntity twinEntity) {
        if (twinEntity.getTwinAliases() != null)
            return;
        List<TwinAliasEntity> aliases = twinAliasRepository.findAllByTwinIdAndArchivedFalse(twinEntity.getId());
        twinEntity.setTwinAliases(new Kit<>(aliases, TwinAliasEntity::getAliasTypeId));
    }

    public void loadAliases(Collection<TwinEntity> twinEntities) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntities)
            if (twinEntity.getTwinAliases() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinAliasEntity, UUID, UUID> aliasKit = new KitGrouped<>(
                twinAliasRepository.findAllByTwinIdInAndArchivedFalse(needLoad.keySet()), TwinAliasEntity::getId, TwinAliasEntity::getTwinId);
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet())
            entry.getValue().setTwinAliases(new Kit<>(aliasKit.getGrouped(entry.getKey()), TwinAliasEntity::getAliasTypeId));
    }


    @Async
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Map<UUID, List<TwinAliasEntity>>> createAliasesForTwins(List<TwinEntity> twins, boolean returnAlias) throws ServiceException {
        Map<UUID, List<TwinAliasEntity>> result = new HashMap<>();
        log.warn("start alias creation: " + twins.size());
        int counter = 0;
        for (TwinEntity twin : twins) {
            counter++;
            log.warn(counter + ") Creation aliases for twin with id: " + twin.getId());
            result.put(twin.getId(), createAliasesForTwin(twin, returnAlias));
        }
        log.warn("finish alias creation.");
        return CompletableFuture.completedFuture(returnAlias ? result : null);
    }

    public List<TwinAliasEntity> createAliasesForTwin(TwinEntity twin, boolean returnAlias) throws ServiceException {
        List<TwinAliasEntity> aliases = new ArrayList<>();
        switch (twin.getTwinClass().getOwnerType()) {
            case DOMAIN:
                addAliasIfNotNull(aliases, createAlias(twin, _D, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _C, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _S, returnAlias));
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                addAliasIfNotNull(aliases, createAlias(twin, _D, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _C, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _B, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _K, returnAlias));
                break;
            case DOMAIN_USER:
                addAliasIfNotNull(aliases, createAlias(twin, _D, returnAlias));
                addAliasIfNotNull(aliases, createAlias(twin, _T, returnAlias));
                break;
            default:
                log.warn("Unsupported owner type for alias creation: {}", twin.getTwinClass().getOwnerType());
        }
        return aliases;
    }

    private void addAliasIfNotNull(List<TwinAliasEntity> aliases, TwinAliasEntity alias) {
        if (alias != null) aliases.add(alias);
    }

    private TwinAliasEntity createAlias(TwinEntity twin, String aliasType, boolean returnAlias) throws ServiceException {
        switch (aliasType) {
            case _D:
                twinAliasRepository.createDomainAlias(twin.getId(), twin.getTwinClass().getDomainId());
                break;
            case _C:
                twinAliasRepository.createDomainClassAlias(twin.getId(), twin.getTwinClassId());
                break;
            case _B:
                twinAliasRepository.createBusinessAccountClassAlias(twin.getId(), twin.getOwnerBusinessAccountId(), twin.getTwinClassId(), twin.getTwinClass().getKey());
                break;
            case _S:
                twinAliasRepository.createSpaceDomainAlias(twin.getId());
            case _K:
            case _T:
                twinAliasRepository.createSpaceBusinessAccountAlias(twin.getId(), aliasType);
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.UNSUPPORTED_ALIAS_TYPE, "Unsupported alias type: " + aliasType);
        }
        return returnAlias ? twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType.valueOf(aliasType)) : null;
    }

    public void forceDeleteAliasCounters(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();
        List<UUID> aliasToDelete = twinAliasRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(aliasToDelete, twinAliasRepository);
    }
}

