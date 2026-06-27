package org.twins.bootstrap;

import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Pure-data container for system bootstrap: declarations of all system TwinClasses,
 * TwinStatuses, TwinClassFields, Links and DataLists that {@link SystemEntityBootstrapService}
 * persists at app startup.
 *
 * <p>No persistence logic here — only records, static lists, and the static initializer
 * that assembles them. Keeping data separate from logic lets new system entities be added
 * by editing this file alone.
 */
public final class SystemEntityBootstrapData {
    private SystemEntityBootstrapData() {}

    public static final List<SystemClass> SYSTEM_CLASSES;
    public static final List<SystemLink> SYSTEM_LINKS;
    public static final List<SystemDataList> SYSTEM_DATA_LISTS;

    static {
        SYSTEM_CLASSES = Collections.unmodifiableList(Arrays.asList(
                new SystemClass(
                        SystemIds.TwinClass.USER,
                        "USER",
                        List.of(new SystemStatus(SystemIds.TwinStatus.User.INIT, SystemIds.TwinClass.USER, true, new I18n(SystemIds.I18n.UserStatus.NAME, "Active"), new I18n(SystemIds.I18n.UserStatus.DESCRIPTION, "User is active"), StatusType.BASIC)),
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
                        List.of(new SystemStatus(SystemIds.TwinStatus.BusinessAccount.INIT, SystemIds.TwinClass.BUSINESS_ACCOUNT, true, new I18n(SystemIds.I18n.BusinessAccountStatus.NAME, "Business Account"), new I18n(SystemIds.I18n.BusinessAccountStatus.DESCRIPTION, "Business Account status"), StatusType.BASIC)),
                        List.of(),
                        false,
                        false
                ),
                new SystemClass(
                        SystemIds.TwinClass.GLOBAL_ANCESTOR,
                        "GLOBAL_ANCESTOR",
                        Collections.emptyList(),
                        List.of(
                                new SystemField(SystemIds.TwinClassField.Base.NAME,             SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.NAME_NAME, "Name"), new I18n(SystemIds.I18n.GlobalAncestorField.NAME_DESCRIPTION, "Twin name"), 4107, "base_name", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.DESCRIPTION,      SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_NAME, "Description"), new I18n(SystemIds.I18n.GlobalAncestorField.DESCRIPTION_DESCRIPTION, "Twin description"), 4107, "base_description", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.EXTERNAL_ID,      SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1321, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_NAME, "External ID"), new I18n(SystemIds.I18n.GlobalAncestorField.EXTERNAL_ID_DESCRIPTION, "External identifier"), 4107, "base_external_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.OWNER_USER_ID,    SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_NAME, "Owner"), new I18n(SystemIds.I18n.GlobalAncestorField.OWNER_USER_DESCRIPTION, "Twin owner"), 4107, "base_owner_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID, SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_NAME, "Assignee"), new I18n(SystemIds.I18n.GlobalAncestorField.ASSIGNEE_DESCRIPTION, "Assigned user"), 4107, "base_assignee_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.CREATOR_USER_ID,  SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1322, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_NAME, "Creator"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATOR_DESCRIPTION, "User who created the twin"), 4107, "base_creator_user", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.HEAD_ID,          SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1323, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_NAME, "Head"), new I18n(SystemIds.I18n.GlobalAncestorField.HEAD_DESCRIPTION, "Head twin"), 4107, "base_head", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.STATUS_ID,        SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1324, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_NAME, "Status"), new I18n(SystemIds.I18n.GlobalAncestorField.STATUS_DESCRIPTION, "Twin status"), 4107, "base_status", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.CREATED_AT,       SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1325, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_NAME, "Created At"), new I18n(SystemIds.I18n.GlobalAncestorField.CREATED_AT_DESCRIPTION, "Creation timestamp"), 4107, "base_created_at", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ID,               SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1327, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ID_NAME, "Id"), new I18n(SystemIds.I18n.GlobalAncestorField.ID_DESCRIPTION, "Twin id"), 4107, "base_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.TWIN_CLASS_ID,    SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1328, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_NAME, "Twin class id"), new I18n(SystemIds.I18n.GlobalAncestorField.TWIN_CLASS_ID_DESCRIPTION, "Twin class id"), 4107, "base_twin_class_id", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.ALIASES,          SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1329, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_NAME, "Aliases"), new I18n(SystemIds.I18n.GlobalAncestorField.ALIASES_DESCRIPTION, "Aliases"), 4101, "base_aliases", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.TAGS,             SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1330, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_NAME, "Tags"), new I18n(SystemIds.I18n.GlobalAncestorField.TAGS_DESCRIPTION, "Tags"), 4101, "base_tags", false, true, true),
                                new SystemField(SystemIds.TwinClassField.Base.MARKERS,          SystemIds.TwinClass.GLOBAL_ANCESTOR, FeaturerTwins.ID_1331, FeaturerTwins.ID_5301, new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_NAME, "Markers"), new I18n(SystemIds.I18n.GlobalAncestorField.MARKERS_DESCRIPTION, "Markers"), 4101, "base_markers", false, true, true)
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
}
