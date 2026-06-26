package org.twins.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nRepository;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
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
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
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
    private final LinkRepository linkRepository;
    private final DataListRepository dataListRepository;
    private final DataListOptionRepository dataListOptionRepository;

    public static final List<SystemClass> SYSTEM_CLASSES;
    public static final List<SystemLink> SYSTEM_LINKS;
    public static final List<SystemDataList> SYSTEM_DATA_LISTS;
    public static Set<UUID> SYSTEM_TWIN_CLASS_FIELDS_UUIDS = new HashSet<>();
    static {
        SYSTEM_CLASSES = Collections.unmodifiableList(Arrays.asList(
                new SystemClass(
                        SystemIds.TwinClass.USER,
                        "USER",
                        List.of(
                                new SystemStatus(SystemIds.TwinStatus.User.INIT, SystemIds.TwinClass.USER, true, new I18n(SystemIds.I18n.UserStatus.NAME, "Active"), new I18n(SystemIds.I18n.UserStatus.DESCRIPTION, "User is active"), StatusType.BASIC)),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.User.EMAIL, SystemIds.TwinClass.USER, FeaturerTwins.ID_1318, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.UserField.EMAIL_NAME, "Email"), new I18n(SystemIds.I18n.UserField.EMAIL_DESCRIPTION, "User email address"), 4101, "email", false, true, true),
                                new SystemField(SystemIds.TwinClassField.User.AVATAR, SystemIds.TwinClass.USER, FeaturerTwins.ID_1319, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.UserField.AVATAR_NAME, "Avatar"), new I18n(SystemIds.I18n.UserField.AVATAR_DESCRIPTION, "User avatar image"), 4101, "avatar", false, true, true)
                        ),
                        false,
                        true
                ),
                new SystemClass(
                        SystemIds.TwinClass.BUSINESS_ACCOUNT,
                        "BUSINESS_ACCOUNT",
                        List.of(
                                new SystemStatus(SystemIds.TwinStatus.BusinessAccount.INIT, SystemIds.TwinClass.BUSINESS_ACCOUNT, true, new I18n(SystemIds.I18n.BusinessAccountStatus.NAME, "Business Account"), new I18n(SystemIds.I18n.BusinessAccountStatus.DESCRIPTION, "Business Account status"), StatusType.BASIC)),
                        List.of(),
                        false,
                        false
                ),
                new SystemClass(
                        SystemIds.TwinClass.GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.Base.NAME, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.NAME_NAME, "Name"), new I18n(SystemIds.I18n.GlobalAncestorField.NAME_DESCRIPTION, "Twin name"), 4107, "base_name", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.DESCRIPTION, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_NAME, "Description"), new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_DESCRIPTION, "Twin description"), 4107, "base_description", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.EXTERNAL_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_NAME, "External ID"), new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_DESCRIPTION, "External identifier"), 4107, "base_external_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.OWNER_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_NAME, "Owner"), new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_DESCRIPTION, "Twin owner"), 4107, "base_owner_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_NAME, "Assignee"), new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_DESCRIPTION, "Assigned user"), 4107, "base_assignee_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.CREATOR_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_NAME, "Creator"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_DESCRIPTION, "User who created the twin"), 4107, "base_creator_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.HEAD_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_NAME, "Head"), new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_DESCRIPTION, "Head twin"), 4107, "base_head", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.STATUS_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_NAME, "Status"), new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_DESCRIPTION, "Twin status"), 4107, "base_status", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.CREATED_AT, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_NAME, "Created At"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_DESCRIPTION, "Creation timestamp"), 4107, "base_created_at", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1327, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ID_NAME, "Id"), new I18n(SystemIds.I18n.GlobalAncestorField.ID_DESCRIPTION, "Twin id"), 4107, "base_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.TWIN_CLASS_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1328, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_NAME, "Twin class id"), new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_DESCRIPTION, "Twin class id"), 4107, "base_twin_class_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ALIASES, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1329, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_NAME, "Aliases"), new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_DESCRIPTION, "Aliases"), 4101, "base_aliases", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.TAGS, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1330, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_NAME, "Tags"), new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_DESCRIPTION, "Tags"), 4101, "base_tags", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.MARKERS, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1331, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_NAME, "Markers"), new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_DESCRIPTION, "Markers"), 4101, "base_markers", false, true, true)
                        ),
                        true,
                        false
                ),
                new SystemClass(
                        SystemIds.TwinClass.FACE_PAGE,
                        "FACE_PAGE",
                        List.of(new SystemStatus(SystemIds.TwinStatus.FacePage.INIT, SystemIds.TwinClass.FACE_PAGE, true, new I18n(SystemIds.I18n.FacePageStatus.NAME, "Published"), new I18n(SystemIds.I18n.FacePageStatus.DESCRIPTION, "Face page published"), StatusType.BASIC)),
                        List.of(),
                        false,
                        true
                ),
                // TWINS_GLOSSARY (TWINS-854): glossary-as-twins. Field name/description i18n left null —
                // field keys are self-describing (same as the former SQL migration behaviour).
                new SystemClass(
                        SystemIds.TwinClass.TWINS_GLOSSARY,
                        "TWINS_GLOSSARY",
                        List.of(
                                new SystemStatus(SystemIds.TwinStatus.Glossary.INIT, SystemIds.TwinClass.TWINS_GLOSSARY, true,
                                        new I18n(SystemIds.I18n.GlossaryStatus.INIT_NAME, "Actual"),
                                        new I18n(SystemIds.I18n.GlossaryStatus.INIT_DESCRIPTION, "Glossary entry is in sync with its markdown source file"),
                                        StatusType.BASIC),
                                new SystemStatus(SystemIds.TwinStatus.Glossary.DELETED, SystemIds.TwinClass.TWINS_GLOSSARY, true,
                                        new I18n(SystemIds.I18n.GlossaryStatus.DELETED_NAME, "Deleted"),
                                        new I18n(SystemIds.I18n.GlossaryStatus.DELETED_DESCRIPTION, "Source markdown file removed; Twin retained for referential integrity"),
                                        StatusType.BASIC)
                        ),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.Glossary.PURPOSE,            SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "purpose",            false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.FIELDS,             SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "fields",             true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.RELATIONS_OVERVIEW, SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "relations_overview", false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.API,                SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "api",                false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.API_DEPRECATED,     SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "api_deprecated",     false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.EXAMPLES,           SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "examples",           false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.DEV_NOTES,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, null, "dev_notes",          false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.JPA_CLASS,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, null, "jpa_class",          false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.DB_TABLE,           SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, null, "db_table",           false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.MARKDOWN_SOURCE,    SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, null, "markdown_source",    true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.MARKDOWN_HASH,      SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, null, "markdown_hash",      true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.IS_SYSTEM,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1306, FeaturerTwins.ID_5301, null, null, null, "is_system",          true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.ACTUALIZED_AT,      SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1302, FeaturerTwins.ID_5301, null, null, null, "actualized_at",      true,  false, false)
                        ),
                        false,
                        false
                )
        ));
        SYSTEM_LINKS = List.of(
                new SystemLink(
                        SystemIds.Link.GLOSSARY_SEE_ALSO,
                        SystemIds.TwinClass.TWINS_GLOSSARY,
                        SystemIds.TwinClass.TWINS_GLOSSARY,
                        new I18n(SystemIds.I18n.GlossaryLink.SEE_ALSO_FORWARD, "See also"),
                        new I18n(SystemIds.I18n.GlossaryLink.SEE_ALSO_BACKWARD, "Referenced by"),
                        LinkType.ManyToMany,
                        LinkStrength.OPTIONAL
                )
        );
        SYSTEM_DATA_LISTS = List.of(
                new SystemDataList(
                        SystemIds.DataList.GLOSSARY_CATEGORY,
                        "GLOSSARY_CATEGORY",
                        List.of(
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_CORE,           "core",           DataListStatus.active, (short) 1),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_WORKFLOW,      "workflow",       DataListStatus.active, (short) 2),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_MULTI_TENANCY, "multi-tenancy",  DataListStatus.active, (short) 3),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_PERMISSIONS,   "permissions",    DataListStatus.active, (short) 4),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_CONTENT,       "content",        DataListStatus.active, (short) 5),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_CROSS_CUTTING, "cross-cutting",  DataListStatus.active, (short) 6),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_FIELDS,        "fields",         DataListStatus.active, (short) 7),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_VALIDATION,    "validation",     DataListStatus.active, (short) 8),
                                new SystemDataListOption(SystemIds.DataListOption.GLOSSARY_CATEGORY_OTHER,         "other",          DataListStatus.active, (short) 9)
                        )
                )
        );
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
                if (field.name() != null) {
                    i18nEntities.add(new I18nEntity()
                            .setId(field.name().i18nId())
                            .setType(I18nType.TWIN_CLASS_FIELD_NAME));
                    i18nTranslationEntities.add(new I18nTranslationEntity()
                            .setI18nId(field.name().i18nId())
                            .setLocale(Locale.ENGLISH)
                            .setTranslation(field.name().translation()));
                }
                if (field.description() != null) {
                    i18nEntities.add(new I18nEntity()
                            .setId(field.description().i18nId())
                            .setType(I18nType.TWIN_CLASS_FIELD_DESCRIPTION));
                    i18nTranslationEntities.add(new I18nTranslationEntity()
                            .setI18nId(field.description().i18nId())
                            .setLocale(Locale.ENGLISH)
                            .setTranslation(field.description().translation()));
                }
                fieldEntities.add(new TwinClassFieldEntity()
                        .setId(field.id())
                        .setTwinClassId(field.twinClassId())
                        .setKey(field.fieldKey())
                        .setNameI18nId(field.name() != null ? field.name().i18nId() : null)
                        .setDescriptionI18nId(field.description() != null ? field.description().i18nId() : null)
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
        // Collect link forward/backward i18n into the same bulk save below.
        for (SystemLink systemLink : SYSTEM_LINKS) {
            if (systemLink.forwardName() != null) {
                i18nEntities.add(new I18nEntity()
                        .setId(systemLink.forwardName().i18nId())
                        .setType(I18nType.LINK_FORWARD_NAME));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(systemLink.forwardName().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(systemLink.forwardName().translation()));
            }
            if (systemLink.backwardName() != null) {
                i18nEntities.add(new I18nEntity()
                        .setId(systemLink.backwardName().i18nId())
                        .setType(I18nType.LINK_BACKWARD_NAME));
                i18nTranslationEntities.add(new I18nTranslationEntity()
                        .setI18nId(systemLink.backwardName().i18nId())
                        .setLocale(Locale.ENGLISH)
                        .setTranslation(systemLink.backwardName().translation()));
            }
        }

        entitySmartService.saveAllAndLog(i18nEntities, i18nRepository);
        entitySmartService.saveAllAndLog(i18nTranslationEntities, i18nTranslationRepository);
        entitySmartService.saveAllAndLog(fieldEntities, twinClassFieldRepository);
        entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);

        // System links (e.g. GLOSSARY_SEE_ALSO) — i18n for forward/backward names was collected above.
        List<LinkEntity> linkEntities = new ArrayList<>();
        for (SystemLink systemLink : SYSTEM_LINKS) {
            linkEntities.add(new LinkEntity()
                    .setId(systemLink.id())
                    .setSrcTwinClassId(systemLink.srcTwinClassId())
                    .setDstTwinClassId(systemLink.dstTwinClassId())
                    .setForwardNameI18NId(systemLink.forwardName() != null ? systemLink.forwardName().i18nId() : null)
                    .setBackwardNameI18NId(systemLink.backwardName() != null ? systemLink.backwardName().i18nId() : null)
                    .setType(systemLink.type())
                    .setLinkStrengthId(systemLink.strength())
                    .setCreatedByUserId(SystemIds.User.SYSTEM));
        }
        entitySmartService.saveAllAndLog(linkEntities, linkRepository);

        // System data lists with their options (e.g. GLOSSARY_CATEGORY)
        List<DataListEntity> dataListEntities = new ArrayList<>();
        List<DataListOptionEntity> dataListOptionEntities = new ArrayList<>();
        for (SystemDataList systemDataList : SYSTEM_DATA_LISTS) {
            dataListEntities.add(new DataListEntity()
                    .setId(systemDataList.id())
                    .setKey(systemDataList.key())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setUpdatedAt(Timestamp.from(Instant.now())));
            for (SystemDataListOption option : systemDataList.options()) {
                dataListOptionEntities.add(new DataListOptionEntity()
                        .setId(option.id())
                        .setDataListId(systemDataList.id())
                        .setOption(option.option())
                        .setStatus(option.status())
                        .setOrder(option.order())
                        .setCreatedAt(Timestamp.from(Instant.now())));
            }
        }
        entitySmartService.saveAllAndLog(dataListEntities, dataListRepository);
        entitySmartService.saveAllAndLog(dataListOptionEntities, dataListOptionRepository);

        TwinEntity twinEntity;
        twinEntity = new TwinEntity()
                .setId(SystemIds.TwinTemplate.USER)
                .setName("User")
                .setTwinClassId(SystemIds.TwinClass.USER)
                .setTwinStatusId(SystemIds.TwinStatus.User.INIT)
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        entitySmartService.save(twinEntity.getId(), twinEntity, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
        twinEntity = new TwinEntity()
                .setId(SystemIds.TwinTemplate.BUSINESS_ACCOUNT)
                .setName("Business account")
                .setTwinClassId(SystemIds.TwinClass.BUSINESS_ACCOUNT)
                .setTwinStatusId(SystemIds.TwinStatus.BusinessAccount.INIT)
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

    public record SystemLink(UUID id, UUID srcTwinClassId, UUID dstTwinClassId,
                             I18n forwardName, I18n backwardName,
                             LinkType type, LinkStrength strength) {
    }

    public record SystemDataList(UUID id, String key, List<SystemDataListOption> options) {
    }

    public record SystemDataListOption(UUID id, String option, DataListStatus status, Short order) {
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
