package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
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
    final EntitySmartService entitySmartService;

    public static final UUID USER_SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID TWIN_CLASS_USER = UUID.fromString("00000000-0000-0000-0001-000000000001");
    public static final UUID TWIN_CLASS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0001-000000000003");
    public static final UUID TWIN_CLASS_GLOBAL_ANCESTOR = UUID.fromString("00000000-0000-0000-0001-000000000004");

    public static final UUID TWIN_CLASS_FIELD_USER_EMAIL = UUID.fromString("00000000-0000-0000-0011-000000000001");
    public static final UUID TWIN_CLASS_FIELD_USER_AVATAR = UUID.fromString("00000000-0000-0000-0011-000000000002");

    public static final UUID TWIN_CLASS_FIELD_TWIN_NAME =           UUID.fromString("00000000-0000-0000-0011-000000000003");
    public static final UUID TWIN_CLASS_FIELD_TWIN_DESCRIPTION =    UUID.fromString("00000000-0000-0000-0011-000000000004");
    public static final UUID TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID =    UUID.fromString("00000000-0000-0000-0011-000000000005");
    public static final UUID TWIN_CLASS_FIELD_TWIN_OWNER_USER =     UUID.fromString("00000000-0000-0000-0011-000000000006");
    public static final UUID TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER =  UUID.fromString("00000000-0000-0000-0011-000000000007");
    public static final UUID TWIN_CLASS_FIELD_TWIN_CREATOR_USER =   UUID.fromString("00000000-0000-0000-0011-000000000008");
    public static final UUID TWIN_CLASS_FIELD_TWIN_HEAD =           UUID.fromString("00000000-0000-0000-0011-000000000009");
    public static final UUID TWIN_CLASS_FIELD_TWIN_STATUS =         UUID.fromString("00000000-0000-0000-0011-000000000010");
    public static final UUID TWIN_CLASS_FIELD_TWIN_CREATED_AT =     UUID.fromString("00000000-0000-0000-0011-000000000011");
    public static final Set<UUID> TWIN_CLASS_FIELDS_SYSTEM_SET = Set.of(
            TWIN_CLASS_FIELD_USER_EMAIL,
            TWIN_CLASS_FIELD_USER_AVATAR,
            TWIN_CLASS_FIELD_TWIN_NAME,
            TWIN_CLASS_FIELD_TWIN_DESCRIPTION,
            TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID,
            TWIN_CLASS_FIELD_TWIN_OWNER_USER,
            TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER,
            TWIN_CLASS_FIELD_TWIN_CREATOR_USER,
            TWIN_CLASS_FIELD_TWIN_HEAD,
            TWIN_CLASS_FIELD_TWIN_STATUS,
            TWIN_CLASS_FIELD_TWIN_CREATED_AT
    );

    public static final UUID TWIN_STATUS_USER = UUID.fromString("00000000-0000-0000-0003-000000000001");
    public static final UUID TWIN_STATUS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0003-000000000003");
    public static final UUID TWIN_STATUS_GLOBAL_ANCESTOR = UUID.fromString("00000000-0000-0000-0003-000000000004");

    public static final UUID TWIN_TEMPLATE_USER = UUID.fromString("00000000-0000-0000-0002-000000000001");
    public static final UUID TWIN_TEMPLATE_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0002-000000000003");

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
        twinClassEntity = new TwinClassEntity()
                .setId(TWIN_CLASS_GLOBAL_ANCESTOR)
                .setKey("GLOBAL_ANCESTOR")
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
        twinStatusEntity = new TwinStatusEntity()
                .setId(TWIN_STATUS_GLOBAL_ANCESTOR)
                .setTwinClassId(TWIN_CLASS_GLOBAL_ANCESTOR);
        entitySmartService.save(twinStatusEntity.getId(), twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinEntity twinEntity;
        twinEntity = new TwinEntity()
                .setId(TWIN_TEMPLATE_USER)
                .setName("User")
                .setTwinClassId(TWIN_CLASS_USER)
                .setTwinStatusId(TWIN_STATUS_USER)
                .setCreatedByUserId(USER_SYSTEM);
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinEntity = new TwinEntity()
                .setId(TWIN_TEMPLATE_BUSINESS_ACCOUNT)
                .setName("Business account")
                .setTwinClassId(TWIN_CLASS_BUSINESS_ACCOUNT)
                .setTwinStatusId(TWIN_STATUS_BUSINESS_ACCOUNT)
                .setCreatedByUserId(USER_SYSTEM);
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
    }

    public UUID getUserIdSystem() {
        return USER_SYSTEM;
    }

    public static boolean isTwinClassForUser(UUID twinClassId) {
        return TWIN_CLASS_USER.equals(twinClassId);
    }

    public static boolean isTwinClassForBusinessAccount(UUID twinClassId) {
        return TWIN_CLASS_BUSINESS_ACCOUNT.equals(twinClassId);
    }

    public static boolean isSystemClass(UUID twinClassId) {
        return isTwinClassForBusinessAccount(twinClassId) || isTwinClassForUser(twinClassId);
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
                .setCreatedByUserId(USER_SYSTEM);
        twinEntity = entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinEntity;
    }
}
