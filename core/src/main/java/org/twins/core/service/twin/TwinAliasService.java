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
        UUID businessAccountId = apiUser.getBusinessAccount() != null ? apiUser.getBusinessAccount().getId() : null;
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
                aliases.add(createAlias(twin, TwinAliasType.D));
                aliases.add(createAlias(twin, TwinAliasType.C));
                aliases.add(createAlias(twin, TwinAliasType.S));
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                aliases.add(createAlias(twin, TwinAliasType.D));
                aliases.add(createAlias(twin, TwinAliasType.B));
                aliases.add(createAlias(twin, TwinAliasType.K));
                break;
            case DOMAIN_USER:
                aliases.add(createAlias(twin, TwinAliasType.D));
                aliases.add(createAlias(twin, TwinAliasType.T));
                break;
            default:
                log.warn("Unsupported owner type for alias creation: {}", twin.getTwinClass().getOwnerType());
        }
        return aliases;
    }

    private TwinAliasEntity createAlias(TwinEntity twin, TwinAliasType aliasType) throws ServiceException {
        switch (aliasType) {
            case D:
                twinAliasRepository.createDomainAlias(twin.getId(), aliasType);
                break;
            case C:
                twinAliasRepository.createDomainClassAlias(twin.getId(), aliasType);
                break;
            case B:
                twinAliasRepository.createBusinessAccountClassAlias(twin.getId(), aliasType);
                break;
            case S:
            case K:
            case T:
                twinAliasRepository.createSpaceAlias(twin.getId(), aliasType);
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.UNSUPPORTED_ALIAS_TYPE, "Unsupported alias type: " + aliasType);
        }
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), aliasType);
    }

    public void forceDeleteAliasCounters(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();
        List<UUID> aliasToDelete = twinAliasRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(aliasToDelete, twinAliasRepository);
    }
}

