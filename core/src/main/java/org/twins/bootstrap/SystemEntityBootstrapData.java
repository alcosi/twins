package org.twins.bootstrap;

import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.permission.Permissions;

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
    public static final List<SystemPermission> SYSTEM_PERMISSIONS;

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
                                new SystemField(SystemIds.TwinClassField.Glossary.PURPOSE,            SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "purpose",            false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.FIELDS,             SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "fields",             true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.RELATIONS_OVERVIEW, SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "relations_overview", false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.API,                SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "api",                false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.API_DEPRECATED,     SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "api_deprecated",     false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.EXAMPLES,           SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "examples",           false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.DEV_NOTES,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1336, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "dev_notes",          false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.JPA_CLASS,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "jpa_class",          false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.DB_TABLE,           SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "db_table",           false, false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.MARKDOWN_SOURCE,    SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "markdown_source",    true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.MARKDOWN_HASH,      SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1301, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "markdown_hash",      true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.IS_SYSTEM,          SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1306, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "is_system",          true,  false, false),
                                new SystemField(SystemIds.TwinClassField.Glossary.ACTUALIZED_AT,      SystemIds.TwinClass.TWINS_GLOSSARY, FeaturerTwins.ID_1302, FeaturerTwins.ID_5301, null, null, FeaturerTwins.ID_4101, "actualized_at",      true,  false, false)
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

    static {
        // System permissions. id/key sourced from SystemIds.Permission.* (single source of truth
        // — Permissions enum reads from there). name/description i18n UUIDs + translations copied
        // verbatim from Flyway migrations (V1.3.180.01 / V1.3.217.02 / V1.3.267.04 / V1.3.276.02 /
        // V1.3.316.02 / V1.3.435.01 / V1.3.448.02 / V1.3.486.01 + V1.4.*). Permissions without
        // i18n in migrations have null name/description here.
        SYSTEM_PERMISSIONS = List.of(
                // ─── 0001 GENERAL ───
                new SystemPermission(SystemIds.Permission.General.DENY_ALL, Permissions.DENY_ALL.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001000"), "Deny all"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001001"), "Deny all")),
                new SystemPermission(SystemIds.Permission.General.SYSTEM_APP_INFO_VIEW, Permissions.SYSTEM_APP_INFO_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001002"), "System app info view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001003"), "System app info view")),
                new SystemPermission(SystemIds.Permission.General.LOG_SUBSTITUTION_VIEW, Permissions.LOG_SUBSTITUTION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001004"), "Log substitution view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001005"), "Log substitution view")),
                new SystemPermission(SystemIds.Permission.General.ACT_AS_USER, Permissions.ACT_AS_USER.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001006"), "Act as user"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001007"), "Give possibility to act as other user")),
                new SystemPermission(SystemIds.Permission.General.SYSTEM_CACHE_EVICT, Permissions.SYSTEM_CACHE_EVICT.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001008"), "System cache evict"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001009"), "System cache evict")),

                // ─── 0002 TWINFLOW ───
                new SystemPermission(SystemIds.Permission.Twinflow.MANAGE, Permissions.TWINFLOW_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100a"), "Twinflow manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100b"), "Twinflow manage")),
                new SystemPermission(SystemIds.Permission.Twinflow.CREATE, Permissions.TWINFLOW_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100c"), "Twinflow create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100d"), "Twinflow create")),
                new SystemPermission(SystemIds.Permission.Twinflow.VIEW, Permissions.TWINFLOW_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100e"), "Twinflow view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000100f"), "Twinflow view")),
                new SystemPermission(SystemIds.Permission.Twinflow.UPDATE, Permissions.TWINFLOW_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001010"), "Twinflow update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001011"), "Twinflow update")),
                new SystemPermission(SystemIds.Permission.Twinflow.DELETE, Permissions.TWINFLOW_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001012"), "Twinflow delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001013"), "Twinflow delete")),

                // ─── 0003 TWINFLOW SCHEMA ───
                new SystemPermission(SystemIds.Permission.TwinflowSchema.MANAGE, Permissions.TWINFLOW_SCHEMA_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001014"), "Twinflow schema manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001015"), "Twinflow schema manage")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.CREATE, Permissions.TWINFLOW_SCHEMA_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001016"), "Twinflow schema create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001017"), "Twinflow schema create")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.VIEW, Permissions.TWINFLOW_SCHEMA_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001018"), "Twinflow schema view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001019"), "Twinflow schema view")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.UPDATE, Permissions.TWINFLOW_SCHEMA_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101a"), "Twinflow schema update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101b"), "Twinflow schema update")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.DELETE, Permissions.TWINFLOW_SCHEMA_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101c"), "Twinflow schema delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101d"), "Twinflow schema delete")),

                // ─── 0004 TWIN CLASS ───
                new SystemPermission(SystemIds.Permission.TwinClass.MANAGE, Permissions.TWIN_CLASS_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101e"), "Class manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000101f"), "Class manage")),
                new SystemPermission(SystemIds.Permission.TwinClass.CREATE, Permissions.TWIN_CLASS_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001020"), "Class create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001021"), "Class create")),
                new SystemPermission(SystemIds.Permission.TwinClass.VIEW, Permissions.TWIN_CLASS_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001022"), "Class view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001023"), "Class view")),
                new SystemPermission(SystemIds.Permission.TwinClass.UPDATE, Permissions.TWIN_CLASS_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001024"), "Class update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001025"), "Class update")),
                new SystemPermission(SystemIds.Permission.TwinClass.DELETE, Permissions.TWIN_CLASS_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001026"), "Class delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001027"), "Class delete")),

                // ─── 0005 TWIN CLASS FIELD ───
                new SystemPermission(SystemIds.Permission.TwinClassField.MANAGE, Permissions.TWIN_CLASS_FIELD_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001028"), "Class field manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001029"), "Class field manage")),
                new SystemPermission(SystemIds.Permission.TwinClassField.CREATE, Permissions.TWIN_CLASS_FIELD_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102a"), "Twin class field create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102b"), "Twin class field create")),
                new SystemPermission(SystemIds.Permission.TwinClassField.VIEW, Permissions.TWIN_CLASS_FIELD_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102c"), "Twin class field view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102d"), "Twin class field view")),
                new SystemPermission(SystemIds.Permission.TwinClassField.UPDATE, Permissions.TWIN_CLASS_FIELD_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102e"), "Twin class field update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000102f"), "Twin class field update")),
                new SystemPermission(SystemIds.Permission.TwinClassField.DELETE, Permissions.TWIN_CLASS_FIELD_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001030"), "Twin class field delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001031"), "Twin class field delete")),

                // ─── 0006 TWIN CLASS CARD ───
                new SystemPermission(SystemIds.Permission.TwinClassCard.MANAGE, Permissions.TWIN_CLASS_CARD_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001032"), "Class card manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001033"), "Class card manage")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.CREATE, Permissions.TWIN_CLASS_CARD_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001034"), "Twin class card create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001035"), "Twin class card create")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.VIEW, Permissions.TWIN_CLASS_CARD_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001036"), "Class card view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001037"), "Class card view")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.UPDATE, Permissions.TWIN_CLASS_CARD_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001038"), "Twin class card update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001039"), "Twin class card update")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.DELETE, Permissions.TWIN_CLASS_CARD_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103a"), "Twin class card delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103b"), "Twin class card delete")),

                // ─── 0007 TRANSITION ───
                new SystemPermission(SystemIds.Permission.Transition.MANAGE, Permissions.TRANSITION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103c"), "Transition manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103d"), "Transition manage")),
                new SystemPermission(SystemIds.Permission.Transition.CREATE, Permissions.TRANSITION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103e"), "Transition create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000103f"), "Transition create")),
                new SystemPermission(SystemIds.Permission.Transition.VIEW, Permissions.TRANSITION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001040"), "Transition view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001041"), "Transition view")),
                new SystemPermission(SystemIds.Permission.Transition.UPDATE, Permissions.TRANSITION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001042"), "Transition update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001043"), "Transition update")),
                new SystemPermission(SystemIds.Permission.Transition.DELETE, Permissions.TRANSITION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001044"), "Transition delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001045"), "Transition delete")),
                new SystemPermission(SystemIds.Permission.Transition.PERFORM, Permissions.TRANSITION_PERFORM.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001046"), "Transition perform"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001047"), "Transition perform")),
                new SystemPermission(SystemIds.Permission.Transition.DRAFT, Permissions.TRANSITION_DRAFT.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001048"), "Transition draft"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001049"), "Transition draft")),

                // ─── 0008 LINK ───
                new SystemPermission(SystemIds.Permission.Link.MANAGE, Permissions.LINK_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104a"), "Link manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104b"), "Link manage")),
                new SystemPermission(SystemIds.Permission.Link.CREATE, Permissions.LINK_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104c"), "Link create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104d"), "Link create")),
                new SystemPermission(SystemIds.Permission.Link.VIEW, Permissions.LINK_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104e"), "Link view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000104f"), "Link view")),
                new SystemPermission(SystemIds.Permission.Link.UPDATE, Permissions.LINK_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001050"), "Link update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001051"), "Link update")),
                new SystemPermission(SystemIds.Permission.Link.DELETE, Permissions.LINK_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001052"), "Link delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001053"), "Link delete")),

                // ─── 0009 DOMAIN ───
                new SystemPermission(SystemIds.Permission.Domain.MANAGE, Permissions.DOMAIN_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001054"), "Domain manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001055"), "domain manage permission")),
                new SystemPermission(SystemIds.Permission.Domain.CREATE, Permissions.DOMAIN_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001056"), "Domain create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001057"), "Domain create")),
                new SystemPermission(SystemIds.Permission.Domain.VIEW, Permissions.DOMAIN_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001058"), "Domain view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001059"), "Domain view")),
                new SystemPermission(SystemIds.Permission.Domain.UPDATE, Permissions.DOMAIN_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105a"), "Domain update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105b"), "Domain update")),
                new SystemPermission(SystemIds.Permission.Domain.DELETE, Permissions.DOMAIN_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105c"), "Domain delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105d"), "Domain delete")),
                new SystemPermission(SystemIds.Permission.Domain.TWINS_VIEW_ALL, Permissions.DOMAIN_TWINS_VIEW_ALL.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105e"), "Domain twins view all"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000105f"), "Domain twins view all")),
                new SystemPermission(SystemIds.Permission.Domain.TWINS_CREATE_ANY, Permissions.DOMAIN_TWINS_CREATE_ANY.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001060"), "Domain twins create any"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001061"), "domain twins create any")),

                // ─── 0010 TWIN STATUS ───
                new SystemPermission(SystemIds.Permission.TwinStatus.MANAGE, Permissions.TWIN_STATUS_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001062"), "Twin status manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001063"), "twin status manage permission")),
                new SystemPermission(SystemIds.Permission.TwinStatus.CREATE, Permissions.TWIN_STATUS_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001064"), "Status create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001065"), "Status create")),
                new SystemPermission(SystemIds.Permission.TwinStatus.VIEW, Permissions.TWIN_STATUS_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001066"), "Status view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001067"), "Status view")),
                new SystemPermission(SystemIds.Permission.TwinStatus.UPDATE, Permissions.TWIN_STATUS_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001068"), "Status update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001069"), "Status update")),
                new SystemPermission(SystemIds.Permission.TwinStatus.DELETE, Permissions.TWIN_STATUS_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106a"), "Status delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106b"), "Status delete")),

                // ─── 0011 TWIN ───
                new SystemPermission(SystemIds.Permission.Twin.MANAGE, Permissions.TWIN_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106c"), "Twin manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106d"), "twin manage permission")),
                new SystemPermission(SystemIds.Permission.Twin.CREATE, Permissions.TWIN_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106e"), "Create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000106f"), "Create")),
                new SystemPermission(SystemIds.Permission.Twin.VIEW, Permissions.TWIN_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001070"), "View"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001071"), "View")),
                new SystemPermission(SystemIds.Permission.Twin.UPDATE, Permissions.TWIN_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001072"), "Update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001073"), "Update")),
                new SystemPermission(SystemIds.Permission.Twin.DELETE, Permissions.TWIN_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001074"), "Delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001075"), "Delete")),
                new SystemPermission(SystemIds.Permission.Twin.SKETCH_CREATE, Permissions.TWIN_SKETCH_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001076"), "Twin sketch create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001077"), "Twinflow create")),

                // ─── 0012 COMMENT ───
                new SystemPermission(SystemIds.Permission.Comment.MANAGE, Permissions.COMMENT_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001078"), "Comment manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001079"), "comment manage permission")),
                new SystemPermission(SystemIds.Permission.Comment.CREATE, Permissions.COMMENT_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107a"), "Comment create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107b"), "Comment create")),
                new SystemPermission(SystemIds.Permission.Comment.VIEW, Permissions.COMMENT_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107c"), "Comment view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107d"), "Comment view")),
                new SystemPermission(SystemIds.Permission.Comment.UPDATE, Permissions.COMMENT_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107e"), "Comment update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000107f"), "Comment update")),
                new SystemPermission(SystemIds.Permission.Comment.DELETE, Permissions.COMMENT_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001080"), "Comment delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001081"), "Comment delete")),

                // ─── 0013 ATTACHMENT ───
                new SystemPermission(SystemIds.Permission.Attachment.MANAGE, Permissions.ATTACHMENT_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001082"), "Attachment manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001083"), "attachment manage permission")),
                new SystemPermission(SystemIds.Permission.Attachment.CREATE, Permissions.ATTACHMENT_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001084"), "Attachment create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001085"), "Attachment create")),
                new SystemPermission(SystemIds.Permission.Attachment.VIEW, Permissions.ATTACHMENT_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001086"), "Attachment view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001087"), "Attachment view")),
                new SystemPermission(SystemIds.Permission.Attachment.UPDATE, Permissions.ATTACHMENT_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001088"), "Attachment update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001089"), "Attachment update")),
                new SystemPermission(SystemIds.Permission.Attachment.DELETE, Permissions.ATTACHMENT_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108a"), "Attachment delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108b"), "Attachment delete")),
                new SystemPermission(SystemIds.Permission.Attachment.VALIDATE, Permissions.ATTACHMENT_VALIDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108c"), "Attachment validate"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108d"), "Attachment validate")),

                // ─── 0014 USER ───
                new SystemPermission(SystemIds.Permission.User.MANAGE, Permissions.USER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108e"), "User manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000108f"), "user manage permission")),
                new SystemPermission(SystemIds.Permission.User.CREATE, Permissions.USER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001090"), "User create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001091"), "User create")),
                new SystemPermission(SystemIds.Permission.User.VIEW, Permissions.USER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001092"), "User view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001093"), "User view")),
                new SystemPermission(SystemIds.Permission.User.UPDATE, Permissions.USER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001094"), "User update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001095"), "User update")),
                new SystemPermission(SystemIds.Permission.User.DELETE, Permissions.USER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001096"), "User delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001097"), "User delete")),

                // ─── 0015 USER GROUP ───
                new SystemPermission(SystemIds.Permission.UserGroup.MANAGE, Permissions.USER_GROUP_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001098"), "User group manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001099"), "user group manage permission")),
                new SystemPermission(SystemIds.Permission.UserGroup.CREATE, Permissions.USER_GROUP_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109a"), "User group create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109b"), "User group create")),
                new SystemPermission(SystemIds.Permission.UserGroup.VIEW, Permissions.USER_GROUP_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109c"), "User group view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109d"), "User group view")),
                new SystemPermission(SystemIds.Permission.UserGroup.UPDATE, Permissions.USER_GROUP_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109e"), "User group update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000109f"), "User group update")),
                new SystemPermission(SystemIds.Permission.UserGroup.DELETE, Permissions.USER_GROUP_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a0"), "User group delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a1"), "User group delete")),

                // ─── 0016 DATA LIST ───
                new SystemPermission(SystemIds.Permission.DataList.MANAGE, Permissions.DATA_LIST_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a2"), "Data list manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a3"), "data list manage permission")),
                new SystemPermission(SystemIds.Permission.DataList.CREATE, Permissions.DATA_LIST_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a4"), "Data list create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a5"), "Data list create")),
                new SystemPermission(SystemIds.Permission.DataList.VIEW, Permissions.DATA_LIST_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a6"), "Data list view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a7"), "Data list view")),
                new SystemPermission(SystemIds.Permission.DataList.UPDATE, Permissions.DATA_LIST_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a8"), "Data list update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010a9"), "Data list update")),
                new SystemPermission(SystemIds.Permission.DataList.DELETE, Permissions.DATA_LIST_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010aa"), "Data list delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ab"), "Data list delete")),

                // ─── 0017 DATA LIST OPTION ───
                new SystemPermission(SystemIds.Permission.DataListOption.MANAGE, Permissions.DATA_LIST_OPTION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ac"), "Data list option manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ad"), "data list option manage permission")),
                new SystemPermission(SystemIds.Permission.DataListOption.CREATE, Permissions.DATA_LIST_OPTION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ae"), "Data list option create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010af"), "Data list option create")),
                new SystemPermission(SystemIds.Permission.DataListOption.VIEW, Permissions.DATA_LIST_OPTION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b0"), "Data list option view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b1"), "Data list option view")),
                new SystemPermission(SystemIds.Permission.DataListOption.UPDATE, Permissions.DATA_LIST_OPTION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b2"), "Data list option update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b3"), "Data list option update")),
                new SystemPermission(SystemIds.Permission.DataListOption.DELETE, Permissions.DATA_LIST_OPTION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b4"), "Data list option delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b5"), "Data list option delete")),

                // ─── 0018 DATA LIST SUBSET ───
                new SystemPermission(SystemIds.Permission.DataListSubset.MANAGE, Permissions.DATA_LIST_SUBSET_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b6"), "Data list subset manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b7"), "data list subset manage permission")),
                new SystemPermission(SystemIds.Permission.DataListSubset.CREATE, Permissions.DATA_LIST_SUBSET_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b8"), "Data list subset create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010b9"), "Data list subset create")),
                new SystemPermission(SystemIds.Permission.DataListSubset.VIEW, Permissions.DATA_LIST_SUBSET_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ba"), "Data list subset view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010bb"), "Data list subset view")),
                new SystemPermission(SystemIds.Permission.DataListSubset.UPDATE, Permissions.DATA_LIST_SUBSET_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010bc"), "Data list subset update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010bd"), "Data list subset update")),
                new SystemPermission(SystemIds.Permission.DataListSubset.DELETE, Permissions.DATA_LIST_SUBSET_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010be"), "Data list subset delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010bf"), "Data list subset delete")),

                // ─── 0019 PERMISSION ───
                new SystemPermission(SystemIds.Permission.PermissionEntity.MANAGE, Permissions.PERMISSION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c0"), "Permission manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c1"), "permission manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionEntity.CREATE, Permissions.PERMISSION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c2"), "Permission create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c3"), "Permission create")),
                new SystemPermission(SystemIds.Permission.PermissionEntity.VIEW, Permissions.PERMISSION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c4"), "Permission view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c5"), "Permission view")),
                new SystemPermission(SystemIds.Permission.PermissionEntity.UPDATE, Permissions.PERMISSION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c6"), "Permission update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c7"), "Permission update")),
                new SystemPermission(SystemIds.Permission.PermissionEntity.DELETE, Permissions.PERMISSION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c8"), "Permission delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010c9"), "Permission delete")),

                // ─── 0020 USER GROUP INVOLVE ASSIGNEE ───
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.MANAGE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ca"), "User group involve assignee manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010cb"), "User group involve assignee manage")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.CREATE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010cc"), "User group involve assignee create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010cd"), "User group involve assignee create")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.VIEW, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ce"), "User group involve assignee view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010cf"), "User group involve assignee view")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.UPDATE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d0"), "User group involve assignee update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d1"), "User group involve assignee update")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.DELETE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d2"), "User group involve assignee delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d3"), "User group involve assignee delete")),

                // ─── 0021 PERMISSION GRANT SPACE ROLE ───
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.MANAGE, Permissions.PERMISSION_GRANT_SPACE_ROLE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d4"), "Permission grant space role manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d5"), "Permission grant space role manage")),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.CREATE, Permissions.PERMISSION_GRANT_SPACE_ROLE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d6"), "Permission grant space role create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d7"), "Permission grant space role create")),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.VIEW, Permissions.PERMISSION_GRANT_SPACE_ROLE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d8"), "Permission grant space role view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010d9"), "Permission grant space role view")),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.UPDATE, Permissions.PERMISSION_GRANT_SPACE_ROLE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010da"), "Permission grant space role update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010db"), "Permission grant space role update")),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.DELETE, Permissions.PERMISSION_GRANT_SPACE_ROLE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010dc"), "Permission grant space role delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010dd"), "Permission grant space role delete")),

                // ─── 0022 PERMISSION GRANT TWIN ROLE ───
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.MANAGE, Permissions.PERMISSION_GRANT_TWIN_ROLE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010de"), "Permission grant twin role manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010df"), "Permission grant twin role manage")),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.CREATE, Permissions.PERMISSION_GRANT_TWIN_ROLE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e0"), "Permission grant twin role create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e1"), "Permission grant twin role create")),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.VIEW, Permissions.PERMISSION_GRANT_TWIN_ROLE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e2"), "Permission grant twin role view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e3"), "Permission grant twin role view")),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.UPDATE, Permissions.PERMISSION_GRANT_TWIN_ROLE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e4"), "Permission grant twin role update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e5"), "Permission grant twin role update")),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.DELETE, Permissions.PERMISSION_GRANT_TWIN_ROLE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e6"), "Permission grant twin role delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e7"), "Permission grant twin role delete")),

                // ─── 0023 PERMISSION GRANT USER ───
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.MANAGE, Permissions.PERMISSION_GRANT_USER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e8"), "Permission grant user manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010e9"), "Permission grant user manage")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.CREATE, Permissions.PERMISSION_GRANT_USER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ea"), "Permission grant user create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010eb"), "Permission grant user create")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.VIEW, Permissions.PERMISSION_GRANT_USER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ec"), "Permission grant user view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ed"), "Permission grant user view")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.UPDATE, Permissions.PERMISSION_GRANT_USER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ee"), "Permission grant user update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ef"), "Permission grant user update")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.DELETE, Permissions.PERMISSION_GRANT_USER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f0"), "Permission grant user delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f1"), "Permission grant user delete")),

                // ─── 0024 PERMISSION GRANT USER GROUP ───
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.MANAGE, Permissions.PERMISSION_GRANT_USER_GROUP_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f2"), "Permission grant user group manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f3"), "Permission grant user group manage")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.CREATE, Permissions.PERMISSION_GRANT_USER_GROUP_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f4"), "Permission grant user group create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f5"), "Permission grant user group create")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.VIEW, Permissions.PERMISSION_GRANT_USER_GROUP_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f6"), "Permission grant user group view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f7"), "Permission grant user group view")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.UPDATE, Permissions.PERMISSION_GRANT_USER_GROUP_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f8"), "Permission grant user group update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010f9"), "Permission grant user group update")),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.DELETE, Permissions.PERMISSION_GRANT_USER_GROUP_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010fa"), "Permission grant user group delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010fb"), "Permission grant user group delete")),

                // ─── 0025 PERMISSION GROUP ───
                new SystemPermission(SystemIds.Permission.PermissionGroup.MANAGE, Permissions.PERMISSION_GROUP_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010fc"), "Permission group manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010fd"), "permission group manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.CREATE, Permissions.PERMISSION_GROUP_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010fe"), "Permission Group create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000010ff"), "Permission Group create")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.VIEW, Permissions.PERMISSION_GROUP_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001100"), "Permission group view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001101"), "Permission group view")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.UPDATE, Permissions.PERMISSION_GROUP_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001102"), "Permission Group update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001103"), "Permission Group update")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.DELETE, Permissions.PERMISSION_GROUP_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001104"), "Permission Group delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001105"), "Permission Group delete")),

                // ─── 0026 PERMISSION SCHEMA ───
                new SystemPermission(SystemIds.Permission.PermissionSchema.MANAGE, Permissions.PERMISSION_SCHEMA_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001106"), "Permission schema manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001107"), "permission schema manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.CREATE, Permissions.PERMISSION_SCHEMA_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001108"), "Schema create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001109"), "Schema create")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.VIEW, Permissions.PERMISSION_SCHEMA_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110a"), "Permission schema view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110b"), "Permission schema view")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.UPDATE, Permissions.PERMISSION_SCHEMA_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110c"), "Schema update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110d"), "Schema update")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.DELETE, Permissions.PERMISSION_SCHEMA_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110e"), "Schema delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000110f"), "Schema delete")),

                // ─── 0027 USER PERMISSION ───
                new SystemPermission(SystemIds.Permission.UserPermission.MANAGE, Permissions.USER_PERMISSION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001110"), "User Permission manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001111"), "User Permission manage")),
                new SystemPermission(SystemIds.Permission.UserPermission.CREATE, Permissions.USER_PERMISSION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001112"), "User Permission create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001113"), "User Permission create")),
                new SystemPermission(SystemIds.Permission.UserPermission.VIEW, Permissions.USER_PERMISSION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001114"), "User permission view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001115"), "User permission view")),
                new SystemPermission(SystemIds.Permission.UserPermission.UPDATE, Permissions.USER_PERMISSION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001116"), "User Permission update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001117"), "User Permission update")),
                new SystemPermission(SystemIds.Permission.UserPermission.DELETE, Permissions.USER_PERMISSION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001118"), "User Permission delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001119"), "User Permission delete")),

                // ─── 0028 I18N ───
                new SystemPermission(SystemIds.Permission.I18n.MANAGE, Permissions.I18N_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111a"), "I18n manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111b"), "I18n manage")),
                new SystemPermission(SystemIds.Permission.I18n.CREATE, Permissions.I18N_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111c"), "I18n create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111d"), "I18n create")),
                new SystemPermission(SystemIds.Permission.I18n.VIEW, Permissions.I18N_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111e"), "I18n view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000111f"), "I18n view")),
                new SystemPermission(SystemIds.Permission.I18n.UPDATE, Permissions.I18N_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001120"), "I18n update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001121"), "I18n update")),
                new SystemPermission(SystemIds.Permission.I18n.DELETE, Permissions.I18N_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001122"), "I18n delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001123"), "I18n delete")),

                // ─── 0029 FACTORY ERASER ───
                new SystemPermission(SystemIds.Permission.FactoryEraser.MANAGE, Permissions.FACTORY_ERASER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001124"), "Eraser manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001125"), "eraser manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryEraser.CREATE, Permissions.FACTORY_ERASER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001126"), "Factory eraser create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001127"), "Factory eraser create")),
                new SystemPermission(SystemIds.Permission.FactoryEraser.VIEW, Permissions.FACTORY_ERASER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001128"), "Factory eraser view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001129"), "Factory eraser view")),
                new SystemPermission(SystemIds.Permission.FactoryEraser.UPDATE, Permissions.FACTORY_ERASER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112a"), "Factory eraser update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112b"), "Factory eraser update")),
                new SystemPermission(SystemIds.Permission.FactoryEraser.DELETE, Permissions.FACTORY_ERASER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112c"), "Factory eraser delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112d"), "Factory eraser delete")),

                // ─── 0030 FACTORY ───
                new SystemPermission(SystemIds.Permission.Factory.MANAGE, Permissions.FACTORY_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112e"), "Factory manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000112f"), "factory manage permission")),
                new SystemPermission(SystemIds.Permission.Factory.CREATE, Permissions.FACTORY_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001130"), "Factory create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001131"), "Factory create")),
                new SystemPermission(SystemIds.Permission.Factory.VIEW, Permissions.FACTORY_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001132"), "Factory view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001133"), "Factory view")),
                new SystemPermission(SystemIds.Permission.Factory.UPDATE, Permissions.FACTORY_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001134"), "Factory update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001135"), "Factory update")),
                new SystemPermission(SystemIds.Permission.Factory.DELETE, Permissions.FACTORY_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001136"), "Factory delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001137"), "Factory delete")),

                // ─── 0031 FACTORY MULTIPLIER ───
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.MANAGE, Permissions.FACTORY_MULTIPLIER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001138"), "multiplier manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001139"), "multiplier manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.CREATE, Permissions.FACTORY_MULTIPLIER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113a"), "Factory multiplier create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113b"), "Factory multiplier create")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.VIEW, Permissions.FACTORY_MULTIPLIER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113c"), "Factory multiplier view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113d"), "Factory multiplier view")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.UPDATE, Permissions.FACTORY_MULTIPLIER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113e"), "Factory multiplier update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000113f"), "Factory multiplier update")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.DELETE, Permissions.FACTORY_MULTIPLIER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001140"), "Factory multiplier delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001141"), "Factory multiplier delete")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.PARAM_MANAGE, Permissions.FACTORY_MULTIPLIER_PARAM_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001142"), "Multiplier param manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001143"), "multiplier param manage permission")),

                // ─── 0032 FACTORY PIPELINE ───
                new SystemPermission(SystemIds.Permission.FactoryPipeline.MANAGE, Permissions.FACTORY_PIPELINE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001144"), "Pipeline manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001145"), "pipeline manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.CREATE, Permissions.FACTORY_PIPELINE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001146"), "Factory pipeline create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001147"), "Factory pipeline create")),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.VIEW, Permissions.FACTORY_PIPELINE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001148"), "Factory pipeline view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001149"), "Factory pipeline view")),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.UPDATE, Permissions.FACTORY_PIPELINE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114a"), "Factory pipeline update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114b"), "Factory pipeline update")),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.DELETE, Permissions.FACTORY_PIPELINE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114c"), "Factory pipeline delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114d"), "Factory pipeline delete")),

                // ─── 0033 FACTORY CONDITION SET ───
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.MANAGE, Permissions.FACTORY_CONDITION_SET_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114e"), "Condition set manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000114f"), "condition set manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.CREATE, Permissions.FACTORY_CONDITION_SET_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001150"), "Factory condition set create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001151"), "Factory condition set create")),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.VIEW, Permissions.FACTORY_CONDITION_SET_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001152"), "Factory condition set view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001153"), "Factory condition set view")),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.UPDATE, Permissions.FACTORY_CONDITION_SET_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001154"), "Factory condition set update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001155"), "Factory condition set update")),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.DELETE, Permissions.FACTORY_CONDITION_SET_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001156"), "Factory condition set delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001157"), "Factory condition set delete")),

                // ─── 0034 FACTORY BRANCH ───
                new SystemPermission(SystemIds.Permission.FactoryBranch.MANAGE, Permissions.FACTORY_BRANCH_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001158"), "Branch manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001159"), "branch manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryBranch.CREATE, Permissions.FACTORY_BRANCH_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115a"), "Factory branch create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115b"), "Factory branch create")),
                new SystemPermission(SystemIds.Permission.FactoryBranch.VIEW, Permissions.FACTORY_BRANCH_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115c"), "Factory branch view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115d"), "Factory branch view")),
                new SystemPermission(SystemIds.Permission.FactoryBranch.UPDATE, Permissions.FACTORY_BRANCH_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115e"), "Factory branch update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000115f"), "Factory branch update")),
                new SystemPermission(SystemIds.Permission.FactoryBranch.DELETE, Permissions.FACTORY_BRANCH_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001160"), "Factory branch delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001161"), "Factory branch delete")),

                // ─── 0035 DRAFT ───
                new SystemPermission(SystemIds.Permission.Draft.MANAGE, Permissions.DRAFT_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001162"), "Draft manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001163"), "Draft manage")),
                new SystemPermission(SystemIds.Permission.Draft.CREATE, Permissions.DRAFT_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001164"), "Draft create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001165"), "Draft create")),
                new SystemPermission(SystemIds.Permission.Draft.VIEW, Permissions.DRAFT_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001166"), "Draft view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001167"), "Draft view")),
                new SystemPermission(SystemIds.Permission.Draft.UPDATE, Permissions.DRAFT_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001168"), "Draft update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001169"), "Draft update")),
                new SystemPermission(SystemIds.Permission.Draft.DELETE, Permissions.DRAFT_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116a"), "Draft delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116b"), "Draft delete")),
                new SystemPermission(SystemIds.Permission.Draft.COMMIT, Permissions.DRAFT_COMMIT.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116c"), "Draft commit"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116d"), "Draft commit")),

                // ─── 0036 DOMAIN BUSINESS ACCOUNT ───
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.MANAGE, Permissions.DOMAIN_BUSINESS_ACCOUNT_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116e"), "Domain business account manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000116f"), "Domain business account manage")),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.CREATE, Permissions.DOMAIN_BUSINESS_ACCOUNT_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001170"), "Domain business account create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001171"), "Domain business account create")),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.VIEW, Permissions.DOMAIN_BUSINESS_ACCOUNT_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001172"), "Domain business account view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001173"), "Domain business account view")),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.UPDATE, Permissions.DOMAIN_BUSINESS_ACCOUNT_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001174"), "Domain business account update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001175"), "Domain business account update")),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.DELETE, Permissions.DOMAIN_BUSINESS_ACCOUNT_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001176"), "Domain business account delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001177"), "Domain business account delete")),

                // ─── 0037 DOMAIN USER ───
                new SystemPermission(SystemIds.Permission.DomainUser.MANAGE, Permissions.DOMAIN_USER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001178"), "Domain user manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001179"), "Domain user manage")),
                new SystemPermission(SystemIds.Permission.DomainUser.CREATE, Permissions.DOMAIN_USER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117a"), "Domain user create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117b"), "Domain user create")),
                new SystemPermission(SystemIds.Permission.DomainUser.VIEW, Permissions.DOMAIN_USER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117c"), "Domain user view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117d"), "Domain user view")),
                new SystemPermission(SystemIds.Permission.DomainUser.UPDATE, Permissions.DOMAIN_USER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117e"), "Domain user update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000117f"), "Domain user update")),
                new SystemPermission(SystemIds.Permission.DomainUser.DELETE, Permissions.DOMAIN_USER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001180"), "Domain user delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001181"), "Domain user delete")),

                // ─── 0038 BUSINESS ACCOUNT ───
                new SystemPermission(SystemIds.Permission.BusinessAccount.MANAGE, Permissions.BUSINESS_ACCOUNT_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001182"), "Business account manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001183"), "business account manage permission")),
                new SystemPermission(SystemIds.Permission.BusinessAccount.CREATE, Permissions.BUSINESS_ACCOUNT_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001184"), "Business account create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001185"), "Business account create")),
                new SystemPermission(SystemIds.Permission.BusinessAccount.VIEW, Permissions.BUSINESS_ACCOUNT_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001186"), "Business account view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001187"), "Business account view")),
                new SystemPermission(SystemIds.Permission.BusinessAccount.UPDATE, Permissions.BUSINESS_ACCOUNT_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001188"), "Business account update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001189"), "Business account update")),
                new SystemPermission(SystemIds.Permission.BusinessAccount.DELETE, Permissions.BUSINESS_ACCOUNT_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118a"), "Business account delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118b"), "Business account delete")),

                // ─── 0039 SPACE ROLE ───
                new SystemPermission(SystemIds.Permission.SpaceRole.MANAGE, Permissions.SPACE_ROLE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118c"), "Space role manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118d"), "Space role manage")),
                new SystemPermission(SystemIds.Permission.SpaceRole.CREATE, Permissions.SPACE_ROLE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118e"), "Space role create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000118f"), "Space role create")),
                new SystemPermission(SystemIds.Permission.SpaceRole.VIEW, Permissions.SPACE_ROLE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001190"), "Space role view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001191"), "Space role view")),
                new SystemPermission(SystemIds.Permission.SpaceRole.UPDATE, Permissions.SPACE_ROLE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001192"), "Space role update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001193"), "Space role update")),
                new SystemPermission(SystemIds.Permission.SpaceRole.DELETE, Permissions.SPACE_ROLE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001194"), "Space role delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001195"), "Space role delete")),

                // ─── 0040 FEATURER ───
                new SystemPermission(SystemIds.Permission.Featurer.MANAGE, Permissions.FEATURER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001196"), "Featurer manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001197"), "featurer manage permission")),
                new SystemPermission(SystemIds.Permission.Featurer.CREATE, Permissions.FEATURER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001198"), "Featurer create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001199"), "Featurer create")),
                new SystemPermission(SystemIds.Permission.Featurer.VIEW, Permissions.FEATURER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119a"), "Featurer view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119b"), "Featurer view")),
                new SystemPermission(SystemIds.Permission.Featurer.UPDATE, Permissions.FEATURER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119c"), "Featurer update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119d"), "Featurer update")),
                new SystemPermission(SystemIds.Permission.Featurer.DELETE, Permissions.FEATURER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119e"), "Featurer delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000119f"), "Featurer delete")),

                // ─── 0041 TIER ───
                new SystemPermission(SystemIds.Permission.Tier.MANAGE, Permissions.TIER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a0"), "Tier manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a1"), "tier manage permission")),
                new SystemPermission(SystemIds.Permission.Tier.CREATE, Permissions.TIER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a2"), "Tier create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a3"), "Tier create")),
                new SystemPermission(SystemIds.Permission.Tier.VIEW, Permissions.TIER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a4"), "Tier view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a5"), "Tier view")),
                new SystemPermission(SystemIds.Permission.Tier.UPDATE, Permissions.TIER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a6"), "Tier update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a7"), "Tier update")),
                new SystemPermission(SystemIds.Permission.Tier.DELETE, Permissions.TIER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a8"), "Tier delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011a9"), "Tier delete")),

                // ─── 0042 FACE ───
                new SystemPermission(SystemIds.Permission.Face.MANAGE, Permissions.FACE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011aa"), "Face manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ab"), "Face manage")),
                new SystemPermission(SystemIds.Permission.Face.CREATE, Permissions.FACE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ac"), "Face create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ad"), "Face create")),
                new SystemPermission(SystemIds.Permission.Face.VIEW, Permissions.FACE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ae"), "Face view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011af"), "Face view")),
                new SystemPermission(SystemIds.Permission.Face.UPDATE, Permissions.FACE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b0"), "Face update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b1"), "Face update")),
                new SystemPermission(SystemIds.Permission.Face.DELETE, Permissions.FACE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b2"), "Face delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b3"), "Face delete")),

                // ─── 0043 FACTORY PIPELINE STEP ───
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.MANAGE, Permissions.FACTORY_PIPELINE_STEP_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b4"), "Pipeline step manage permission"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b5"), "pipeline step manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.CREATE, Permissions.FACTORY_PIPELINE_STEP_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b6"), "Factory pipeline step create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b7"), "Factory pipeline step create")),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.VIEW, Permissions.FACTORY_PIPELINE_STEP_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b8"), "Factory pipeline step view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011b9"), "Factory pipeline step view")),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.UPDATE, Permissions.FACTORY_PIPELINE_STEP_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ba"), "Factory pipeline step update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011bb"), "Factory pipeline step update")),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.DELETE, Permissions.FACTORY_PIPELINE_STEP_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011bc"), "Factory pipeline step delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011bd"), "Factory pipeline step delete")),

                // ─── 0044 HISTORY ───
                new SystemPermission(SystemIds.Permission.History.MANAGE, Permissions.HISTORY_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011be"), "History manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011bf"), "History manage")),
                new SystemPermission(SystemIds.Permission.History.CREATE, Permissions.HISTORY_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c0"), "History create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c1"), "History create")),
                new SystemPermission(SystemIds.Permission.History.VIEW, Permissions.HISTORY_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c2"), "History view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c3"), "History view")),
                new SystemPermission(SystemIds.Permission.History.UPDATE, Permissions.HISTORY_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c4"), "History update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c5"), "History update")),
                new SystemPermission(SystemIds.Permission.History.DELETE, Permissions.HISTORY_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c6"), "History delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c7"), "History delete")),
                new SystemPermission(SystemIds.Permission.History.MACHINE_USER_VIEW, Permissions.HISTORY_MACHINE_USER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c8"), "History machine user view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011c9"), "History machine user view")),

                // ─── 0045 PROJECTION ───
                new SystemPermission(SystemIds.Permission.Projection.MANAGE, Permissions.PROJECTION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ca"), "Projection manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011cb"), "Projection manage")),
                new SystemPermission(SystemIds.Permission.Projection.CREATE, Permissions.PROJECTION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011cc"), "Projection create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011cd"), "Projection create")),
                new SystemPermission(SystemIds.Permission.Projection.VIEW, Permissions.PROJECTION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ce"), "Projection view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011cf"), "Projection view")),
                new SystemPermission(SystemIds.Permission.Projection.UPDATE, Permissions.PROJECTION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d0"), "Projection update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d1"), "Projection update")),
                new SystemPermission(SystemIds.Permission.Projection.DELETE, Permissions.PROJECTION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d2"), "Projection delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d3"), "Projection delete")),

                // ─── 0046 PROJECTION EXCLUSION ───
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.MANAGE, Permissions.PROJECTION_EXCLUSION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d4"), "Projection exclusion manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d5"), "Projection exclusion manage")),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.CREATE, Permissions.PROJECTION_EXCLUSION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d6"), "Projection exclusion create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d7"), "Projection exclusion create")),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.VIEW, Permissions.PROJECTION_EXCLUSION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d8"), "Projection exclusion view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011d9"), "Projection exclusion view")),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.UPDATE, Permissions.PROJECTION_EXCLUSION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011da"), "Projection exclusion update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011db"), "Projection exclusion update")),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.DELETE, Permissions.PROJECTION_EXCLUSION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011dc"), "Projection exclusion delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011dd"), "Projection exclusion delete")),

                // ─── 0047 TWIN CLASS FIELD RULE ───
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011de"), "Class field rule manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011df"), "Class field rule manage")),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.CREATE, Permissions.TWIN_CLASS_FIELD_RULE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e0"), "Class field rule create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e1"), "Class field rule create")),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.VIEW, Permissions.TWIN_CLASS_FIELD_RULE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e2"), "Class field rule view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e3"), "Class field rule view")),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.UPDATE, Permissions.TWIN_CLASS_FIELD_RULE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e4"), "Class field rule update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e5"), "Class field rule update")),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.DELETE, Permissions.TWIN_CLASS_FIELD_RULE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e6"), "Class field rule delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e7"), "Class field rule delete")),

                // ─── 0048 TWINFLOW FACTORY ───
                new SystemPermission(SystemIds.Permission.TwinflowFactory.MANAGE, Permissions.TWINFLOW_FACTORY_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e8"), "Twinflow factory manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011e9"), "Twinflow factory manage")),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.CREATE, Permissions.TWINFLOW_FACTORY_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ea"), "Twinflow factory create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011eb"), "Twinflow factory create")),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.VIEW, Permissions.TWINFLOW_FACTORY_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ec"), "Twinflow factory view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ed"), "Twinflow factory view")),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.UPDATE, Permissions.TWINFLOW_FACTORY_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ee"), "Twinflow factory update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ef"), "Twinflow factory update")),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.DELETE, Permissions.TWINFLOW_FACTORY_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f0"), "Twinflow factory delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f1"), "Twinflow factory delete")),

                // ─── 0049 TWIN CLASS FREEZE ───
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.MANAGE, Permissions.TWIN_CLASS_FREEZE_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f2"), "Class freeze manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f3"), "Class freeze manage")),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.CREATE, Permissions.TWIN_CLASS_FREEZE_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f4"), "Class freeze create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f5"), "Class freeze create")),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.VIEW, Permissions.TWIN_CLASS_FREEZE_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f6"), "Class freeze view"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f7"), "Class freeze view")),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.UPDATE, Permissions.TWIN_CLASS_FREEZE_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f8"), "Class freeze update"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011f9"), "Class freeze update")),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.DELETE, Permissions.TWIN_CLASS_FREEZE_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011fa"), "Class freeze delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011fb"), "Class freeze delete")),

                // ─── 0050 HISTORY NOTIFICATION ───
                new SystemPermission(SystemIds.Permission.HistoryNotification.MANAGE, Permissions.HISTORY_NOTIFICATION_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011fc"), "History notification manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011fd"), "History notification manage")),
                new SystemPermission(SystemIds.Permission.HistoryNotification.CREATE, Permissions.HISTORY_NOTIFICATION_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011fe"), "History notification create"), new I18n(UUID.fromString("00000000-0000-0000-0012-0000000011ff"), "History notification create")),
                new SystemPermission(SystemIds.Permission.HistoryNotification.VIEW, Permissions.HISTORY_NOTIFICATION_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001200"), "History notification view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001201"), "History notification view")),
                new SystemPermission(SystemIds.Permission.HistoryNotification.UPDATE, Permissions.HISTORY_NOTIFICATION_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001202"), "History notification update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001203"), "History notification update")),
                new SystemPermission(SystemIds.Permission.HistoryNotification.DELETE, Permissions.HISTORY_NOTIFICATION_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001204"), "History notification delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001205"), "History notification delete")),

                // ─── 0051 SCHEDULER ───
                new SystemPermission(SystemIds.Permission.Scheduler.MANAGE, Permissions.SCHEDULER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001206"), "Scheduler manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001207"), "Scheduler manage")),
                new SystemPermission(SystemIds.Permission.Scheduler.CREATE, Permissions.SCHEDULER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001208"), "Scheduler create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001209"), "Scheduler create")),
                new SystemPermission(SystemIds.Permission.Scheduler.VIEW, Permissions.SCHEDULER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120a"), "Scheduler view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120b"), "Scheduler view")),
                new SystemPermission(SystemIds.Permission.Scheduler.UPDATE, Permissions.SCHEDULER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120c"), "Scheduler update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120d"), "Scheduler update")),
                new SystemPermission(SystemIds.Permission.Scheduler.DELETE, Permissions.SCHEDULER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120e"), "Scheduler delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000120f"), "Scheduler delete")),

                // ─── 0052 TWIN CLASS DYNAMIC MARKER ───
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.MANAGE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001210"), "Class dynamic marker manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001211"), "Class dynamic marker manage")),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.CREATE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001212"), "Class dynamic marker create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001213"), "Class dynamic marker create")),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.VIEW, Permissions.TWIN_CLASS_DYNAMIC_MARKER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001214"), "Class dynamic marker view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001215"), "Class dynamic marker view")),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.UPDATE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001216"), "Class dynamic marker update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001217"), "Class dynamic marker update")),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.DELETE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001218"), "Class dynamic marker delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001219"), "Class dynamic marker delete")),

                // ─── 0053 TWIN VALIDATOR SET ───
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.MANAGE, Permissions.TWIN_VALIDATOR_SET_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121a"), "Validator set manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121b"), "Validator set manage")),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.CREATE, Permissions.TWIN_VALIDATOR_SET_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121c"), "Validator set create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121d"), "Validator set create")),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.VIEW, Permissions.TWIN_VALIDATOR_SET_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121e"), "Validator set view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000121f"), "Validator set view")),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.UPDATE, Permissions.TWIN_VALIDATOR_SET_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001220"), "Validator set update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001221"), "Validator set update")),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.DELETE, Permissions.TWIN_VALIDATOR_SET_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001222"), "Validator set delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001223"), "Validator set delete")),

                // ─── 0054 TWIN TRIGGER ───
                new SystemPermission(SystemIds.Permission.TwinTrigger.MANAGE, Permissions.TWIN_TRIGGER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001224"), "Trigger manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001225"), "Trigger manage")),
                new SystemPermission(SystemIds.Permission.TwinTrigger.CREATE, Permissions.TWIN_TRIGGER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001226"), "Trigger create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001227"), "Trigger create")),
                new SystemPermission(SystemIds.Permission.TwinTrigger.VIEW, Permissions.TWIN_TRIGGER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001228"), "Trigger view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001229"), "Trigger view")),
                new SystemPermission(SystemIds.Permission.TwinTrigger.UPDATE, Permissions.TWIN_TRIGGER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122a"), "Trigger update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122b"), "Trigger update")),
                new SystemPermission(SystemIds.Permission.TwinTrigger.DELETE, Permissions.TWIN_TRIGGER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122c"), "Trigger delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122d"), "Trigger delete")),

                // ─── 0055 USER GROUP INVOLVE ACT AS USER ───
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.MANAGE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122e"), "User group involve act as user manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000122f"), "User group involve act as user manage")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.CREATE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001230"), "User group involve act as user create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001231"), "User group involve act as user create")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.VIEW, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001232"), "User group involve act as user view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001233"), "User group involve act as user view")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.UPDATE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001234"), "User group involve act as user update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001235"), "User group involve act as user update")),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.DELETE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001236"), "User group involve act as user delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001237"), "User group involve act as user delete")),

                // ─── 0056 ACTION RESTRICTION REASON ───
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.MANAGE, Permissions.ACTION_RESTRICTION_REASON_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001238"), "Action restriction reason manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001239"), "Action restriction reason manage")),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.CREATE, Permissions.ACTION_RESTRICTION_REASON_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123a"), "Action restriction reason create"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123b"), "Action restriction reason create")),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.VIEW, Permissions.ACTION_RESTRICTION_REASON_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123c"), "Action restriction reason view"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123d"), "Action restriction reason view")),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.UPDATE, Permissions.ACTION_RESTRICTION_REASON_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123e"), "Action restriction reason update"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000123f"), "Action restriction reason update")),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.DELETE, Permissions.ACTION_RESTRICTION_REASON_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001240"), "Action restriction reason delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001241"), "Action restriction reason delete")),

                // ─── 0057 NOTIFICATION SCHEMA ───
                new SystemPermission(SystemIds.Permission.NotificationSchema.MANAGE, Permissions.NOTIFICATION_SCHEMA_MANAGE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001242"), "Notification schema manage"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001243"), "Notification schema manage")),
                new SystemPermission(SystemIds.Permission.NotificationSchema.CREATE, Permissions.NOTIFICATION_SCHEMA_CREATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001244"), "Notification schema create"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001245"), "Notification schema create")),
                new SystemPermission(SystemIds.Permission.NotificationSchema.VIEW, Permissions.NOTIFICATION_SCHEMA_VIEW.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001246"), "Notification schema view"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001247"), "Notification schema view")),
                new SystemPermission(SystemIds.Permission.NotificationSchema.UPDATE, Permissions.NOTIFICATION_SCHEMA_UPDATE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001248"), "Notification schema update"), new I18n(UUID.fromString("00000000-0000-0000-0012-000000001249"), "Notification schema update")),
                new SystemPermission(SystemIds.Permission.NotificationSchema.DELETE, Permissions.NOTIFICATION_SCHEMA_DELETE.name(), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000124a"), "Notification schema delete"), new I18n(UUID.fromString("00000000-0000-0000-0012-00000000124b"), "Notification schema delete"))
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

    public record SystemPermission(UUID id, String key, I18n name, I18n description) {
    }
}
