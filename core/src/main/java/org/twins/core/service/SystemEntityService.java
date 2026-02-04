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

    // last type.id = 0015
    public static final UUID USER_SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID TWIN_CLASS_USER = UUID.fromString("00000000-0000-0000-0001-000000000001");
    public static final UUID TWIN_CLASS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0001-000000000003");
    public static final UUID TWIN_CLASS_GLOBAL_ANCESTOR = UUID.fromString("00000000-0000-0000-0001-000000000004");
    public static final UUID TWIN_CLASS_FACE_PAGE = UUID.fromString("00000000-0000-0000-0001-000000000005");

    // last field.id = 16
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
    public static final UUID TWIN_CLASS_FIELD_TWIN_ID = UUID.fromString("00000000-0000-0000-0011-000000000012");
    public static final UUID TWIN_CLASS_FIELD_TWIN_TWIN_CLASS_ID = UUID.fromString("00000000-0000-0000-0011-000000000013");
    public static final UUID TWIN_CLASS_FIELD_TWIN_ALIASES = UUID.fromString("00000000-0000-0000-0011-000000000014");
    public static final UUID TWIN_CLASS_FIELD_TWIN_TAGS = UUID.fromString("00000000-0000-0000-0011-000000000015");
    public static final UUID TWIN_CLASS_FIELD_TWIN_MARKERS = UUID.fromString("00000000-0000-0000-0011-000000000016");
    // last i18.id = 46
    public static final UUID I18N_4CLASS_USER_FIELD_EMAIL_NAME = UUID.fromString("00000000-0000-0000-0012-000000000001");
    public static final UUID I18N_4CLASS_USER_FIELD_AVATAR_NAME = UUID.fromString("00000000-0000-0000-0012-000000000002");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_NAME_NAME = UUID.fromString("00000000-0000-0000-0012-000000000003");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_DESCRIPTION_NAME = UUID.fromString("00000000-0000-0000-0012-000000000004");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_EXTERNAL_ID_NAME = UUID.fromString("00000000-0000-0000-0012-000000000005");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_OWNER_USER_NAME = UUID.fromString("00000000-0000-0000-0012-000000000006");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ASSIGNEE_NAME = UUID.fromString("00000000-0000-0000-0012-000000000007");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATOR_NAME = UUID.fromString("00000000-0000-0000-0012-000000000008");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_HEAD_NAME = UUID.fromString("00000000-0000-0000-0012-000000000009");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_STATUS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000010");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATED_AT_NAME = UUID.fromString("00000000-0000-0000-0012-000000000011");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ID_NAME = UUID.fromString("00000000-0000-0000-0012-000000000027");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TWIN_CLASS_ID_NAME = UUID.fromString("00000000-0000-0000-0012-000000000028");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ALIASES_NAME = UUID.fromString("00000000-0000-0000-0012-000000000029");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TAGS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000030");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_MARKERS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000031");
    public static final UUID I18N_4CLASS_USER_STATUS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000023");
    public static final UUID I18N_4CLASS_BUSINESS_ACCOUNT_STATUS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000024");
    public static final UUID I18N_4CLASS_FACE_PAGE_STATUS_NAME = UUID.fromString("00000000-0000-0000-0012-000000000037");

    public static final UUID I18N_4CLASS_USER_FIELD_EMAIL_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000012");
    public static final UUID I18N_4CLASS_USER_FIELD_AVATAR_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000013");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_NAME_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000014");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_DESCRIPTION_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000015");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_EXTERNAL_ID_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000016");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_OWNER_USER_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000017");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ASSIGNEE_USER_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000018");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATOR_USER_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000019");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_HEAD_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000020");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_STATUS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000021");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATED_AT_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000022");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ID_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000032");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TWIN_CLASS_ID_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000033");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ALIASES_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000034");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TAGS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000035");
    public static final UUID I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_MARKERS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000036");
    public static final UUID I18N_4CLASS_USER_STATUS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000025");
    public static final UUID I18N_4CLASS_BUSINESS_ACCOUNT_STATUS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000026");
    public static final UUID I18N_4CLASS_FACE_PAGE_STATUS_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000038");

    public static final UUID TWIN_ATTACHMENT_EXTERNAL_URI_STORAGER_ID = UUID.fromString("00000000-0000-0000-0013-000000000002");
    public static final UUID TWIN_STATUS_USER = UUID.fromString("00000000-0000-0000-0003-000000000001");
    public static final UUID TWIN_STATUS_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0003-000000000003");
    public static final UUID TWIN_STATUS_FACE_PAGE = UUID.fromString("00000000-0000-0000-0003-000000000004");

    public static final UUID TWIN_STATUS_SKETCH = UUID.fromString("00000001-0000-0000-0000-000000000001"); //todo changge my id

    public static final UUID TWIN_TEMPLATE_USER = UUID.fromString("00000000-0000-0000-0002-000000000001");
    public static final UUID TWIN_TEMPLATE_BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0002-000000000003");

    public static final UUID TWIN_CLASS_FIELD_SEARCH_UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000001");
    public static final UUID TWIN_SEARCH_UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000002");
    public static final UUID TWIN_CLASS_SEARCH_UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000003");
    public static final UUID USER_SEARCH_UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000004");
    public static final UUID DATA_LIST_OPTION_SEARCH_UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000005");

    public static final List<SystemClass> SYSTEM_CLASSES;
    public static Set<UUID> SYSTEM_TWIN_CLASS_FIELDS_UUIDS = new HashSet<>();

    static {
        SYSTEM_CLASSES = Collections.unmodifiableList(Arrays.asList(
                new SystemClass(
                        TWIN_CLASS_USER,
                        "USER",
                        List.of(new SystemStatus(TWIN_STATUS_USER, TWIN_CLASS_USER, new I18n(I18N_4CLASS_USER_STATUS_NAME, "Active"), new I18n(I18N_4CLASS_USER_STATUS_DESCRIPTION, "User is active"), StatusType.BASIC)),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_USER_EMAIL, TWIN_CLASS_USER, 1318, new I18n(I18N_4CLASS_USER_FIELD_EMAIL_NAME, "Email"), new I18n(I18N_4CLASS_USER_FIELD_EMAIL_DESCRIPTION, "User email address"), 4101,"email", false, true),
                                new SystemField(TWIN_CLASS_FIELD_USER_AVATAR, TWIN_CLASS_USER, 1319, new I18n(I18N_4CLASS_USER_FIELD_AVATAR_NAME, "Avatar"), new I18n(I18N_4CLASS_USER_FIELD_AVATAR_DESCRIPTION, "User avatar image"), 4101,"avatar", false, true)
                        ),
                        false,
                        true
                ),
                new SystemClass(
                        TWIN_CLASS_BUSINESS_ACCOUNT,
                        "BUSINESS_ACCOUNT",
                        List.of(new SystemStatus(TWIN_STATUS_BUSINESS_ACCOUNT, TWIN_CLASS_BUSINESS_ACCOUNT, new I18n(I18N_4CLASS_BUSINESS_ACCOUNT_STATUS_NAME, "Business Account"), new I18n(I18N_4CLASS_BUSINESS_ACCOUNT_STATUS_DESCRIPTION, "Business Account status"), StatusType.BASIC)),
                        List.of(),
                        false,
                        false
                ),
                new SystemClass(
                        TWIN_CLASS_GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(TWIN_CLASS_FIELD_TWIN_NAME, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_NAME_NAME, "Name"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_NAME_DESCRIPTION, "Twin name"), 4107,"base_name", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_DESCRIPTION, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_DESCRIPTION_NAME, "Description"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_DESCRIPTION_DESCRIPTION, "Twin description"), 4107,"base_description", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_EXTERNAL_ID_NAME, "External ID"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_EXTERNAL_ID_DESCRIPTION, "External identifier"), 4107,"base_external_id", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_OWNER_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_OWNER_USER_NAME, "Owner"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_OWNER_USER_DESCRIPTION, "Twin owner"), 4107,"base_owner_user", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ASSIGNEE_NAME, "Assignee"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ASSIGNEE_USER_DESCRIPTION, "Assigned user"), 4107,"base_assignee_user", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATOR_USER, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATOR_NAME, "Creator"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATOR_USER_DESCRIPTION, "User who created the twin"), 4107,"base_creator_user", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_HEAD, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_HEAD_NAME, "Head"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_HEAD_DESCRIPTION, "Head twin"), 4107,"base_head", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_STATUS, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_STATUS_NAME, "Status"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_STATUS_DESCRIPTION, "Twin status"), 4107,"base_status", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_CREATED_AT, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATED_AT_NAME, "Created At"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_CREATED_AT_DESCRIPTION, "Creation timestamp"), 4107,"base_created_at", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ID, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1327, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ID_NAME, "Id"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ID_DESCRIPTION, "Twin id"), 4107,"base_id", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_TWIN_CLASS_ID, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1328, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TWIN_CLASS_ID_NAME, "Twin class id"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TWIN_CLASS_ID_DESCRIPTION, "Twin class id"), 4107,"base_twin_class_id", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_ALIASES, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1329, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ALIASES_NAME, "Aliases"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_ALIASES_DESCRIPTION, "Aliases"), 4101,"base_aliases", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_TAGS, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1330, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TAGS_NAME, "Tags"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_TAGS_DESCRIPTION, "Tags"), 4101,"base_tags", false, true),
                                new SystemField(TWIN_CLASS_FIELD_TWIN_MARKERS, TWIN_CLASS_GLOBAL_ANCESTOR, FeaturerTwins.ID_1331, new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_MARKERS_NAME, "Markers"), new I18n(I18N_4CLASS_GLOBAL_ANCESTOR_FIELD_MARKERS_DESCRIPTION, "Markers"), 4101,"base_markers", false, true)
                        ),
                        true,
                        false
                ),
                new SystemClass(
                        TWIN_CLASS_FACE_PAGE,
                        "FACE_PAGE",
                        List.of(new SystemStatus(TWIN_STATUS_FACE_PAGE, TWIN_CLASS_FACE_PAGE, new I18n(I18N_4CLASS_FACE_PAGE_STATUS_NAME, "Published"), new I18n(I18N_4CLASS_FACE_PAGE_STATUS_DESCRIPTION, "Face page published"), StatusType.BASIC)),
                        List.of(),
                        false,
                        true
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

        List<I18nEntity> i18nEntities = new ArrayList<>();
        List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();
        List<TwinStatusEntity> statusEntities = new ArrayList<>();
        List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();

        for (SystemClass systemClass : SYSTEM_CLASSES) {
            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.id())
                    .setKey(systemClass.key())
                    .setOwnerType(OwnerType.SYSTEM)
                    .setCreatedByUserId(USER_SYSTEM)
                    .setAbstractt(systemClass.abstractt)
                    .setExtendsHierarchyTree(convertToLTreeFormat(systemClass.id))
                    .setAssigneeRequired(systemClass.assigneeRequired)
                    .setSegment(false)
                    .setHasSegment(false)
                    .setUniqueName(false)
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
                        .setTwinSorterFeaturerId(field.twinSorterFeaturerId())
                        .setRequired(field.required())
                        .setSystem(field.system())
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
                .setOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT)
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

    public record SystemClass(UUID id, String key, List<SystemStatus> statuses, List<SystemField> fields, boolean abstractt, boolean assigneeRequired) {}

    public record SystemStatus(UUID id, UUID twinClassId, I18n name, I18n description, StatusType type) {}

    public record SystemField(UUID id, UUID twinClassId, Integer fieldTyperId, I18n name, I18n description, Integer twinSorterFeaturerId, String fieldKey, Boolean required, Boolean system) { }

    public record I18n(UUID i18nId, String translation) {}

    public static Object getSystemFieldValue(TwinEntity twinEntity, UUID systemFieldId) throws ServiceException {
        //todo to use deserialize logic in future
        if (systemFieldId == null || twinEntity == null) {
            return null;
        }

        if (TWIN_CLASS_FIELD_TWIN_NAME.equals(systemFieldId)) {
            return twinEntity.getName();
        }
        if (TWIN_CLASS_FIELD_TWIN_DESCRIPTION.equals(systemFieldId)) {
            return twinEntity.getDescription();
        }
        if (TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID.equals(systemFieldId)) {
            return twinEntity.getExternalId();
        }
        if (TWIN_CLASS_FIELD_TWIN_OWNER_USER.equals(systemFieldId)) {
            return twinEntity.getOwnerUserId();
        }
        if (TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER.equals(systemFieldId)) {
            return twinEntity.getAssignerUserId();
        }
        if (TWIN_CLASS_FIELD_TWIN_CREATOR_USER.equals(systemFieldId)) {
            return twinEntity.getCreatedByUserId();
        }
        if (TWIN_CLASS_FIELD_TWIN_HEAD.equals(systemFieldId)) {
            return twinEntity.getHeadTwinId();
        }
        if (TWIN_CLASS_FIELD_TWIN_STATUS.equals(systemFieldId)) {
            return twinEntity.getTwinStatusId();
        }
        if (TWIN_CLASS_FIELD_TWIN_CREATED_AT.equals(systemFieldId)) {
            return twinEntity.getCreatedAt();
        }
        if (TWIN_CLASS_FIELD_TWIN_ID.equals(systemFieldId)) {
            return twinEntity.getId();
        }
        if (TWIN_CLASS_FIELD_TWIN_TWIN_CLASS_ID.equals(systemFieldId)) {
            return twinEntity.getTwinClassId();
        }
        if (TWIN_CLASS_FIELD_TWIN_ALIASES.equals(systemFieldId)) {
            return twinEntity.getTwinAliases();
        }
        if (TWIN_CLASS_FIELD_TWIN_TAGS.equals(systemFieldId)) {
            return twinEntity.getTags();
        }
        if (TWIN_CLASS_FIELD_TWIN_MARKERS.equals(systemFieldId)) {
            return twinEntity.getMarkers();
        }
        return null;
    }
}
