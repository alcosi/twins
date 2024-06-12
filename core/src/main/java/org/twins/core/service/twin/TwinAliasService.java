package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.*;
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
    final TwinBusinessAccountAliasRepository twinBusinessAccountAliasRepository;
    final TwinDomainAliasRepository twinDomainAliasRepository;
    final TwinAliasRepository twinAliasRepository;
    final AuthService authService;
    final EntitySmartService entitySmartService;

//    public TwinAliasEntity findAlias(String twinAlias) throws ServiceException {
//        ApiUser apiUser = authService.getApiUser();
//
//        if (apiUser.getBusinessAccount() != null) {
//            TwinBusinessAccountAliasEntity twinBusinessAccountAliasEntity = twinBusinessAccountAliasRepository.findByBusinessAccountIdAndAlias(apiUser.getBusinessAccount().getId(), twinAlias);
//            if (twinBusinessAccountAliasEntity != null)
//                return twinBusinessAccountAliasEntity.getTwin();
//        }
//        TwinDomainAliasEntity twinDomainAliasEntity = twinDomainAliasRepository.findByDomainIdAndAlias(apiUser.getDomain().getId(), twinAlias);
//        if (twinDomainAliasEntity == null)
//            throw new ServiceException(ErrorCodeTwins.TWIN_ALIAS_UNKNOWN, "unknown twin alias[" + twinAlias + "]");
//        return twinDomainAliasEntity.getTwin();
//    }

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

//    public List<TwinAliasEntity> createTwinBusinessAccountAliases(TwinEntity twinEntity) {
//        if (twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT && twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT_USER)
//            return new ArrayList<>(); // businessAccountAliases can not be created for this twin
//        twinBusinessAccountAliasRepository.createAliasByClass(twinEntity.getId());
//        TwinEntity spaceTwin = loadSpaceForTwin(twinEntity);
//        if (spaceTwin != null) {
//            twinBusinessAccountAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
//        }
//        return twinBusinessAccountAliasRepository.findAllByTwinId(twinEntity.getId());
//    }
//
//    public List<TwinAliasEntity> createTwinDomainAliases(TwinEntity twinEntity) {
//        twinDomainAliasRepository.createAliasByClass(twinEntity.getId());
//        TwinEntity spaceTwin = loadSpaceForTwin(twinEntity);
//        if (spaceTwin != null) {
//            twinDomainAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
//        }
//        return twinDomainAliasRepository.findAllByTwinId(twinEntity.getId());
//    }

    public void regenerateExistAliases() throws ServiceException {
    }

    public List<TwinAliasEntity> createAliases(TwinEntity twin) throws ServiceException {
        List<TwinAliasEntity> aliases = new ArrayList<>();
        switch (twin.getTwinClass().getOwnerType()) {
            case DOMAIN:
                aliases.add(createAliasD(twin));
                aliases.add(createAliasC(twin));
                aliases.add(createAliasS(twin, TwinAliasType.S));
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                aliases.add(createAliasD(twin));
                aliases.add(createAliasB(twin));
                aliases.add(createAliasS(twin, TwinAliasType.K));
                break;
            case DOMAIN_USER:
                aliases.add(createAliasD(twin));
                aliases.add(createAliasS(twin, TwinAliasType.T));
                break;
            default:
                log.warn("Unsupported owner type for alias creation: {}", twin.getTwinClass().getOwnerType());
        }
        return aliases;
    }

    private TwinAliasEntity createAliasD(TwinEntity twin) {
        twinAliasRepository.createAliasByClass(twin.getId(), TwinAliasType._D);
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType._D);
    }

    private TwinAliasEntity createAliasC(TwinEntity twin) {
        twinAliasRepository.createAliasByClass(twin.getId(), TwinAliasType._C);
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType._C);
    }

    private TwinAliasEntity createAliasB(TwinEntity twin) {
        twinAliasRepository.createAliasByClass(twin.getId(), TwinAliasType._B);
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType._B);
    }

    private TwinAliasEntity createAliasS(TwinEntity twin, TwinAliasType aliasType) {
        twinAliasRepository.createAliasByClass(twin.getId(), aliasType.name());
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), aliasType.name());
    }

    private TwinAliasEntity createAliasK(TwinEntity twin) {
        twinAliasRepository.createAliasByClass(twin.getId(), TwinAliasType._K);
        return twinAliasRepository.findByTwinIdAndType(twin.getId(), TwinAliasType._K);
    }

    public void forceDeleteAliasCounters(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();
        List<UUID> aliasToDelete = twinBusinessAccountAliasRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(aliasToDelete, twinBusinessAccountAliasRepository);
    }
}
