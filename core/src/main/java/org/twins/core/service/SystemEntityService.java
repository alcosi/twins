package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.*;
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

    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_USER_EMAIL = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000001"),
            UUID.fromString("00000000-0000-0000-0013-000000000001")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_USER_AVATAR = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000002"),
            UUID.fromString("00000000-0000-0000-0013-000000000002")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_TWIN_NAME = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000003"),
            UUID.fromString("00000000-0000-0000-0013-000000000003")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_DESCRIPTION = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000004"),
            UUID.fromString("00000000-0000-0000-0013-000000000004")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_EXTERNAL_ID = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000005"),
            UUID.fromString("00000000-0000-0000-0013-000000000005")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_OWNER_USER = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000006"),
            UUID.fromString("00000000-0000-0000-0013-000000000006")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_ASSIGNEE_USER = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000007"),
            UUID.fromString("00000000-0000-0000-0013-000000000007")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_CREATOR_USER = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000008"),
            UUID.fromString("00000000-0000-0000-0013-000000000008")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_HEAD = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000009"),
            UUID.fromString("00000000-0000-0000-0013-000000000009")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_STATUS = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000010"),
            UUID.fromString("00000000-0000-0000-0013-000000000010")
    );
    public static final ImmutablePair<UUID, UUID> TWIN_CLASS_FIELD_I18N_CREATED_AT = ImmutablePair.of(
            UUID.fromString("00000000-0000-0000-0012-000000000011"),
            UUID.fromString("00000000-0000-0000-0013-000000000011")
    );

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
                                new SystemField(TWIN_CLASS_FIELD_USER_EMAIL, TWIN_CLASS_USER, TWIN_CLASS_FIELD_I18N_USER_EMAIL, 1318, "Email", "User email address", "email", false),
                                new SystemField(TWIN_CLASS_FIELD_USER_AVATAR, TWIN_CLASS_USER, TWIN_CLASS_FIELD_I18N_USER_AVATAR, 1319, "Avatar", "User avatar image", "avatar", false)
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
                                new SystemField(TWIN_CLASS_FIELD_TWIN_NAME, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_TWIN_NAME, FeaturerTwins.ID_1321, "Name", "Twin name", "base_name", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_DESCRIPTION, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_DESCRIPTION, FeaturerTwins.ID_1321, "Description", "Twin description", "base_description", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_EXTERNAL_ID, FeaturerTwins.ID_1321, "External ID", "External identifier", "base_external_id", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_OWNER_USER, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_OWNER_USER, FeaturerTwins.ID_1322, "Owner", "Twin owner", "base_owner_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_ASSIGNEE_USER, FeaturerTwins.ID_1322, "Assignee", "Assigned user", "base_assignee_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATOR_USER, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_CREATOR_USER, FeaturerTwins.ID_1322, "Creator", "User who created the twin", "base_creator_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_HEAD, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_HEAD, FeaturerTwins.ID_1323, "Head", "Head twin", "base_head", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_STATUS, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_STATUS, FeaturerTwins.ID_1324, "Status", "Twin status", "base_status", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATED_AT, TWIN_CLASS_GLOBAL_ANCESTOR, TWIN_CLASS_FIELD_I18N_CREATED_AT, FeaturerTwins.ID_1325, "Created At", "Creation timestamp", "base_created_at", false)
                        )
                )
        ));
    }

    private final I18nRepository i18nRepository;
    private final I18nTranslationRepository i18nTranslationRepository;

    @PostConstruct
    public void postConstruct() throws ServiceException {
        UserEntity systemUser = new UserEntity()
                .setId(USER_SYSTEM)
                .setName("SYSTEM")
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(USER_SYSTEM, systemUser, userRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        for (SystemClass systemClass : SYSTEM_CLASSES) {
            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.id())
                    .setKey(systemClass.key())
                    .setOwnerType(TwinClassEntity.OwnerType.SYSTEM)
                    .setCreatedByUserId(USER_SYSTEM)
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

            List<TwinStatusEntity> statusEntities = new ArrayList<>();
            for (SystemStatus status : systemClass.statuses()) {
                TwinStatusEntity statusEntity = new TwinStatusEntity()
                        .setId(status.id())
                        .setTwinClassId(status.twinClassId());
                statusEntities.add(statusEntity);
            }
            entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);

            List<I18nEntity> i18nEntities = new ArrayList<>();
            List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();
            List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();
            for (SystemField field : systemClass.fields()) {
                I18nEntity i18nName = new I18nEntity().setId(field.nameAndDescriptionI18nId().left).setType(I18nType.TWIN_CLASS_FIELD_NAME);
                I18nTranslationEntity nameTranslation = new I18nTranslationEntity()
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(field.translationName());
                I18nEntity i18nDescription = new I18nEntity().setId(field.nameAndDescriptionI18nId().right).setType(I18nType.TWIN_CLASS_FIELD_DESCRIPTION);
                I18nTranslationEntity descriptionTranslation = new I18nTranslationEntity()
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(field.translationDescription());
                TwinClassFieldEntity fieldEntity = new TwinClassFieldEntity()
                        .setId(field.id())
                        .setTwinClassId(field.twinClassId())
                        .setKey(field.fieldKey())
                        .setNameI18nId(i18nName.getId())
                        .setDescriptionI18nId(i18nDescription.getId())
                        .setFieldTyperFeaturerId(field.fieldTyperId())
                        .setRequired(field.required());
                i18nEntities.add(i18nName);
                i18nEntities.add(i18nDescription);
                i18nTranslationEntities.add(nameTranslation);
                i18nTranslationEntities.add(descriptionTranslation);
                fieldEntities.add(fieldEntity);
            }
            entitySmartService.saveAllAndLog(i18nEntities, i18nRepository);
            entitySmartService.saveAllAndLog(i18nTranslationEntities, i18nTranslationRepository);
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
            for(SystemField systemField : systemClass.fields())
                SYSTEM_TWIN_CLASS_FIELDS_UUIDS.add(systemField.id());
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

    public record SystemClass(UUID id, String key, List<SystemStatus> statuses, List<SystemField> fields) {}

    public record SystemStatus(UUID id, UUID twinClassId) {}

    public record SystemField(UUID id, UUID twinClassId, ImmutablePair<UUID, UUID> nameAndDescriptionI18nId, Integer fieldTyperId, String translationName, String translationDescription, String fieldKey, Boolean required) {}
}
