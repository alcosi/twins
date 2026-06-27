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

    static {
        // System permissions. id/key sourced from SystemIds.Permission.* (single source of truth
        // — Permissions enum reads from there). name/description i18n UUIDs + translations copied
        // verbatim from Flyway migrations (V1.3.180.01 / V1.3.217.02 / V1.3.267.04 / V1.3.276.02 /
        // V1.3.316.02 / V1.3.435.01 / V1.3.448.02 / V1.3.486.01 + V1.4.*). Permissions without
        // i18n in migrations have null name/description here.
        SYSTEM_PERMISSIONS = List.of(
                // ─── 0001 GENERAL ───
                new SystemPermission(SystemIds.Permission.General.DENY_ALL,             Permissions.DENY_ALL.name(),             null, null),
                new SystemPermission(SystemIds.Permission.General.SYSTEM_APP_INFO_VIEW, Permissions.SYSTEM_APP_INFO_VIEW.name(), null, null),
                new SystemPermission(SystemIds.Permission.General.LOG_SUBSTITUTION_VIEW,Permissions.LOG_SUBSTITUTION_VIEW.name(),null, null),
                new SystemPermission(SystemIds.Permission.General.ACT_AS_USER,          Permissions.ACT_AS_USER.name(),          new I18n(UUID.fromString("fb8cd66e-db98-4882-9a4d-66cf78102290"), "Act as user"), new I18n(UUID.fromString("a007e960-9b60-4e41-8d8c-5f1acd2a3bf4"), "Give possibility to act as other user")),
                new SystemPermission(SystemIds.Permission.General.SYSTEM_CACHE_EVICT,   Permissions.SYSTEM_CACHE_EVICT.name(),   null, null),

                // ─── 0002 TWINFLOW ───
                new SystemPermission(SystemIds.Permission.Twinflow.MANAGE, Permissions.TWINFLOW_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Twinflow.CREATE, Permissions.TWINFLOW_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Twinflow.VIEW,   Permissions.TWINFLOW_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Twinflow.UPDATE, Permissions.TWINFLOW_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Twinflow.DELETE, Permissions.TWINFLOW_DELETE.name(), new I18n(UUID.fromString("29d711a7-6cd2-4740-b81b-962a7fbbb72e"), "Twinflow delete"), new I18n(UUID.fromString("8ee9e49d-9fe8-4024-8fc2-bf76a3a062be"), "Twinflow delete")),

                // ─── 0003 TWINFLOW SCHEMA ───
                new SystemPermission(SystemIds.Permission.TwinflowSchema.MANAGE, Permissions.TWINFLOW_SCHEMA_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.CREATE, Permissions.TWINFLOW_SCHEMA_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc048"), "Twinflow schema create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956748"), "Twinflow schema create")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.VIEW,   Permissions.TWINFLOW_SCHEMA_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.UPDATE, Permissions.TWINFLOW_SCHEMA_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc049"), "Twinflow schema update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956749"), "Twinflow schema update")),
                new SystemPermission(SystemIds.Permission.TwinflowSchema.DELETE, Permissions.TWINFLOW_SCHEMA_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc050"), "Twinflow schema delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956750"), "Twinflow schema delete")),

                // ─── 0004 TWIN CLASS ───
                new SystemPermission(SystemIds.Permission.TwinClass.MANAGE, Permissions.TWIN_CLASS_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClass.CREATE, Permissions.TWIN_CLASS_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClass.VIEW,   Permissions.TWIN_CLASS_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinClass.UPDATE, Permissions.TWIN_CLASS_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClass.DELETE, Permissions.TWIN_CLASS_DELETE.name(), null, null),

                // ─── 0005 TWIN CLASS FIELD ───
                new SystemPermission(SystemIds.Permission.TwinClassField.MANAGE, Permissions.TWIN_CLASS_FIELD_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassField.CREATE, Permissions.TWIN_CLASS_FIELD_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc051"), "Twin class field create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956751"), "Twin class field create")),
                new SystemPermission(SystemIds.Permission.TwinClassField.VIEW,   Permissions.TWIN_CLASS_FIELD_VIEW.name(),   new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc052"), "Twin class field view"),   new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956752"), "Twin class field view")),
                new SystemPermission(SystemIds.Permission.TwinClassField.UPDATE, Permissions.TWIN_CLASS_FIELD_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc053"), "Twin class field update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956753"), "Twin class field update")),
                new SystemPermission(SystemIds.Permission.TwinClassField.DELETE, Permissions.TWIN_CLASS_FIELD_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc054"), "Twin class field delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956754"), "Twin class field delete")),

                // ─── 0006 TWIN CLASS CARD ───
                new SystemPermission(SystemIds.Permission.TwinClassCard.MANAGE, Permissions.TWIN_CLASS_CARD_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassCard.CREATE, Permissions.TWIN_CLASS_CARD_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc006"), "Twin class card create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956706"), "Twin class card create")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.VIEW,   Permissions.TWIN_CLASS_CARD_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinClassCard.UPDATE, Permissions.TWIN_CLASS_CARD_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc007"), "Twin class card update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956707"), "Twin class card update")),
                new SystemPermission(SystemIds.Permission.TwinClassCard.DELETE, Permissions.TWIN_CLASS_CARD_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc008"), "Twin class card delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956708"), "Twin class card delete")),

                // ─── 0007 TRANSITION ───
                new SystemPermission(SystemIds.Permission.Transition.MANAGE,  Permissions.TRANSITION_MANAGE.name(),  null, null),
                new SystemPermission(SystemIds.Permission.Transition.CREATE,  Permissions.TRANSITION_CREATE.name(),  null, null),
                new SystemPermission(SystemIds.Permission.Transition.VIEW,    Permissions.TRANSITION_VIEW.name(),    null, null),
                new SystemPermission(SystemIds.Permission.Transition.UPDATE,  Permissions.TRANSITION_UPDATE.name(),  null, null),
                new SystemPermission(SystemIds.Permission.Transition.DELETE,  Permissions.TRANSITION_DELETE.name(),  new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc002"), "Transition delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956702"), "Transition delete")),
                new SystemPermission(SystemIds.Permission.Transition.PERFORM, Permissions.TRANSITION_PERFORM.name(), null, null),
                new SystemPermission(SystemIds.Permission.Transition.DRAFT,   Permissions.TRANSITION_DRAFT.name(),   null, null),

                // ─── 0008 LINK ───
                new SystemPermission(SystemIds.Permission.Link.MANAGE, Permissions.LINK_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Link.CREATE, Permissions.LINK_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Link.VIEW,   Permissions.LINK_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Link.UPDATE, Permissions.LINK_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Link.DELETE, Permissions.LINK_DELETE.name(), null, null),

                // ─── 0009 DOMAIN ───
                new SystemPermission(SystemIds.Permission.Domain.MANAGE,           Permissions.DOMAIN_MANAGE.name(),           new I18n(UUID.fromString("f1203df7-a6d9-4b5d-a3ab-75481afe9877"), "Domain manage permission"), new I18n(UUID.fromString("eebf50d3-3053-42ea-9863-57104ce9b7fe"), "domain manage permission")),
                new SystemPermission(SystemIds.Permission.Domain.CREATE,           Permissions.DOMAIN_CREATE.name(),           null, null),
                new SystemPermission(SystemIds.Permission.Domain.VIEW,             Permissions.DOMAIN_VIEW.name(),             null, null),
                new SystemPermission(SystemIds.Permission.Domain.UPDATE,           Permissions.DOMAIN_UPDATE.name(),           null, null),
                new SystemPermission(SystemIds.Permission.Domain.DELETE,           Permissions.DOMAIN_DELETE.name(),           null, null),
                new SystemPermission(SystemIds.Permission.Domain.TWINS_VIEW_ALL,   Permissions.DOMAIN_TWINS_VIEW_ALL.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Domain.TWINS_CREATE_ANY, Permissions.DOMAIN_TWINS_CREATE_ANY.name(), new I18n(UUID.fromString("b9ebdb43-e334-46f2-93d8-d8d79d66c6a6"), "Domain twins create any"), new I18n(UUID.fromString("edf24088-797d-4485-98af-929710bc046c"), "domain twins create any")),

                // ─── 0010 TWIN STATUS ───
                new SystemPermission(SystemIds.Permission.TwinStatus.MANAGE, Permissions.TWIN_STATUS_MANAGE.name(), new I18n(UUID.fromString("232cbe43-9ca5-427a-86c9-74a56c8b8e7f"), "Twin status manage permission"), new I18n(UUID.fromString("782a73d2-5eff-43db-982e-943fd1f72426"), "twin status manage permission")),
                new SystemPermission(SystemIds.Permission.TwinStatus.CREATE, Permissions.TWIN_STATUS_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinStatus.VIEW,   Permissions.TWIN_STATUS_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinStatus.UPDATE, Permissions.TWIN_STATUS_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinStatus.DELETE, Permissions.TWIN_STATUS_DELETE.name(), null, null),

                // ─── 0011 TWIN ───
                new SystemPermission(SystemIds.Permission.Twin.MANAGE,        Permissions.TWIN_MANAGE.name(),        new I18n(UUID.fromString("9d9a97b8-ae09-467b-ad09-4a12ae89abb8"), "Twin manage permission"), new I18n(UUID.fromString("110a1ef2-be50-4d74-b68b-9a2d28b1b83f"), "twin manage permission")),
                new SystemPermission(SystemIds.Permission.Twin.CREATE,        Permissions.TWIN_CREATE.name(),        null, null),
                new SystemPermission(SystemIds.Permission.Twin.VIEW,          Permissions.TWIN_VIEW.name(),          null, null),
                new SystemPermission(SystemIds.Permission.Twin.UPDATE,        Permissions.TWIN_UPDATE.name(),        null, null),
                new SystemPermission(SystemIds.Permission.Twin.DELETE,        Permissions.TWIN_DELETE.name(),        null, null),
                new SystemPermission(SystemIds.Permission.Twin.SKETCH_CREATE, Permissions.TWIN_SKETCH_CREATE.name(), new I18n(UUID.fromString("19aa7ccf-cbec-4922-80a2-1b5ca00dd3b8"), "Twin sketch create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956725"), "Twinflow create")),

                // ─── 0012 COMMENT ───
                new SystemPermission(SystemIds.Permission.Comment.MANAGE, Permissions.COMMENT_MANAGE.name(), new I18n(UUID.fromString("1b30d251-b068-4bb8-aaa2-21382feb7d56"), "Comment manage permission"), new I18n(UUID.fromString("792a5685-751b-496c-b1ef-322d96e1c94a"), "comment manage permission")),
                new SystemPermission(SystemIds.Permission.Comment.CREATE, Permissions.COMMENT_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Comment.VIEW,   Permissions.COMMENT_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Comment.UPDATE, Permissions.COMMENT_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Comment.DELETE, Permissions.COMMENT_DELETE.name(), null, null),

                // ─── 0013 ATTACHMENT ───
                new SystemPermission(SystemIds.Permission.Attachment.MANAGE,   Permissions.ATTACHMENT_MANAGE.name(),   new I18n(UUID.fromString("6b066663-6182-4632-ab63-b53c27d00651"), "Attachment manage permission"), new I18n(UUID.fromString("a585f023-595f-4c53-bb49-a23b48a731fd"), "attachment manage permission")),
                new SystemPermission(SystemIds.Permission.Attachment.CREATE,   Permissions.ATTACHMENT_CREATE.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Attachment.VIEW,     Permissions.ATTACHMENT_VIEW.name(),     null, null),
                new SystemPermission(SystemIds.Permission.Attachment.UPDATE,   Permissions.ATTACHMENT_UPDATE.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Attachment.DELETE,   Permissions.ATTACHMENT_DELETE.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Attachment.VALIDATE, Permissions.ATTACHMENT_VALIDATE.name(), null, null),

                // ─── 0014 USER ───
                new SystemPermission(SystemIds.Permission.User.MANAGE, Permissions.USER_MANAGE.name(), new I18n(UUID.fromString("a35b40b9-65bf-4a72-aec4-a5c62cae5e3e"), "User manage permission"), new I18n(UUID.fromString("f27a3375-7742-466b-acf3-1581a5dec66f"), "user manage permission")),
                new SystemPermission(SystemIds.Permission.User.CREATE, Permissions.USER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.User.VIEW,   Permissions.USER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.User.UPDATE, Permissions.USER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.User.DELETE, Permissions.USER_DELETE.name(), null, null),

                // ─── 0015 USER GROUP ───
                new SystemPermission(SystemIds.Permission.UserGroup.MANAGE, Permissions.USER_GROUP_MANAGE.name(), new I18n(UUID.fromString("49dfc3a4-4dc7-4769-8463-0b05f4a17b2d"), "User group manage permission"), new I18n(UUID.fromString("fdc14ecd-bf6a-48bc-a570-caa1088d7b65"), "user group manage permission")),
                new SystemPermission(SystemIds.Permission.UserGroup.CREATE, Permissions.USER_GROUP_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroup.VIEW,   Permissions.USER_GROUP_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.UserGroup.UPDATE, Permissions.USER_GROUP_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroup.DELETE, Permissions.USER_GROUP_DELETE.name(), null, null),

                // ─── 0016 DATA LIST ───
                new SystemPermission(SystemIds.Permission.DataList.MANAGE, Permissions.DATA_LIST_MANAGE.name(), new I18n(UUID.fromString("89806276-a5d1-4bf5-9e29-d2387bf6cfd8"), "Data list manage permission"), new I18n(UUID.fromString("c118e27b-b756-43a4-b722-66ea2443d68d"), "data list manage permission")),
                new SystemPermission(SystemIds.Permission.DataList.CREATE, Permissions.DATA_LIST_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataList.VIEW,   Permissions.DATA_LIST_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.DataList.UPDATE, Permissions.DATA_LIST_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataList.DELETE, Permissions.DATA_LIST_DELETE.name(), null, null),

                // ─── 0017 DATA LIST OPTION ───
                new SystemPermission(SystemIds.Permission.DataListOption.MANAGE, Permissions.DATA_LIST_OPTION_MANAGE.name(), new I18n(UUID.fromString("31b339c3-f8f5-4474-b2d4-8adf752f916d"), "Data list option manage permission"), new I18n(UUID.fromString("a781f6da-184e-425e-a582-7194dbbb651a"), "data list option manage permission")),
                new SystemPermission(SystemIds.Permission.DataListOption.CREATE, Permissions.DATA_LIST_OPTION_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataListOption.VIEW,   Permissions.DATA_LIST_OPTION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.DataListOption.UPDATE, Permissions.DATA_LIST_OPTION_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataListOption.DELETE, Permissions.DATA_LIST_OPTION_DELETE.name(), null, null),

                // ─── 0018 DATA LIST SUBSET ───
                new SystemPermission(SystemIds.Permission.DataListSubset.MANAGE, Permissions.DATA_LIST_SUBSET_MANAGE.name(), new I18n(UUID.fromString("4a59d325-5218-4aa1-959e-e406a46f8e51"), "Data list subset manage permission"), new I18n(UUID.fromString("5cf2fe15-8958-4be0-87f9-a37816b853b3"), "data list subset manage permission")),
                new SystemPermission(SystemIds.Permission.DataListSubset.CREATE, Permissions.DATA_LIST_SUBSET_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataListSubset.VIEW,   Permissions.DATA_LIST_SUBSET_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.DataListSubset.UPDATE, Permissions.DATA_LIST_SUBSET_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DataListSubset.DELETE, Permissions.DATA_LIST_SUBSET_DELETE.name(), null, null),

                // ─── 0019 PERMISSION ───
                new SystemPermission(SystemIds.Permission.PermissionEntity.MANAGE, Permissions.PERMISSION_MANAGE.name(), new I18n(UUID.fromString("e3833ff4-e5b2-410d-a48a-1aaada9c681c"), "Permission manage permission"), new I18n(UUID.fromString("5019537b-27a9-4603-8671-2abfcc9be1cf"), "permission manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionEntity.CREATE, Permissions.PERMISSION_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionEntity.VIEW,   Permissions.PERMISSION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionEntity.UPDATE, Permissions.PERMISSION_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionEntity.DELETE, Permissions.PERMISSION_DELETE.name(), null, null),

                // ─── 0020 USER GROUP INVOLVE ASSIGNEE ───
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.MANAGE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.CREATE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.VIEW,   Permissions.USER_GROUP_INVOLVE_ASSIGNEE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.UPDATE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveAssignee.DELETE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_DELETE.name(), null, null),

                // ─── 0021 PERMISSION GRANT SPACE ROLE ───
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.MANAGE, Permissions.PERMISSION_GRANT_SPACE_ROLE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.CREATE, Permissions.PERMISSION_GRANT_SPACE_ROLE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.VIEW,   Permissions.PERMISSION_GRANT_SPACE_ROLE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.UPDATE, Permissions.PERMISSION_GRANT_SPACE_ROLE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantSpaceRole.DELETE, Permissions.PERMISSION_GRANT_SPACE_ROLE_DELETE.name(), null, null),

                // ─── 0022 PERMISSION GRANT TWIN ROLE ───
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.MANAGE, Permissions.PERMISSION_GRANT_TWIN_ROLE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.CREATE, Permissions.PERMISSION_GRANT_TWIN_ROLE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.VIEW,   Permissions.PERMISSION_GRANT_TWIN_ROLE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.UPDATE, Permissions.PERMISSION_GRANT_TWIN_ROLE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantTwinRole.DELETE, Permissions.PERMISSION_GRANT_TWIN_ROLE_DELETE.name(), null, null),

                // ─── 0023 PERMISSION GRANT USER ───
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.MANAGE, Permissions.PERMISSION_GRANT_USER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.CREATE, Permissions.PERMISSION_GRANT_USER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.VIEW,   Permissions.PERMISSION_GRANT_USER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.UPDATE, Permissions.PERMISSION_GRANT_USER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUser.DELETE, Permissions.PERMISSION_GRANT_USER_DELETE.name(), null, null),

                // ─── 0024 PERMISSION GRANT USER GROUP ───
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.MANAGE, Permissions.PERMISSION_GRANT_USER_GROUP_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.CREATE, Permissions.PERMISSION_GRANT_USER_GROUP_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.VIEW,   Permissions.PERMISSION_GRANT_USER_GROUP_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.UPDATE, Permissions.PERMISSION_GRANT_USER_GROUP_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.PermissionGrantUserGroup.DELETE, Permissions.PERMISSION_GRANT_USER_GROUP_DELETE.name(), null, null),

                // ─── 0025 PERMISSION GROUP ───
                new SystemPermission(SystemIds.Permission.PermissionGroup.MANAGE, Permissions.PERMISSION_GROUP_MANAGE.name(), new I18n(UUID.fromString("1751502c-8da7-4b32-8101-62df0f9ad11e"), "Permission group manage permission"), new I18n(UUID.fromString("22626457-54f4-4927-9d05-ee8cb13b237c"), "permission group manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.CREATE, Permissions.PERMISSION_GROUP_CREATE.name(), new I18n(UUID.fromString("b407a7b1-d16b-386a-83f5-cd0d9cffc555"), "Permission Group create"), new I18n(UUID.fromString("cf4d65eb-594d-3b17-b5d3-66ab79956756"), "Permission Group create")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.VIEW,   Permissions.PERMISSION_GROUP_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionGroup.UPDATE, Permissions.PERMISSION_GROUP_UPDATE.name(), new I18n(UUID.fromString("d507a7b1-d16b-386a-83f5-cd0d9cffc557"), "Permission Group update"), new I18n(UUID.fromString("df4d65eb-594d-3b17-b5d3-66ab79956758"), "Permission Group update")),
                new SystemPermission(SystemIds.Permission.PermissionGroup.DELETE, Permissions.PERMISSION_GROUP_DELETE.name(), new I18n(UUID.fromString("e607a7b1-d16b-386a-83f5-cd0d9cffc559"), "Permission Group delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956760"), "Permission Group delete")),

                // ─── 0026 PERMISSION SCHEMA ───
                new SystemPermission(SystemIds.Permission.PermissionSchema.MANAGE, Permissions.PERMISSION_SCHEMA_MANAGE.name(), new I18n(UUID.fromString("cdc74f7c-2894-4b01-a981-e6441d0aacaf"), "Permission schema manage permission"), new I18n(UUID.fromString("41689330-3a80-48a3-870e-2ae48237eea9"), "permission schema manage permission")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.CREATE, Permissions.PERMISSION_SCHEMA_CREATE.name(), new I18n(UUID.fromString("8e44d042-3532-4ccf-b4e9-efad657f176e"), "Schema create"), new I18n(UUID.fromString("3578b14d-8e30-4b25-9e56-d7a5486d9a51"), "Schema create")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.VIEW,   Permissions.PERMISSION_SCHEMA_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.PermissionSchema.UPDATE, Permissions.PERMISSION_SCHEMA_UPDATE.name(), new I18n(UUID.fromString("4ea7f1d1-167d-43a5-8a25-5ca41138e09d"), "Schema update"), new I18n(UUID.fromString("7a7a93a6-7b1a-4f52-b6d6-9ec07a6766d4"), "Schema update")),
                new SystemPermission(SystemIds.Permission.PermissionSchema.DELETE, Permissions.PERMISSION_SCHEMA_DELETE.name(), new I18n(UUID.fromString("f53d63a9-4b8b-41a0-8a63-0b8afcfd17ca"), "Schema delete"), new I18n(UUID.fromString("c2c4b5e7-12a0-4f7b-97c1-3d03fbb0a65f"), "Schema delete")),

                // ─── 0027 USER PERMISSION ───
                new SystemPermission(SystemIds.Permission.UserPermission.MANAGE, Permissions.USER_PERMISSION_MANAGE.name(), new I18n(UUID.fromString("07e9a2b8-9f44-4379-bb23-6a687d5c70f4"), "User Permission manage"), new I18n(UUID.fromString("3db87e6a-10cb-4d1f-b1b6-5b69d41e3459"), "User Permission manage")),
                new SystemPermission(SystemIds.Permission.UserPermission.CREATE, Permissions.USER_PERMISSION_CREATE.name(), new I18n(UUID.fromString("f1b73647-b7d6-4d10-9d5b-086a4e8f4e68"), "User Permission create"), new I18n(UUID.fromString("7f48e22b-1205-4b44-8c48-9edb0a479c3d"), "User Permission create")),
                new SystemPermission(SystemIds.Permission.UserPermission.VIEW,   Permissions.USER_PERMISSION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.UserPermission.UPDATE, Permissions.USER_PERMISSION_UPDATE.name(), new I18n(UUID.fromString("82c8b983-f135-4a18-8a40-7a4b02442099"), "User Permission update"), new I18n(UUID.fromString("2906c5c0-49bc-4639-9ad6-004dbdf3d263"), "User Permission update")),
                new SystemPermission(SystemIds.Permission.UserPermission.DELETE, Permissions.USER_PERMISSION_DELETE.name(), new I18n(UUID.fromString("1f6c60f6-b1c0-4db1-9d9b-60b6f6716a0c"), "User Permission delete"), new I18n(UUID.fromString("1aa03e1e-274e-4b94-91d6-5701efb3d3e1"), "User Permission delete")),

                // ─── 0028 I18N ───
                new SystemPermission(SystemIds.Permission.I18n.MANAGE, Permissions.I18N_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.I18n.CREATE, Permissions.I18N_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.I18n.VIEW,   Permissions.I18N_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.I18n.UPDATE, Permissions.I18N_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.I18n.DELETE, Permissions.I18N_DELETE.name(), null, null),

                // ─── 0029 FACTORY ERASER ───
                new SystemPermission(SystemIds.Permission.FactoryEraser.MANAGE, Permissions.FACTORY_ERASER_MANAGE.name(), new I18n(UUID.fromString("bfcaf5aa-2c7e-49f1-bb71-72bddb8a0abf"), "Eraser manage permission"), new I18n(UUID.fromString("9ab910b0-6054-4412-a034-c00c1e136414"), "eraser manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryEraser.CREATE, Permissions.FACTORY_ERASER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryEraser.VIEW,   Permissions.FACTORY_ERASER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.FactoryEraser.UPDATE, Permissions.FACTORY_ERASER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryEraser.DELETE, Permissions.FACTORY_ERASER_DELETE.name(), null, null),

                // ─── 0030 FACTORY ───
                new SystemPermission(SystemIds.Permission.Factory.MANAGE, Permissions.FACTORY_MANAGE.name(), new I18n(UUID.fromString("1d03aee8-a358-48b0-9a77-b7cffeb6afe0"), "Factory manage permission"), new I18n(UUID.fromString("ba202321-5091-4da0-95b0-8fc6fcc0110a"), "factory manage permission")),
                new SystemPermission(SystemIds.Permission.Factory.CREATE, Permissions.FACTORY_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Factory.VIEW,   Permissions.FACTORY_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Factory.UPDATE, Permissions.FACTORY_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Factory.DELETE, Permissions.FACTORY_DELETE.name(), null, null),

                // ─── 0031 FACTORY MULTIPLIER ───
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.MANAGE,       Permissions.FACTORY_MULTIPLIER_MANAGE.name(),       new I18n(UUID.fromString("aaebc4a6-92a3-4f66-8394-9824ecda27ea"), "multiplier manage permission"),       new I18n(UUID.fromString("e0e27b29-abb9-4e80-ad3f-cca9b3819fdc"), "multiplier manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.CREATE,       Permissions.FACTORY_MULTIPLIER_CREATE.name(),       null, null),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.VIEW,         Permissions.FACTORY_MULTIPLIER_VIEW.name(),         null, null),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.UPDATE,       Permissions.FACTORY_MULTIPLIER_UPDATE.name(),       null, null),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.DELETE,       Permissions.FACTORY_MULTIPLIER_DELETE.name(),       null, null),
                new SystemPermission(SystemIds.Permission.FactoryMultiplier.PARAM_MANAGE, Permissions.FACTORY_MULTIPLIER_PARAM_MANAGE.name(), new I18n(UUID.fromString("1f752658-3594-4b35-8311-76aa11d21468"), "Multiplier param manage permission"), new I18n(UUID.fromString("d639ec60-a9c3-4584-8572-b22538f3c189"), "multiplier param manage permission")),

                // ─── 0032 FACTORY PIPELINE ───
                new SystemPermission(SystemIds.Permission.FactoryPipeline.MANAGE, Permissions.FACTORY_PIPELINE_MANAGE.name(), new I18n(UUID.fromString("2d35015c-2446-4339-8cad-b60b9dbac6c8"), "Pipeline manage permission"), new I18n(UUID.fromString("9e4026c0-1496-4fae-aa83-e2a973f91bc0"), "pipeline manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.CREATE, Permissions.FACTORY_PIPELINE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.VIEW,   Permissions.FACTORY_PIPELINE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.UPDATE, Permissions.FACTORY_PIPELINE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipeline.DELETE, Permissions.FACTORY_PIPELINE_DELETE.name(), null, null),

                // ─── 0033 FACTORY CONDITION SET ───
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.MANAGE, Permissions.FACTORY_CONDITION_SET_MANAGE.name(), new I18n(UUID.fromString("bb4e4298-469b-4cb7-baa2-a5e1932709a4"), "Condition set manage permission"), new I18n(UUID.fromString("123e3aae-5b83-4e61-8e1f-e5b4ec602d60"), "condition set manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.CREATE, Permissions.FACTORY_CONDITION_SET_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.VIEW,   Permissions.FACTORY_CONDITION_SET_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.UPDATE, Permissions.FACTORY_CONDITION_SET_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryConditionSet.DELETE, Permissions.FACTORY_CONDITION_SET_DELETE.name(), null, null),

                // ─── 0034 FACTORY BRANCH ───
                new SystemPermission(SystemIds.Permission.FactoryBranch.MANAGE, Permissions.FACTORY_BRANCH_MANAGE.name(), new I18n(UUID.fromString("6bec0d9e-646e-4de3-8527-8d43af615a71"), "Branch manage permission"), new I18n(UUID.fromString("c0275859-5465-40eb-96c9-47e56f23ecbd"), "branch manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryBranch.CREATE, Permissions.FACTORY_BRANCH_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryBranch.VIEW,   Permissions.FACTORY_BRANCH_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.FactoryBranch.UPDATE, Permissions.FACTORY_BRANCH_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryBranch.DELETE, Permissions.FACTORY_BRANCH_DELETE.name(), null, null),

                // ─── 0035 DRAFT ───
                new SystemPermission(SystemIds.Permission.Draft.MANAGE, Permissions.DRAFT_MANAGE.name(), new I18n(UUID.fromString("e29fb86a-1780-4751-9799-0ec72bc6c923"), "Draft manage"), new I18n(UUID.fromString("e7f363b9-2f3e-453c-8fd9-dc29c0aa683c"), "Draft manage")),
                new SystemPermission(SystemIds.Permission.Draft.CREATE, Permissions.DRAFT_CREATE.name(), new I18n(UUID.fromString("7856222c-7e24-4f7d-9962-7ae67a7b05e1"), "Draft create"), new I18n(UUID.fromString("c3d0bbf2-eb05-4515-ab45-2ecf5e21c993"), "Draft create")),
                new SystemPermission(SystemIds.Permission.Draft.VIEW,   Permissions.DRAFT_VIEW.name(),   new I18n(UUID.fromString("185565a6-9727-4d08-b26d-eff24bf3b248"), "Draft view"),   new I18n(UUID.fromString("54cf7275-615e-4608-8e63-d48c175d43a9"), "Draft view")),
                new SystemPermission(SystemIds.Permission.Draft.UPDATE, Permissions.DRAFT_UPDATE.name(), new I18n(UUID.fromString("66d51f57-78f7-4eca-aa01-fabcc048e327"), "Draft update"), new I18n(UUID.fromString("12d871d2-5c23-4b23-a139-a85e3e798f1f"), "Draft update")),
                new SystemPermission(SystemIds.Permission.Draft.DELETE, Permissions.DRAFT_DELETE.name(), new I18n(UUID.fromString("f01254bf-89a2-4e52-9e85-1205aa69bd29"), "Draft delete"), new I18n(UUID.fromString("c6fb326f-d2a5-4021-b22d-29cf1fa45204"), "Draft delete")),
                new SystemPermission(SystemIds.Permission.Draft.COMMIT, Permissions.DRAFT_COMMIT.name(), null, null),

                // ─── 0036 DOMAIN BUSINESS ACCOUNT ───
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.MANAGE, Permissions.DOMAIN_BUSINESS_ACCOUNT_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.CREATE, Permissions.DOMAIN_BUSINESS_ACCOUNT_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.VIEW,   Permissions.DOMAIN_BUSINESS_ACCOUNT_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.UPDATE, Permissions.DOMAIN_BUSINESS_ACCOUNT_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainBusinessAccount.DELETE, Permissions.DOMAIN_BUSINESS_ACCOUNT_DELETE.name(), null, null),

                // ─── 0037 DOMAIN USER ───
                new SystemPermission(SystemIds.Permission.DomainUser.MANAGE, Permissions.DOMAIN_USER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainUser.CREATE, Permissions.DOMAIN_USER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainUser.VIEW,   Permissions.DOMAIN_USER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.DomainUser.UPDATE, Permissions.DOMAIN_USER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.DomainUser.DELETE, Permissions.DOMAIN_USER_DELETE.name(), null, null),

                // ─── 0038 BUSINESS ACCOUNT ───
                new SystemPermission(SystemIds.Permission.BusinessAccount.MANAGE, Permissions.BUSINESS_ACCOUNT_MANAGE.name(), new I18n(UUID.fromString("2c4ec981-4e53-4968-9ffe-215e69cee856"), "Business account manage permission"), new I18n(UUID.fromString("9b688865-81be-4ff1-9ea6-3e0d5f88ed01"), "business account manage permission")),
                new SystemPermission(SystemIds.Permission.BusinessAccount.CREATE, Permissions.BUSINESS_ACCOUNT_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.BusinessAccount.VIEW,   Permissions.BUSINESS_ACCOUNT_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.BusinessAccount.UPDATE, Permissions.BUSINESS_ACCOUNT_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.BusinessAccount.DELETE, Permissions.BUSINESS_ACCOUNT_DELETE.name(), null, null),

                // ─── 0039 SPACE ROLE ───
                new SystemPermission(SystemIds.Permission.SpaceRole.MANAGE, Permissions.SPACE_ROLE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.SpaceRole.CREATE, Permissions.SPACE_ROLE_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc039"), "Space role create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956739"), "Space role create")),
                new SystemPermission(SystemIds.Permission.SpaceRole.VIEW,   Permissions.SPACE_ROLE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.SpaceRole.UPDATE, Permissions.SPACE_ROLE_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc040"), "Space role update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956740"), "Space role update")),
                new SystemPermission(SystemIds.Permission.SpaceRole.DELETE, Permissions.SPACE_ROLE_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc041"), "Space role delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956741"), "Space role delete")),

                // ─── 0040 FEATURER ───
                new SystemPermission(SystemIds.Permission.Featurer.MANAGE, Permissions.FEATURER_MANAGE.name(), new I18n(UUID.fromString("7f7175c8-6d23-48b3-9bd2-2277ccf03f95"), "Featurer manage permission"), new I18n(UUID.fromString("f3c8d5cd-1c08-4e29-bc09-df293e1ab44b"), "featurer manage permission")),
                new SystemPermission(SystemIds.Permission.Featurer.CREATE, Permissions.FEATURER_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc042"), "Featurer create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956742"), "Featurer create")),
                new SystemPermission(SystemIds.Permission.Featurer.VIEW,   Permissions.FEATURER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Featurer.UPDATE, Permissions.FEATURER_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc043"), "Featurer update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956743"), "Featurer update")),
                new SystemPermission(SystemIds.Permission.Featurer.DELETE, Permissions.FEATURER_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc044"), "Featurer delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956744"), "Featurer delete")),

                // ─── 0041 TIER ───
                new SystemPermission(SystemIds.Permission.Tier.MANAGE, Permissions.TIER_MANAGE.name(), new I18n(UUID.fromString("b871973e-2f36-483e-a899-6ab754a2739c"), "Tier manage permission"), new I18n(UUID.fromString("88eafed6-b864-45ff-bc99-0485dd8ac06c"), "tier manage permission")),
                new SystemPermission(SystemIds.Permission.Tier.CREATE, Permissions.TIER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Tier.VIEW,   Permissions.TIER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Tier.UPDATE, Permissions.TIER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Tier.DELETE, Permissions.TIER_DELETE.name(), null, null),

                // ─── 0042 FACE ───
                new SystemPermission(SystemIds.Permission.Face.MANAGE, Permissions.FACE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Face.CREATE, Permissions.FACE_CREATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc045"), "Face create"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956745"), "Face create")),
                new SystemPermission(SystemIds.Permission.Face.VIEW,   Permissions.FACE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Face.UPDATE, Permissions.FACE_UPDATE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc046"), "Face update"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956746"), "Face update")),
                new SystemPermission(SystemIds.Permission.Face.DELETE, Permissions.FACE_DELETE.name(), new I18n(UUID.fromString("a207a7b1-d16b-386a-83f5-cd0d9cffc047"), "Face delete"), new I18n(UUID.fromString("ef4d65eb-594d-3b17-b5d3-66ab79956747"), "Face delete")),

                // ─── 0043 FACTORY PIPELINE STEP ───
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.MANAGE, Permissions.FACTORY_PIPELINE_STEP_MANAGE.name(), new I18n(UUID.fromString("4bca9177-8301-42dd-b947-bf93d1d75c8a"), "Pipeline step manage permission"), new I18n(UUID.fromString("006df45e-bcf8-4aa8-8776-4decb7c6f537"), "pipeline step manage permission")),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.CREATE, Permissions.FACTORY_PIPELINE_STEP_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.VIEW,   Permissions.FACTORY_PIPELINE_STEP_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.UPDATE, Permissions.FACTORY_PIPELINE_STEP_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.FactoryPipelineStep.DELETE, Permissions.FACTORY_PIPELINE_STEP_DELETE.name(), null, null),

                // ─── 0044 HISTORY ───
                new SystemPermission(SystemIds.Permission.History.MANAGE,            Permissions.HISTORY_MANAGE.name(),            new I18n(UUID.fromString("dbce948e-47a3-4e5b-a6ec-8c6e2a06f6f0"), "History manage"), new I18n(UUID.fromString("e9638f97-9dbf-4c07-a871-f5c57df87f69"), "History manage")),
                new SystemPermission(SystemIds.Permission.History.CREATE,            Permissions.HISTORY_CREATE.name(),            new I18n(UUID.fromString("2e9a1c12-4a3b-4e02-9c28-21f241377d33"), "History create"), new I18n(UUID.fromString("11d9c5b4-153e-4e57-99e6-35d5401c45a3"), "History create")),
                new SystemPermission(SystemIds.Permission.History.VIEW,              Permissions.HISTORY_VIEW.name(),              new I18n(UUID.fromString("b18a2c65-b9d6-4e4d-8c5a-f1e0688a11bc"), "History view"),   new I18n(UUID.fromString("96f06362-94ad-4c9f-b476-53e65a9470bc"), "History view")),
                new SystemPermission(SystemIds.Permission.History.UPDATE,            Permissions.HISTORY_UPDATE.name(),            new I18n(UUID.fromString("d1a1799c-7984-4889-b3e7-34803f3c7ca0"), "History update"), new I18n(UUID.fromString("fbd8d3db-9c85-4dbd-80ea-44fa3e4e1ee7"), "History update")),
                new SystemPermission(SystemIds.Permission.History.DELETE,            Permissions.HISTORY_DELETE.name(),            new I18n(UUID.fromString("0b3c53e9-3a3e-48b6-81de-cfcd3ccf0f23"), "History delete"), new I18n(UUID.fromString("6a785344-0f28-4f5c-8ee8-eaf646efb6d9"), "History delete")),
                new SystemPermission(SystemIds.Permission.History.MACHINE_USER_VIEW, Permissions.HISTORY_MACHINE_USER_VIEW.name(), null, null),

                // ─── 0045 PROJECTION ───
                new SystemPermission(SystemIds.Permission.Projection.MANAGE, Permissions.PROJECTION_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Projection.CREATE, Permissions.PROJECTION_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Projection.VIEW,   Permissions.PROJECTION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Projection.UPDATE, Permissions.PROJECTION_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Projection.DELETE, Permissions.PROJECTION_DELETE.name(), null, null),

                // ─── 0046 PROJECTION EXCLUSION ───
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.MANAGE, Permissions.PROJECTION_EXCLUSION_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.CREATE, Permissions.PROJECTION_EXCLUSION_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.VIEW,   Permissions.PROJECTION_EXCLUSION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.UPDATE, Permissions.PROJECTION_EXCLUSION_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ProjectionExclusion.DELETE, Permissions.PROJECTION_EXCLUSION_DELETE.name(), null, null),

                // ─── 0047 TWIN CLASS FIELD RULE ───
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.CREATE, Permissions.TWIN_CLASS_FIELD_RULE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.VIEW,   Permissions.TWIN_CLASS_FIELD_RULE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.UPDATE, Permissions.TWIN_CLASS_FIELD_RULE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFieldRule.DELETE, Permissions.TWIN_CLASS_FIELD_RULE_DELETE.name(), null, null),

                // ─── 0048 TWINFLOW FACTORY ───
                new SystemPermission(SystemIds.Permission.TwinflowFactory.MANAGE, Permissions.TWINFLOW_FACTORY_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.CREATE, Permissions.TWINFLOW_FACTORY_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.VIEW,   Permissions.TWINFLOW_FACTORY_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.UPDATE, Permissions.TWINFLOW_FACTORY_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinflowFactory.DELETE, Permissions.TWINFLOW_FACTORY_DELETE.name(), null, null),

                // ─── 0049 TWIN CLASS FREEZE ───
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.MANAGE, Permissions.TWIN_CLASS_FREEZE_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.CREATE, Permissions.TWIN_CLASS_FREEZE_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.VIEW,   Permissions.TWIN_CLASS_FREEZE_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.UPDATE, Permissions.TWIN_CLASS_FREEZE_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassFreeze.DELETE, Permissions.TWIN_CLASS_FREEZE_DELETE.name(), null, null),

                // ─── 0050 HISTORY NOTIFICATION ───
                new SystemPermission(SystemIds.Permission.HistoryNotification.MANAGE, Permissions.HISTORY_NOTIFICATION_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.HistoryNotification.CREATE, Permissions.HISTORY_NOTIFICATION_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.HistoryNotification.VIEW,   Permissions.HISTORY_NOTIFICATION_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.HistoryNotification.UPDATE, Permissions.HISTORY_NOTIFICATION_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.HistoryNotification.DELETE, Permissions.HISTORY_NOTIFICATION_DELETE.name(), null, null),

                // ─── 0051 SCHEDULER ───
                new SystemPermission(SystemIds.Permission.Scheduler.MANAGE, Permissions.SCHEDULER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Scheduler.CREATE, Permissions.SCHEDULER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Scheduler.VIEW,   Permissions.SCHEDULER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.Scheduler.UPDATE, Permissions.SCHEDULER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.Scheduler.DELETE, Permissions.SCHEDULER_DELETE.name(), null, null),

                // ─── 0052 TWIN CLASS DYNAMIC MARKER ───
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.MANAGE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.CREATE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.VIEW,   Permissions.TWIN_CLASS_DYNAMIC_MARKER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.UPDATE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinClassDynamicMarker.DELETE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_DELETE.name(), null, null),

                // ─── 0053 TWIN VALIDATOR SET ───
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.MANAGE, Permissions.TWIN_VALIDATOR_SET_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.CREATE, Permissions.TWIN_VALIDATOR_SET_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.VIEW,   Permissions.TWIN_VALIDATOR_SET_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.UPDATE, Permissions.TWIN_VALIDATOR_SET_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinValidatorSet.DELETE, Permissions.TWIN_VALIDATOR_SET_DELETE.name(), null, null),

                // ─── 0054 TWIN TRIGGER ───
                new SystemPermission(SystemIds.Permission.TwinTrigger.MANAGE, Permissions.TWIN_TRIGGER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinTrigger.CREATE, Permissions.TWIN_TRIGGER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinTrigger.VIEW,   Permissions.TWIN_TRIGGER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.TwinTrigger.UPDATE, Permissions.TWIN_TRIGGER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.TwinTrigger.DELETE, Permissions.TWIN_TRIGGER_DELETE.name(), null, null),

                // ─── 0055 USER GROUP INVOLVE ACT AS USER ───
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.MANAGE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.CREATE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.VIEW,   Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.UPDATE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.UserGroupInvolveActAsUser.DELETE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_DELETE.name(), null, null),

                // ─── 0056 ACTION RESTRICTION REASON ───
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.MANAGE, Permissions.ACTION_RESTRICTION_REASON_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.CREATE, Permissions.ACTION_RESTRICTION_REASON_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.VIEW,   Permissions.ACTION_RESTRICTION_REASON_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.UPDATE, Permissions.ACTION_RESTRICTION_REASON_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.ActionRestrictionReason.DELETE, Permissions.ACTION_RESTRICTION_REASON_DELETE.name(), null, null),

                // ─── 0057 NOTIFICATION SCHEMA ───
                new SystemPermission(SystemIds.Permission.NotificationSchema.MANAGE, Permissions.NOTIFICATION_SCHEMA_MANAGE.name(), null, null),
                new SystemPermission(SystemIds.Permission.NotificationSchema.CREATE, Permissions.NOTIFICATION_SCHEMA_CREATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.NotificationSchema.VIEW,   Permissions.NOTIFICATION_SCHEMA_VIEW.name(),   null, null),
                new SystemPermission(SystemIds.Permission.NotificationSchema.UPDATE, Permissions.NOTIFICATION_SCHEMA_UPDATE.name(), null, null),
                new SystemPermission(SystemIds.Permission.NotificationSchema.DELETE, Permissions.NOTIFICATION_SCHEMA_DELETE.name(), null, null)
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
