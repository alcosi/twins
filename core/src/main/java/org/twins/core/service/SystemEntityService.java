package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nRepository;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.cambium.common.util.LTreeUtils.convertToLTreeFormat;

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
    private final I18nRepository i18nRepository;
    private final I18nTranslationRepository i18nTranslationRepository;
    private final TwinClassSchemaRepository twinClassSchemaRepository;
    private final PermissionSchemaRepository permissionSchemaRepository;
    private final TwinflowSchemaRepository twinflowSchemaRepository;

    public static final List<SystemClass> SYSTEM_CLASSES;
    public static Set<UUID> SYSTEM_TWIN_CLASS_FIELDS_UUIDS = new HashSet<>();
    static {
        SYSTEM_CLASSES = Collections.unmodifiableList(Arrays.asList(
                new SystemClass(
                        SystemIds.TwinClass.USER,
                        "USER",
                        List.of(new SystemStatus(SystemIds.TwinStatus.USER, SystemIds.TwinClass.USER, true, new I18n(SystemIds.I18n.UserStatus.NAME, "Active"), new I18n(SystemIds.I18n.UserStatus.DESCRIPTION, "User is active"), StatusType.BASIC)),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.USER_EMAIL, SystemIds.TwinClass.USER, FeaturerTwins.ID_1318, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.UserField.EMAIL_NAME, "Email"), new I18n(SystemIds.I18n.UserField.EMAIL_DESCRIPTION, "User email address"), 4101, "email", false, true, true),
                                new SystemField(SystemIds.TwinClassField.USER_AVATAR, SystemIds.TwinClass.USER, FeaturerTwins.ID_1319, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.UserField.AVATAR_NAME, "Avatar"), new I18n(SystemIds.I18n.UserField.AVATAR_DESCRIPTION, "User avatar image"), 4101, "avatar", false, true, true)
                        ),
                        false,
                        true
                ),
                new SystemClass(
                        SystemIds.TwinClass.BUSINESS_ACCOUNT,
                        "BUSINESS_ACCOUNT",
                        List.of(new SystemStatus(SystemIds.TwinStatus.BUSINESS_ACCOUNT, SystemIds.TwinClass.BUSINESS_ACCOUNT, true, new I18n(SystemIds.I18n.BusinessAccountStatus.NAME, "Business Account"), new I18n(SystemIds.I18n.BusinessAccountStatus.DESCRIPTION, "Business Account status"), StatusType.BASIC)),
                        List.of(),
                        false,
                        false
                ),
                new SystemClass(
                        SystemIds.TwinClass.GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.TWIN_NAME, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.NAME_NAME, "Name"), new I18n(SystemIds.I18n.GlobalAncestorField.NAME_DESCRIPTION, "Twin name"), 4107, "base_name", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_DESCRIPTION, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_NAME, "Description"), new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_DESCRIPTION, "Twin description"), 4107, "base_description", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_EXTERNAL_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_NAME, "External ID"), new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_DESCRIPTION, "External identifier"), 4107, "base_external_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_OWNER_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_NAME, "Owner"), new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_DESCRIPTION, "Twin owner"), 4107, "base_owner_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_ASSIGNEE_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_NAME, "Assignee"), new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_DESCRIPTION, "Assigned user"), 4107, "base_assignee_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_CREATOR_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_NAME, "Creator"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_DESCRIPTION, "User who created the twin"), 4107, "base_creator_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_HEAD_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_NAME, "Head"), new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_DESCRIPTION, "Head twin"), 4107, "base_head", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_STATUS_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_NAME, "Status"), new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_DESCRIPTION, "Twin status"), 4107, "base_status", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_CREATED_AT, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_NAME, "Created At"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_DESCRIPTION, "Creation timestamp"), 4107, "base_created_at", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1327, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ID_NAME, "Id"), new I18n(SystemIds.I18n.GlobalAncestorField.ID_DESCRIPTION, "Twin id"), 4107, "base_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_TWIN_CLASS_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1328, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_NAME, "Twin class id"), new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_DESCRIPTION, "Twin class id"), 4107, "base_twin_class_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_ALIASES, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1329, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_NAME, "Aliases"), new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_DESCRIPTION, "Aliases"), 4101, "base_aliases", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_TAGS, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1330, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_NAME, "Tags"), new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_DESCRIPTION, "Tags"), 4101, "base_tags", false, true, true),
                                new SystemField(SystemIds.TwinClassField.TWIN_MARKERS, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1331, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_NAME, "Markers"), new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_DESCRIPTION, "Markers"), 4101, "base_markers", false, true, true)
                        ),
                        true,
                        false
                ),
                new SystemClass(
                        SystemIds.TwinClass.FACE_PAGE,
                        "FACE_PAGE",
                        List.of(new SystemStatus(SystemIds.TwinStatus.FACE_PAGE, SystemIds.TwinClass.FACE_PAGE, true, new I18n(SystemIds.I18n.FacePageStatus.NAME, "Published"), new I18n(SystemIds.I18n.FacePageStatus.DESCRIPTION, "Face page published"), StatusType.BASIC)),
                        List.of(),
                        false,
                        true
                )
        ));
    }
    @PostConstruct
    public void postConstruct() throws ServiceException {

        PermissionSchemaEntity permissionSchema = new PermissionSchemaEntity()
                .setId(SystemIds.PermissionScheme.DEFAULT)
                .setName("System permission schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.PermissionScheme.DEFAULT, permissionSchema, permissionSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinClassSchemaEntity twinClassSchemaEntity = new TwinClassSchemaEntity()
                .setId(SystemIds.TwinClassScheme.DEFAULT)
                .setName("System twinclass schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.TwinClassScheme.DEFAULT, twinClassSchemaEntity, twinClassSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinflowSchemaEntity twinflowSchemaEntity = new TwinflowSchemaEntity()
                .setId(SystemIds.TwinflowScheme.DEFAULT)
                .setName("System twinflow schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.TwinflowScheme.DEFAULT, twinflowSchemaEntity, twinflowSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        UserEntity systemUser = new UserEntity()
                .setId(SystemIds.User.SYSTEM)
                .setName("SYSTEM")
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.User.SYSTEM, systemUser, userRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        List<I18nEntity> i18nEntities = new ArrayList<>();
        List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();
        List<TwinStatusEntity> statusEntities = new ArrayList<>();
        List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();

        for (SystemClass systemClass : SYSTEM_CLASSES) {
            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.id())
                    .setKey(systemClass.key())
                    .setOwnerType(OwnerType.SYSTEM)
                    .setCreatedByUserId(SystemIds.User.SYSTEM)
                    .setAbstractt(systemClass.abstractt)
                    .setExtendsHierarchyTree(convertToLTreeFormat(systemClass.id))
                    .setAssigneeRequired(systemClass.assigneeRequired)
                    .setSegment(false)
                    .setHasSegment(false)
                    .setUniqueName(false)
                    .setHasDynamicMarkers(false)
                    .setHeadHierarchyCounterDirectChildren(0)
                    .setExtendsHierarchyCounterDirectChildren(0)
                    .setTwinCounter(0)
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndLogOnException);

            for (SystemStatus status : systemClass.statuses()) {
                i18nEntities.add(new I18nEntity()
                        .setId(status.name().i18nId())
                        .setType(I18nType.TWIN_STATUS_NAME));
                i18nEntities.add(new I18nEntity()
                        .setId(status.description().i18nId())
                        .setType(I18nType.TWIN_STATUS_DESCRIPTION));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(status.name().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(status.name().translation()));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(status.description().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(status.description().translation()));
                statusEntities.add(new TwinStatusEntity()
                        .setId(status.id())
                        .setNameI18nId(status.name().i18nId())
                        .setDescriptionI18nId(status.description().i18nId())
                        .setTwinClassId(status.twinClassId())
                        .setInheritable(status.inheritable())
                        .setType(status.type()));
            }

            for (SystemField field : systemClass.fields()) {
                i18nEntities.add(new I18nEntity()
                        .setId(field.name().i18nId())
                        .setType(I18nType.TWIN_CLASS_FIELD_NAME));
                i18nEntities.add(new I18nEntity()
                        .setId(field.description().i18nId())
                        .setType(I18nType.TWIN_CLASS_FIELD_DESCRIPTION));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(field.name().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(field.name().translation()));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(field.description().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(field.description().translation()));
                fieldEntities.add(new TwinClassFieldEntity()
                        .setId(field.id())
                        .setTwinClassId(field.twinClassId())
                        .setKey(field.fieldKey())
                        .setNameI18nId(field.name().i18nId())
                        .setDescriptionI18nId(field.description().i18nId())
                        .setFieldTyperFeaturerId(field.fieldTyperId())
                        .setFieldInitializerFeaturerId(field.fieldInitializerFeaturerId())
                        .setTwinSorterFeaturerId(field.twinSorterFeaturerId())
                        .setRequired(field.required())
                        .setSystem(field.system())
                        .setInheritable(field.inheritable())
                        .setDependentField(false)
                        .setHasDependentFields(false)
                        .setProjectionField(false)
                        .setHasProjectedFields(false));
            }
        }
        entitySmartService.saveAllAndLog(i18nEntities, i18nRepository);
        entitySmartService.saveAllAndLog(i18nTranslationEntities, i18nTranslationRepository);
        entitySmartService.saveAllAndLog(fieldEntities, twinClassFieldRepository);
        entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);

        TwinEntity twinEntity;
        twinEntity = new TwinEntity()
                .setId(SystemIds.TwinTemplate.USER)
                .setName("User")
                .setTwinClassId(SystemIds.TwinClass.USER)
                .setTwinStatusId(SystemIds.TwinStatus.USER)
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinEntity = new TwinEntity()
                .setId(SystemIds.TwinTemplate.BUSINESS_ACCOUNT)
                .setName("Business account")
                .setTwinClassId(SystemIds.TwinClass.BUSINESS_ACCOUNT)
                .setTwinStatusId(SystemIds.TwinStatus.BUSINESS_ACCOUNT)
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
    }

    public UUID getUserIdSystem() {
        return SystemIds.User.SYSTEM;
    }

    public static boolean isTwinClassForUser(UUID twinClassId) {
        return SystemIds.TwinClass.USER.equals(twinClassId);
    }

    public static Set<UUID> getSystemFieldsIds() {
        if (!SYSTEM_TWIN_CLASS_FIELDS_UUIDS.isEmpty())
            return SYSTEM_TWIN_CLASS_FIELDS_UUIDS;
        for (SystemClass systemClass : SYSTEM_CLASSES)
            for (SystemField systemField : systemClass.fields())
                SYSTEM_TWIN_CLASS_FIELDS_UUIDS.add(systemField.id());
        return SYSTEM_TWIN_CLASS_FIELDS_UUIDS;
    }

    public static boolean isSystemField(UUID fieldId) {
        return getSystemFieldsIds().contains(fieldId);
    }

    public static boolean isTwinClassForBusinessAccount(UUID twinClassId) {
        return SystemIds.TwinClass.BUSINESS_ACCOUNT.equals(twinClassId);
    }

    public static boolean isSystemClass(UUID twinClassId) {
        return isTwinClassForBusinessAccount(twinClassId) || isTwinClassForUser(twinClassId);
    }

    public UUID getTwinIdTemplateForUser() {
        return SystemIds.TwinTemplate.USER;
    }

    public UUID getTwinIdTemplateForBusinessAccount() {
        return SystemIds.TwinTemplate.BUSINESS_ACCOUNT;
    }

    public TwinEntity createTwinTemplateDomainBusinessAccount(UUID domainId) throws ServiceException {
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainId)
                .setKey("DOMAIN_BUSINESS_ACCOUNT")
                .setOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT)
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setInheritable(true);
        twinStatusEntity = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        TwinEntity twinEntity = new TwinEntity()
                .setName("Domain business account")
                .setTwinClassId(twinClassEntity.getId())
                .setTwinStatusId(twinStatusEntity.getId())
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        twinEntity = entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinEntity;
    }

    public record SystemClass(UUID id, String key, List<SystemStatus> statuses, List<SystemField> fields,
                              boolean abstractt, boolean assigneeRequired) {
    }

    public record SystemStatus(UUID id, UUID twinClassId, Boolean inheritable, I18n name, I18n description, StatusType type) {
    }

    public record SystemField(UUID id, UUID twinClassId, Integer fieldTyperId, Integer fieldInitializerFeaturerId,
                              I18n name, I18n description, Integer twinSorterFeaturerId, String fieldKey,
                              Boolean required, Boolean system, Boolean inheritable) {
    }

    public record I18n(UUID i18nId, String translation) {
    }

    public static Object getSystemFieldValue(TwinEntity twinEntity, UUID systemFieldId) throws ServiceException {
        //todo to use deserialize logic in future
        if (systemFieldId == null || twinEntity == null) {
            return null;
        }
        var basicField = TwinEntity.BasicField.convertOrNull(systemFieldId);
        if (basicField != null)
            return basicField.getValue(twinEntity);
        return null;
    }
}
