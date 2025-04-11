package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public static final UUID I18N_NAME_ID_EMAIL = UUID.fromString("00000000-0000-0000-0012-000000000001");
    public static final UUID I18N_NAME_ID_AVATAR = UUID.fromString("00000000-0000-0000-0012-000000000002");
    public static final UUID I18N_NAME_ID_NAME = UUID.fromString("00000000-0000-0000-0012-000000000003");
    public static final UUID I18N_NAME_ID_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000004");
    public static final UUID I18N_NAME_ID_EXTERNAL_ID = UUID.fromString("00000000-0000-0000-0012-000000000005");
    public static final UUID I18N_NAME_ID_OWNER_USE = UUID.fromString("00000000-0000-0000-0012-000000000006");
    public static final UUID I18N_NAME_ID_ASSIGNEE_USER = UUID.fromString("00000000-0000-0000-0012-000000000007");
    public static final UUID I18N_NAME_ID_CREATOR_USER = UUID.fromString("00000000-0000-0000-0012-000000000008");
    public static final UUID I18N_NAME_ID_HEAD = UUID.fromString("00000000-0000-0000-0012-000000000009");
    public static final UUID I18N_NAME_ID_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000010");
    public static final UUID I18N_NAME_ID_CREATED_AT = UUID.fromString("00000000-0000-0000-0012-000000000011");
    public static final UUID I18N_NAME_ID_USER_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000023");
    public static final UUID I18N_NAME_ID_BUSINESS_ACCOUNT_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000024");

    public static final UUID I18N_DESCRIPTION_ID_EMAIL = UUID.fromString("00000000-0000-0000-0012-000000000012");
    public static final UUID I18N_DESCRIPTION_ID_AVATAR = UUID.fromString("00000000-0000-0000-0012-000000000013");
    public static final UUID I18N_DESCRIPTION_ID_NAME = UUID.fromString("00000000-0000-0000-0012-000000000014");
    public static final UUID I18N_DESCRIPTION_ID_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000015");
    public static final UUID I18N_DESCRIPTION_ID_EXTERNAL_ID = UUID.fromString("00000000-0000-0000-0012-000000000016");
    public static final UUID I18N_DESCRIPTION_ID_OWNER_USE = UUID.fromString("00000000-0000-0000-0012-000000000017");
    public static final UUID I18N_DESCRIPTION_ID_ASSIGNEE_USER = UUID.fromString("00000000-0000-0000-0012-000000000018");
    public static final UUID I18N_DESCRIPTION_ID_CREATOR_USER = UUID.fromString("00000000-0000-0000-0012-000000000019");
    public static final UUID I18N_DESCRIPTION_ID_HEAD = UUID.fromString("00000000-0000-0000-0012-000000000020");
    public static final UUID I18N_DESCRIPTION_ID_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000021");
    public static final UUID I18N_DESCRIPTION_ID_CREATED_AT = UUID.fromString("00000000-0000-0000-0012-000000000022");
    public static final UUID I18N_DESCRIPTION_ID_USER_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000025");
    public static final UUID I18N_DESCRIPTION_ID_BUSINESS_ACCOUNT_STATUS = UUID.fromString("00000000-0000-0000-0012-000000000026");

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
                        List.of(new SystemStatus(TWIN_STATUS_USER, TWIN_CLASS_USER, new I18n(I18N_NAME_ID_USER_STATUS, "User"), new I18n(I18N_DESCRIPTION_ID_USER_STATUS, "User status"))),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_USER_EMAIL, TWIN_CLASS_USER, 1318, new I18n(I18N_NAME_ID_EMAIL, "Email"), new I18n(I18N_DESCRIPTION_ID_EMAIL, "User email address"), "email", false),
                                new SystemField(TWIN_CLASS_FIELD_USER_AVATAR, TWIN_CLASS_USER, 1319, new I18n(I18N_NAME_ID_AVATAR, "Avatar"), new I18n(I18N_DESCRIPTION_ID_AVATAR, "User avatar image"),  "avatar", false)
                        )
                ),
                new SystemClass(
                        TWIN_CLASS_BUSINESS_ACCOUNT,
                        "BUSINESS_ACCOUNT",
                        List.of(new SystemStatus(TWIN_STATUS_BUSINESS_ACCOUNT, TWIN_CLASS_BUSINESS_ACCOUNT, new I18n(I18N_NAME_ID_BUSINESS_ACCOUNT_STATUS, "Business Account"), new I18n(I18N_DESCRIPTION_ID_BUSINESS_ACCOUNT_STATUS, "Business Account status"))),
                        List.of()
                ),
                new SystemClass(
                        TWIN_CLASS_GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_TWIN_NAME, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_NAME_ID_NAME, "Name"), new I18n(I18N_DESCRIPTION_ID_NAME, "Twin name"), "base_name", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_DESCRIPTION, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_NAME_ID_DESCRIPTION, "Description"), new I18n(I18N_DESCRIPTION_ID_DESCRIPTION, "Twin description"), "base_description", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_NAME_ID_EXTERNAL_ID, "External ID"), new I18n(I18N_DESCRIPTION_ID_EXTERNAL_ID, "External identifier"), "base_external_id", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_OWNER_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_NAME_ID_OWNER_USE, "Owner"), new I18n(I18N_DESCRIPTION_ID_OWNER_USE, "Twin owner"), "base_owner_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_NAME_ID_ASSIGNEE_USER, "Assignee"), new I18n(I18N_DESCRIPTION_ID_ASSIGNEE_USER, "Assigned user"), "base_assignee_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATOR_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_NAME_ID_CREATOR_USER, "Creator"), new I18n(I18N_DESCRIPTION_ID_CREATOR_USER, "User who created the twin"), "base_creator_user", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_HEAD, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, new I18n(I18N_NAME_ID_HEAD, "Head"), new I18n(I18N_DESCRIPTION_ID_HEAD, "Head twin"), "base_head", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_STATUS, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, new I18n(I18N_NAME_ID_STATUS, "Status"), new I18n(I18N_DESCRIPTION_ID_STATUS, "Twin status"), "base_status", false),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATED_AT, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, new I18n(I18N_NAME_ID_CREATED_AT, "Created At"), new I18n(I18N_DESCRIPTION_ID_CREATED_AT, "Creation timestamp"), "base_created_at", false)
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
            List<I18nEntity> i18nEntities = new ArrayList<>();
            List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();

            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.id())
                    .setKey(systemClass.key())
                    .setOwnerType(TwinClassEntity.OwnerType.SYSTEM)
                    .setCreatedByUserId(USER_SYSTEM)
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

            List<TwinStatusEntity> statusEntities = new ArrayList<>();
            for (SystemStatus status : systemClass.statuses()) {
                I18nEntity i18nName = new I18nEntity().setId(status.name().i18nId()).setType(I18nType.TWIN_STATUS_NAME);
                I18nTranslationEntity nameTranslation = new I18nTranslationEntity()
                        .setI18nId(status.name().i18nId())
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(status.name().translation());
                I18nEntity i18nDescription = new I18nEntity().setId(status.description().i18nId()).setType(I18nType.TWIN_STATUS_DESCRIPTION);
                I18nTranslationEntity descriptionTranslation = new I18nTranslationEntity()
                        .setI18nId(status.description().i18nId())
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(status.description().translation());
                TwinStatusEntity statusEntity = new TwinStatusEntity()
                        .setId(status.id())
                        .setNameI18nId(i18nName.getId())
                        .setDescriptionI18nId(i18nDescription.getId())
                        .setTwinClassId(status.twinClassId());
                i18nEntities.add(i18nName);
                i18nEntities.add(i18nDescription);
                i18nTranslationEntities.add(nameTranslation);
                i18nTranslationEntities.add(descriptionTranslation);
                statusEntities.add(statusEntity);
            }
            entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);

            List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();
            for (SystemField field : systemClass.fields()) {
                I18nEntity i18nName = new I18nEntity().setId(field.name().i18nId()).setType(I18nType.TWIN_CLASS_FIELD_NAME);
                I18nTranslationEntity nameTranslation = new I18nTranslationEntity()
                        .setI18nId(field.name().i18nId())
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(field.name().translation());
                I18nEntity i18nDescription = new I18nEntity().setId(field.description().i18nId()).setType(I18nType.TWIN_CLASS_FIELD_DESCRIPTION);
                I18nTranslationEntity descriptionTranslation = new I18nTranslationEntity()
                        .setI18nId(field.description().i18nId())
                        .setLocale(Locale.forLanguageTag("en"))
                        .setTranslation(field.description().translation());
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

    public record SystemStatus(UUID id, UUID twinClassId, I18n name, I18n description) {}

    public record SystemField(UUID id, UUID twinClassId, Integer fieldTyperId, I18n name, I18n description, String fieldKey, Boolean required) {}

    public record I18n(UUID i18nId, String translation) {}
}
