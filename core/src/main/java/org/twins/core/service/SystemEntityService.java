package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class SystemEntityService {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinStatusRepository twinStatusRepository;
    final UserRepository userRepository;
    final TwinClassExtendsMapRepository twinClassExtendsMapRepository;
    final TwinClassChildMapRepository twinClassChildMapRepository;
    final EntitySmartService entitySmartService;

    private static final UUID USER_SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final UUID TWIN_CLASS_USER = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private static final UUID TWIN_CLASS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0001-000000000003");

    private static final UUID TWIN_STATUS_USER = UUID.fromString("00000000-0000-0000-0003-000000000001");
    private static final UUID TWIN_STATUS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0003-000000000003");

    private static final UUID TWIN_TEMPLATE_USER = UUID.fromString("00000000-0000-0000-0002-000000000001");
    private static final UUID TWIN_TEMPLATE_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0002-000000000003");

    @PostConstruct
    public void postConstruct() throws ServiceException {
        UserEntity systemUser = new UserEntity()
                .setId(USER_SYSTEM)
                .setName("SYSTEM")
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(USER_SYSTEM, systemUser, userRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinClassEntity twinClassEntity;
        twinClassEntity = new TwinClassEntity()
                .setId(TWIN_CLASS_USER)
                .setKey("USER")
                .setOwnerType(TwinClassEntity.OwnerType.SYSTEM)
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinClassEntity = new TwinClassEntity()
                .setId(TWIN_CLASS_BUSINESS_ACCOUNT)
                .setKey("BUSINESS_ACCOUNT")
                .setOwnerType(TwinClassEntity.OwnerType.SYSTEM)
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinStatusEntity twinStatusEntity;
        twinStatusEntity = new TwinStatusEntity()
                .setId(TWIN_STATUS_USER)
                .setTwinClassId(TWIN_CLASS_USER);
        entitySmartService.save(twinStatusEntity.getId(), twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinStatusEntity = new TwinStatusEntity()
                .setId(TWIN_STATUS_BUSINESS_ACCOUNT)
                .setTwinClassId(TWIN_CLASS_BUSINESS_ACCOUNT);
        entitySmartService.save(twinStatusEntity.getId(), twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinEntity twinEntity;
        twinEntity = new TwinEntity()
                .setId(TWIN_TEMPLATE_USER)
                .setName("User")
                .setTwinClassId(TWIN_CLASS_USER)
                .setTwinStatusId(TWIN_STATUS_USER)
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinEntity = new TwinEntity()
                .setId(TWIN_TEMPLATE_BUSINESS_ACCOUNT)
                .setName("Business account")
                .setTwinClassId(TWIN_CLASS_BUSINESS_ACCOUNT)
                .setTwinStatusId(TWIN_STATUS_BUSINESS_ACCOUNT)
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        loadTwinClassInheritanceMaps();
    }

    private void loadTwinClassInheritanceMaps() {
        Iterable<TwinClassNoRelationsProjection> allClasses = twinClassRepository.findAllProjectedBy(TwinClassNoRelationsProjection.class);
        List<TwinClassChildMapEntity> twinClassChildMapEntityList = new ArrayList<>();
        List<TwinClassExtendsMapEntity> twinClassExtendsMapEntityList = new ArrayList<>();
        for (TwinClassNoRelationsProjection twinClassEntity : allClasses) {
            for (UUID twinClassExtendsId : loadExtendedClasses(twinClassEntity)) {
                twinClassExtendsMapEntityList.add(new TwinClassExtendsMapEntity()
                        .setTwinClassId(twinClassEntity.id())
                        .setExtendsTwinClassId(twinClassExtendsId));
            }
            for (UUID twinClassChildId : loadChildClasses(twinClassEntity.id())) {
                twinClassChildMapEntityList.add(new TwinClassChildMapEntity()
                        .setTwinClassId(twinClassEntity.id())
                        .setChildTwinClassId(twinClassChildId));
            }
        }
        twinClassExtendsMapRepository.truncateTable();
        twinClassExtendsMapRepository.saveAll(twinClassExtendsMapEntityList);
        twinClassChildMapRepository.truncateTable();
        twinClassChildMapRepository.saveAll(twinClassChildMapEntityList);
        //todo db trigger must be added on twin_class table
    }

    public Set<UUID> loadExtendedClasses(TwinClassNoRelationsProjection twinClassEntity) {
        Set<UUID> extendedClassIdSet = new LinkedHashSet<>();
        extendedClassIdSet.add(twinClassEntity.id());
        if (twinClassEntity.extendsTwinClassId() == null)
            return extendedClassIdSet;
        UUID extendedTwinClassId = twinClassEntity.extendsTwinClassId();
        extendedClassIdSet.add(extendedTwinClassId);
        for (int i = 0; i <= 10; i++) {
            extendedTwinClassId = twinClassRepository.findExtendedClassId(extendedTwinClassId);
            if (extendedTwinClassId == null)
                break;
            if (extendedClassIdSet.contains(extendedTwinClassId)) {
                log.warn(twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " inheritance recursion");
                break;
            }
            extendedClassIdSet.add(extendedTwinClassId);
        }
        return extendedClassIdSet;
    }

    public Set<UUID> loadChildClasses(UUID twinClassId) {
        Set<UUID> childClassIdSet = new LinkedHashSet<>();
        childClassIdSet.add(twinClassId);
        loadChildClasses(childClassIdSet, twinClassId, 10);
        return childClassIdSet;
    }

    private void loadChildClasses(Set<UUID> childClassIdSet, UUID twinClassId, int recursionDepth) {
        if (recursionDepth <= 0) {
            log.warn("Load child classes recursion depth limit reached");
            return;
        }
        List<UUID> childTwinClassIdList = twinClassRepository.findChildClassIdList(twinClassId);
        if (CollectionUtils.isNotEmpty(childTwinClassIdList)) {
            for (UUID childTwinClassId : childTwinClassIdList) {
                childClassIdSet.add(childTwinClassId);
                loadChildClasses(childClassIdSet, childTwinClassId, recursionDepth - 1);
            }
        }
    }

    public UUID getUserIdSystem() {
        return USER_SYSTEM;
    }

    public boolean isTwinClassForUser(UUID twinClassId) {
        return TWIN_CLASS_USER.equals(twinClassId);
    }

    public boolean isTwinClassForBusinessAccount(UUID twinClassId) {
        return TWIN_CLASS_BUSINESS_ACCOUNT.equals(twinClassId);
    }

    public UUID getTwinIdTemplateForUser() {
        return TWIN_TEMPLATE_USER;
    }

    public UUID getTwinIdTemplateForBusinessAccount() {
        return TWIN_TEMPLATE_BUSINESS_ACCOUNT;
    }

    public TwinEntity createTwinTemplateDomainBusinessAccount(UUID domainId) throws ServiceException {
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainId)
                .setKey("DOMAIN_BUSINESS_ACCOUNT")
                .setOwnerType(TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT)
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId());
        twinStatusEntity = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        TwinEntity twinEntity = new TwinEntity()
                .setName("Domain business account")
                .setTwinClassId(twinClassEntity.getId())
                .setTwinStatusId(twinStatusEntity.getId())
                .setCreatedByUserId(USER_SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        twinEntity = entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinEntity;
    }
}
