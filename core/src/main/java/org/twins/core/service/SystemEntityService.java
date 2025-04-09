package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.featurer.FeaturerTwins;

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
    final TwinClassFieldRepository twinClassFieldRepository;
    final EntitySmartService entitySmartService;

    public static final UUID USER_SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID TWIN_CLASS_USER = UUID.fromString("00000000-0000-0000-0001-000000000001");
    public static final UUID TWIN_CLASS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0001-000000000003");
    public static final UUID TWIN_CLASS_GLOBAL_ANCESTOR = UUID.fromString("00000000-0000-0000-0001-000000000004");

    public static final UUID TWIN_CLASS_FIELD_USER_EMAIL = UUID.fromString("00000000-0000-0000-0011-000000000001");
    public static final UUID TWIN_CLASS_FIELD_USER_AVATAR = UUID.fromString("00000000-0000-0000-0011-000000000002");
    public static final UUID TWIN_CLASS_FIELD_TWIN_NAME = UUID.fromString("00000000-0000-0000-0011-000000000003");
    public static final UUID TWIN_CLASS_FIELD_TWIN_DESCRIPTION = UUID.fromString("00000000-0000-0000-0011-000000000004");
    public static final UUID TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID = UUID.fromString("00000000-0000-0000-0011-000000000005");
    public static final UUID TWIN_CLASS_FIELD_TWIN_OWNER_USER = UUID.fromString("00000000-0000-0000-0011-000000000006");
    public static final UUID TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER = UUID.fromString("00000000-0000-0000-0011-000000000007");
    public static final UUID TWIN_CLASS_FIELD_TWIN_CREATOR_USER = UUID.fromString("00000000-0000-0000-0011-000000000008");
    public static final UUID TWIN_CLASS_FIELD_TWIN_HEAD = UUID.fromString("00000000-0000-0000-0011-000000000009");
    public static final UUID TWIN_CLASS_FIELD_TWIN_STATUS = UUID.fromString("00000000-0000-0000-0011-000000000010");
    public static final UUID TWIN_CLASS_FIELD_TWIN_CREATED_AT = UUID.fromString("00000000-0000-0000-0011-000000000011");

    public static final UUID TWIN_STATUS_USER = UUID.fromString("00000000-0000-0000-0003-000000000001");
    public static final UUID TWIN_STATUS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0003-000000000003");

    public static final UUID TWIN_TEMPLATE_USER = UUID.fromString("00000000-0000-0000-0002-000000000001");
    public static final UUID TWIN_TEMPLATE_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0002-000000000003");

    public static final List<SystemClass> SYSTEM_CLASSES;
    public static Set<UUID> SYSTEM_TWIN_CLASS_FIELDS_UUIDS = new HashSet<>();

    static {
        SYSTEM_CLASSES = Collections.unmodifiableList(Arrays.asList(
                new SystemClass(
                        TWIN_CLASS_USER,
                        "USER",
                        List.of(new SystemStatus(TWIN_STATUS_USER, TWIN_CLASS_USER)),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_USER_EMAIL, TWIN_CLASS_USER, 1318, "email"),
                                new SystemField(TWIN_CLASS_FIELD_USER_AVATAR, TWIN_CLASS_USER, 1319, "avatar")
                        )
                ),
                new SystemClass(
                        TWIN_CLASS_BUSINESS_ACCOUNT,
                        "BUSINESS_ACCOUNT",
                        List.of(new SystemStatus(TWIN_STATUS_BUSINESS_ACCOUNT, TWIN_CLASS_BUSINESS_ACCOUNT)),
                        List.of()
                ),
                new SystemClass(
                        TWIN_CLASS_GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_TWIN_NAME, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, "base_name"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_DESCRIPTION, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, "base_description"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, "base_external_id"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_OWNER_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, "base_owner_user"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, "base_assignee_user"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATOR_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, "base_creator_user"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_HEAD, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, "base_head"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_STATUS, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, "base_status"),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATED_AT, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, "base_created_at")
                        )
                )
        ));
    }

    @PostConstruct
    public void postConstruct() throws ServiceException {
        UserEntity systemUser = new UserEntity()
                .setId(USER_SYSTEM)
                .setName("SYSTEM")
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(USER_SYSTEM, systemUser, userRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        for (SystemClass systemClass : SYSTEM_CLASSES) {
            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.getId())
                    .setKey(systemClass.getKey())
                    .setOwnerType(TwinClassEntity.OwnerType.SYSTEM)
                    .setCreatedByUserId(USER_SYSTEM)
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

            List<TwinStatusEntity> statusEntities = new ArrayList<>();
            for (SystemStatus status : systemClass.getStatuses()) {
                TwinStatusEntity statusEntity = new TwinStatusEntity()
                        .setId(status.getId())
                        .setTwinClassId(status.getTwinClassId());
                statusEntities.add(statusEntity);
            }
            entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);

            List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();
            for (SystemField field : systemClass.getFields()) {
                TwinClassFieldEntity fieldEntity = new TwinClassFieldEntity()
                        .setId(field.getId())
                        .setTwinClassId(field.getTwinClassId())
                        .setKey(field.getFieldKey())
                                .setFieldTyperFeaturerId(field.fieldTyperId);
                fieldEntities.add(fieldEntity);
            }
            entitySmartService.saveAllAndLog(fieldEntities, twinClassFieldRepository);
        }

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

    public static Set<UUID> getSystemFieldsIds() {
        if(!SYSTEM_TWIN_CLASS_FIELDS_UUIDS.isEmpty())
            return SYSTEM_TWIN_CLASS_FIELDS_UUIDS;
        for (SystemClass systemClass : SYSTEM_CLASSES)
            for(SystemField systemField : systemClass.getFields())
                SYSTEM_TWIN_CLASS_FIELDS_UUIDS.add(systemField.getId());
        return SYSTEM_TWIN_CLASS_FIELDS_UUIDS;
    }

    public static boolean isSystemField(UUID fieldId) {
        return getSystemFieldsIds().contains(fieldId);
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

    @Data
    @AllArgsConstructor
    public static class SystemClass {
        private UUID id;
        private String key;
        private List<SystemStatus> statuses;
        private List<SystemField> fields;
    }

    @Data
    @AllArgsConstructor
    public static class SystemStatus {
        private UUID id;
        private UUID twinClassId;
    }

    @Data
    @AllArgsConstructor
    public static class SystemField {
        private UUID id;
        private UUID twinClassId;
        private Integer fieldTyperId;
        private String fieldKey;
    }
}
