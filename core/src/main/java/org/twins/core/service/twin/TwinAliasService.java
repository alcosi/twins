package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twin.TwinAliasRepository;
import org.twins.core.dao.twin.TwinAliasType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.twins.core.dao.twin.TwinAliasType.*;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinAliasService {
    final TwinAliasRepository twinAliasRepository;
    final AuthService authService;
    final EntitySmartService entitySmartService;

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

    public List<TwinAliasEntity> createAliases(TwinEntity twin) throws ServiceException {
        List<TwinAliasEntity> aliases = new ArrayList<>();
        switch (twin.getTwinClass().getOwnerType()) {
            case DOMAIN:
                addAliasIfNotNull(aliases, createAlias(twin, _D));
                addAliasIfNotNull(aliases, createAlias(twin, _C));
                addAliasIfNotNull(aliases, createAlias(twin, _S));
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                addAliasIfNotNull(aliases, createAlias(twin, _D));
                addAliasIfNotNull(aliases, createAlias(twin, _B));
                addAliasIfNotNull(aliases, createAlias(twin, _K));
                break;
            case DOMAIN_USER:
                addAliasIfNotNull(aliases, createAlias(twin, _D));
                addAliasIfNotNull(aliases, createAlias(twin, _T));
                break;
            default:
                log.warn("Unsupported owner type for alias creation: {}", twin.getTwinClass().getOwnerType());
        }
        return aliases;
    }

    private void addAliasIfNotNull(List<TwinAliasEntity> aliases, TwinAliasEntity alias) {
        if (alias != null) aliases.add(alias);
    }

    private TwinAliasEntity createAlias(TwinEntity twin, String aliasType) throws ServiceException {
        switch (aliasType) {
            case _D:
                twinAliasRepository.createDomainAlias(twin.getId(), aliasType);
                break;
            case _C:
                twinAliasRepository.createDomainClassAlias(twin.getId(), aliasType);
                break;
            case _B:
                twinAliasRepository.createBusinessAccountClassAlias(twin.getId(), aliasType);
                break;
            case _S:
                twinAliasRepository.createSpaceDomainAlias(twin.getId(), aliasType);
            case _K:
            case _T:
                twinAliasRepository.createSpaceBusinessAccountAlias(twin.getId(), aliasType);
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.UNSUPPORTED_ALIAS_TYPE, "Unsupported alias type: " + aliasType);
        }
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType.valueOf(aliasType));
    }

    public void forceDeleteAliasCounters(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();
        List<UUID> aliasToDelete = twinAliasRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(aliasToDelete, twinAliasRepository);
    }
}

