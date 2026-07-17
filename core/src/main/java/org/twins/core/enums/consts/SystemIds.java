package org.twins.core.enums.consts;

import java.util.Set;
import java.util.UUID;

/**
 * All system-level UUID identifiers used across the Twins codebase — pure constants only.
 *
 * <p>Grouped into nested holder classes by entity type. Each UUID is the canonical,
 * stable identifier of a system row inserted at application bootstrap (see
 * {@code SystemEntityBootstrapService}) or via Flyway migrations.
 *
 * <p>Usage: {@code SystemIds.TwinClass.USER}, {@code SystemIds.TwinClassField.Glossary.PURPOSE}.
 *
 * <p>For TwinClassField and TwinStatus, second-level holders group constants by the owning
 * TwinClass (User / GlobalAncestor / Glossary / etc.). For TwinStatus, the per-class initial
 * status is named {@code INIT}.
 *
 * <p>Lookup predicates ({@code isTwinClassForUser}, {@code isSystemField}, etc.) live in
 * {@code SystemLookup} — this class is data only. When a new system field is added to
 * {@code SystemBootstrapData.SYSTEM_CLASSES}, add its UUID to
 * {@link TwinClassField.Base#ALL_FIELDS_SET} below.
 */
public final class SystemIds {
    private SystemIds() {}

    public static final class User {
        public static final UUID SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    public static final class UserGroup {
        public static final UUID DOMAIN_ADMIN = UUID.fromString("00000000-0000-0000-0006-000000000001");
    }

    public static final class TwinClass {
        public static final UUID USER             = UUID.fromString("00000000-0000-0000-0001-000000000001");
        public static final UUID BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0001-000000000003");
        public static final UUID GLOBAL_ANCESTOR  = UUID.fromString("00000000-0000-0000-0001-000000000004");
        public static final UUID FACE_PAGE        = UUID.fromString("00000000-0000-0000-0001-000000000005");
        public static final UUID TWINS_GLOSSARY   = UUID.fromString("00000000-0000-0000-0001-000000000006");
    }

    public static final class TwinClassField {

        public static final class User {
            public static final UUID EMAIL  = UUID.fromString("00000000-0000-0000-0011-000000000001");
            public static final UUID AVATAR = UUID.fromString("00000000-0000-0000-0011-000000000002");
        }

        public static final class Base {
            public static final UUID NAME             = UUID.fromString("00000000-0000-0000-0011-000000000003");
            public static final UUID DESCRIPTION      = UUID.fromString("00000000-0000-0000-0011-000000000004");
            public static final UUID EXTERNAL_ID      = UUID.fromString("00000000-0000-0000-0011-000000000005");
            public static final UUID OWNER_USER_ID    = UUID.fromString("00000000-0000-0000-0011-000000000006");
            public static final UUID ASSIGNEE_USER_ID = UUID.fromString("00000000-0000-0000-0011-000000000007");
            public static final UUID CREATOR_USER_ID  = UUID.fromString("00000000-0000-0000-0011-000000000008");
            public static final UUID HEAD_ID          = UUID.fromString("00000000-0000-0000-0011-000000000009");
            public static final UUID STATUS_ID        = UUID.fromString("00000000-0000-0000-0011-000000000010");
            public static final UUID CREATED_AT       = UUID.fromString("00000000-0000-0000-0011-000000000011");
            public static final UUID ID               = UUID.fromString("00000000-0000-0000-0011-000000000012");
            public static final UUID TWIN_CLASS_ID    = UUID.fromString("00000000-0000-0000-0011-000000000013");
            public static final UUID ALIASES          = UUID.fromString("00000000-0000-0000-0011-000000000014");
            public static final UUID TAGS             = UUID.fromString("00000000-0000-0000-0011-000000000015");
            public static final UUID MARKERS          = UUID.fromString("00000000-0000-0000-0011-000000000016");
            public static final UUID FLAVOR_ID = UUID.fromString("00000000-0000-0000-0011-000000000017");
            /**
             * All TwinClassField UUIDs that belong to system TwinClasses (USER / GLOBAL_ANCESTOR /
             * TWINS_GLOSSARY). Enumerated explicitly to keep this class self-contained — no
             * dependency back to {@code SystemBootstrapData}. When a new system field is added
             * to {@code SystemBootstrapData.SYSTEM_CLASSES}, add its UUID here too.
             *
             * <p>Used by {@code SystemLookup#isSystemField(UUID)} / {@code SystemLookup#getSystemFieldsIds()}.
             */
            public static final Set<UUID> ALL_FIELDS_SET = Set.of(
                    NAME,
                    DESCRIPTION,
                    EXTERNAL_ID,
                    OWNER_USER_ID,
                    ASSIGNEE_USER_ID,
                    CREATOR_USER_ID,
                    HEAD_ID,
                    STATUS_ID,
                    CREATED_AT,
                    ID,
                    TWIN_CLASS_ID,
                    ALIASES,
                    TAGS,
                    MARKERS,
                    FLAVOR_ID
            );
        }

        public static final class Glossary {
            public static final UUID PURPOSE            = UUID.fromString("00000000-0000-0000-0011-000000001001");
            public static final UUID FIELDS             = UUID.fromString("00000000-0000-0000-0011-000000001002");
            public static final UUID RELATIONS_OVERVIEW = UUID.fromString("00000000-0000-0000-0011-000000001003");
            public static final UUID API                = UUID.fromString("00000000-0000-0000-0011-000000001004");
            public static final UUID API_DEPRECATED     = UUID.fromString("00000000-0000-0000-0011-000000001005");
            public static final UUID EXAMPLES           = UUID.fromString("00000000-0000-0000-0011-000000001006");
            public static final UUID DEV_NOTES          = UUID.fromString("00000000-0000-0000-0011-000000001007");
            public static final UUID JPA_CLASS          = UUID.fromString("00000000-0000-0000-0011-000000001008");
            public static final UUID DB_TABLE           = UUID.fromString("00000000-0000-0000-0011-000000001009");
            public static final UUID MARKDOWN_SOURCE    = UUID.fromString("00000000-0000-0000-0011-000000001010");
            public static final UUID MARKDOWN_HASH      = UUID.fromString("00000000-0000-0000-0011-000000001011");
            public static final UUID IS_SYSTEM          = UUID.fromString("00000000-0000-0000-0011-000000001012");
            public static final UUID ACTUALIZED_AT      = UUID.fromString("00000000-0000-0000-0011-000000001013");
        }
    }

    public static final class TwinStatus {
        public static final UUID SKETCH = UUID.fromString("00000001-0000-0000-0000-000000000001");

        public static final class User {
            public static final UUID INIT = UUID.fromString("00000000-0000-0000-0003-000000000001");
        }

        public static final class BusinessAccount {
            public static final UUID INIT = UUID.fromString("00000000-0000-0000-0003-000000000003");
        }

        public static final class FacePage {
            public static final UUID INIT = UUID.fromString("00000000-0000-0000-0003-000000000004");
        }

        public static final class Glossary {
            public static final UUID INIT    = UUID.fromString("00000000-0000-0000-0003-000000001001");
            public static final UUID DELETED = UUID.fromString("00000000-0000-0000-0003-000000001002");
        }
    }

    public static final class TwinTemplate {
        public static final UUID USER             = UUID.fromString("00000000-0000-0000-0002-000000000001");
        public static final UUID BUSINESS_ACCOUNT = UUID.fromString("00000000-0000-0000-0002-000000000003");
    }

    public static final class TwinClassFieldSearch {
        public static final UUID UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000001");
    }

    public static final class TwinSearch {
        public static final UUID UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000002");
    }

    public static final class TwinClassSearch {
        public static final UUID UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000003");
    }

    public static final class UserSearch {
        public static final UUID UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000004");
    }

    public static final class DataListOptionSearch {
        public static final UUID UNLIMITED = UUID.fromString("00000000-0000-0000-0014-000000000005");
    }

    public static final class PermissionScheme {
        public static final UUID DEFAULT = UUID.fromString("00000000-0000-0000-0016-000000000001");
    }

    public static final class TwinflowScheme {
        public static final UUID DEFAULT = UUID.fromString("00000000-0000-0000-0017-000000000001");
    }

    public static final class TwinClassScheme {
        public static final UUID DEFAULT = UUID.fromString("00000000-0000-0000-0018-000000000001");
    }

    /**
     * System permission UUIDs. Mirror of {@code Permissions} enum — each category gets a
     * sub-holder with action constants ({@code MANAGE}/{@code CREATE}/{@code VIEW}/
     * {@code UPDATE}/{@code DELETE}, plus extras where applicable).
     *
     * <p>The {@code Permissions} enum sources its UUIDs from here — single source of truth.
     */
    public static final class Permission {
        /** Permission group all system permissions belong to. */
        public static final UUID PERMISSION_GROUP_DEFAULT = UUID.fromString("00000000-0000-0000-0005-000000000001");

        public static final class General {
            public static final UUID DENY_ALL            = UUID.fromString("00000000-0000-0004-0001-000000000101");
            public static final UUID SYSTEM_APP_INFO_VIEW= UUID.fromString("00000000-0000-0004-0001-000000000201");
            public static final UUID LOG_SUBSTITUTION_VIEW=UUID.fromString("00000000-0000-0004-0001-000000000301");
            public static final UUID ACT_AS_USER         = UUID.fromString("00000000-0000-0004-0001-000000000401");
            public static final UUID SYSTEM_CACHE_EVICT  = UUID.fromString("00000000-0000-0004-0001-000000000501");
        }

        public static final class Twinflow {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0002-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0002-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0002-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0002-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0002-000000000005");
        }

        public static final class TwinflowSchema {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0003-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0003-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0003-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0003-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0003-000000000005");
        }

        public static final class TwinClass {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0004-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0004-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0004-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0004-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0004-000000000005");
        }

        public static final class TwinClassField {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0005-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0005-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0005-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0005-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0005-000000000005");
        }

        public static final class TwinClassCard {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0006-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0006-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0006-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0006-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0006-000000000005");
        }

        public static final class Transition {
            public static final UUID MANAGE  = UUID.fromString("00000000-0000-0004-0007-000000000001");
            public static final UUID CREATE  = UUID.fromString("00000000-0000-0004-0007-000000000002");
            public static final UUID VIEW    = UUID.fromString("00000000-0000-0004-0007-000000000003");
            public static final UUID UPDATE  = UUID.fromString("00000000-0000-0004-0007-000000000004");
            public static final UUID DELETE  = UUID.fromString("00000000-0000-0004-0007-000000000005");
            public static final UUID PERFORM = UUID.fromString("00000000-0000-0004-0007-000000000006");
            public static final UUID DRAFT   = UUID.fromString("00000000-0000-0004-0007-000000000007");
        }

        public static final class Link {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0008-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0008-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0008-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0008-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0008-000000000005");
        }

        public static final class Domain {
            public static final UUID MANAGE            = UUID.fromString("00000000-0000-0004-0009-000000000001");
            public static final UUID CREATE            = UUID.fromString("00000000-0000-0004-0009-000000000002");
            public static final UUID VIEW              = UUID.fromString("00000000-0000-0004-0009-000000000003");
            public static final UUID UPDATE            = UUID.fromString("00000000-0000-0004-0009-000000000004");
            public static final UUID DELETE            = UUID.fromString("00000000-0000-0004-0009-000000000005");
            public static final UUID TWINS_VIEW_ALL    = UUID.fromString("00000000-0000-0004-0009-000000000006");
            public static final UUID TWINS_CREATE_ANY  = UUID.fromString("00000000-0000-0004-0009-000000000007");
        }

        public static final class TwinStatus {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0010-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0010-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0010-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0010-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0010-000000000005");
        }

        public static final class Twin {
            public static final UUID MANAGE        = UUID.fromString("00000000-0000-0004-0011-000000000001");
            public static final UUID CREATE        = UUID.fromString("00000000-0000-0004-0011-000000000002");
            public static final UUID VIEW          = UUID.fromString("00000000-0000-0004-0011-000000000003");
            public static final UUID UPDATE        = UUID.fromString("00000000-0000-0004-0011-000000000004");
            public static final UUID DELETE        = UUID.fromString("00000000-0000-0004-0011-000000000005");
            public static final UUID SKETCH_CREATE = UUID.fromString("00000000-0000-0004-0011-000000000006");
        }

        public static final class Comment {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0012-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0012-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0012-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0012-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0012-000000000005");
        }

        public static final class Attachment {
            public static final UUID MANAGE   = UUID.fromString("00000000-0000-0004-0013-000000000001");
            public static final UUID CREATE   = UUID.fromString("00000000-0000-0004-0013-000000000002");
            public static final UUID VIEW     = UUID.fromString("00000000-0000-0004-0013-000000000003");
            public static final UUID UPDATE   = UUID.fromString("00000000-0000-0004-0013-000000000004");
            public static final UUID DELETE   = UUID.fromString("00000000-0000-0004-0013-000000000005");
            public static final UUID VALIDATE = UUID.fromString("00000000-0000-0004-0013-000000000006");
        }

        public static final class User {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0014-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0014-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0014-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0014-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0014-000000000005");
        }

        public static final class UserGroup {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0015-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0015-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0015-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0015-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0015-000000000005");
        }

        public static final class DataList {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0016-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0016-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0016-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0016-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0016-000000000005");
        }

        public static final class DataListOption {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0017-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0017-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0017-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0017-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0017-000000000005");
        }

        public static final class DataListSubset {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0018-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0018-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0018-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0018-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0018-000000000005");
        }

        public static final class PermissionEntity {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0019-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0019-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0019-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0019-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0019-000000000005");
        }

        public static final class UserGroupInvolveAssignee {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0020-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0020-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0020-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0020-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0020-000000000005");
        }

        public static final class PermissionGrantSpaceRole {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0021-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0021-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0021-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0021-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0021-000000000005");
        }

        public static final class PermissionGrantTwinRole {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0022-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0022-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0022-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0022-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0022-000000000005");
        }

        public static final class PermissionGrantUser {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0023-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0023-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0023-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0023-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0023-000000000005");
        }

        public static final class PermissionGrantUserGroup {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0024-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0024-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0024-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0024-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0024-000000000005");
        }

        public static final class PermissionGroup {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0025-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0025-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0025-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0025-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0025-000000000005");
        }

        public static final class PermissionSchema {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0026-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0026-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0026-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0026-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0026-000000000005");
        }

        public static final class UserPermission {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0027-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0027-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0027-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0027-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0027-000000000005");
        }

        public static final class I18n {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0028-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0028-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0028-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0028-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0028-000000000005");
        }

        public static final class FactoryEraser {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0029-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0029-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0029-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0029-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0029-000000000005");
        }

        public static final class Factory {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0030-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0030-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0030-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0030-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0030-000000000005");
        }

        public static final class FactoryMultiplier {
            public static final UUID MANAGE       = UUID.fromString("00000000-0000-0004-0031-000000000001");
            public static final UUID CREATE       = UUID.fromString("00000000-0000-0004-0031-000000000002");
            public static final UUID VIEW         = UUID.fromString("00000000-0000-0004-0031-000000000003");
            public static final UUID UPDATE       = UUID.fromString("00000000-0000-0004-0031-000000000004");
            public static final UUID DELETE       = UUID.fromString("00000000-0000-0004-0031-000000000005");
            public static final UUID PARAM_MANAGE = UUID.fromString("00000000-0000-0004-0031-000000000006");
        }

        public static final class FactoryPipeline {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0032-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0032-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0032-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0032-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0032-000000000005");
        }

        public static final class FactoryConditionSet {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0033-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0033-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0033-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0033-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0033-000000000005");
        }

        public static final class FactoryBranch {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0034-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0034-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0034-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0034-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0034-000000000005");
        }

        public static final class Draft {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0035-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0035-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0035-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0035-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0035-000000000005");
            public static final UUID COMMIT = UUID.fromString("00000000-0000-0004-0035-000000000006");
        }

        public static final class DomainBusinessAccount {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0036-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0036-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0036-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0036-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0036-000000000005");
        }

        public static final class DomainUser {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0037-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0037-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0037-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0037-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0037-000000000005");
        }

        public static final class BusinessAccount {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0038-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0038-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0038-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0038-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0038-000000000005");
        }

        public static final class SpaceRole {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0039-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0039-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0039-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0039-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0039-000000000005");
        }

        public static final class Featurer {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0040-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0040-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0040-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0040-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0040-000000000005");
        }

        public static final class Tier {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0041-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0041-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0041-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0041-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0041-000000000005");
        }

        public static final class Face {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0042-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0042-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0042-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0042-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0042-000000000005");
        }

        public static final class FactoryPipelineStep {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0043-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0043-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0043-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0043-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0043-000000000005");
        }

        public static final class History {
            public static final UUID MANAGE            = UUID.fromString("00000000-0000-0004-0044-000000000001");
            public static final UUID CREATE            = UUID.fromString("00000000-0000-0004-0044-000000000002");
            public static final UUID VIEW              = UUID.fromString("00000000-0000-0004-0044-000000000003");
            public static final UUID UPDATE            = UUID.fromString("00000000-0000-0004-0044-000000000004");
            public static final UUID DELETE            = UUID.fromString("00000000-0000-0004-0044-000000000005");
            public static final UUID MACHINE_USER_VIEW = UUID.fromString("00000000-0000-0004-0044-000000000006");
        }

        public static final class Projection {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0045-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0045-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0045-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0045-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0045-000000000005");
        }

        public static final class ProjectionExclusion {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0046-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0046-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0046-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0046-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0046-000000000005");
        }

        public static final class TwinClassFieldRule {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0047-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0047-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0047-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0047-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0047-000000000005");
        }

        public static final class TwinflowFactory {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0048-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0048-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0048-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0048-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0048-000000000005");
        }

        public static final class TwinClassFreeze {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0049-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0049-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0049-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0049-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0049-000000000005");
        }

        public static final class HistoryNotification {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0050-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0050-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0050-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0050-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0050-000000000005");
        }

        public static final class Scheduler {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0051-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0051-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0051-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0051-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0051-000000000005");
        }

        public static final class TwinClassDynamicMarker {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0052-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0052-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0052-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0052-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0052-000000000005");
        }

        public static final class TwinValidatorSet {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0053-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0053-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0053-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0053-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0053-000000000005");
        }

        public static final class TwinTrigger {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0054-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0054-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0054-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0054-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0054-000000000005");
        }

        public static final class UserGroupInvolveActAsUser {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0055-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0055-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0055-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0055-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0055-000000000005");
        }

        public static final class ActionRestrictionReason {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0056-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0056-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0056-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0056-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0056-000000000005");
        }

        public static final class NotificationSchema {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0057-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0057-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0057-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0057-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0057-000000000005");
        }

        public static final class TwinPointer {
            public static final UUID MANAGE = UUID.fromString("00000000-0000-0004-0058-000000000001");
            public static final UUID CREATE = UUID.fromString("00000000-0000-0004-0058-000000000002");
            public static final UUID VIEW   = UUID.fromString("00000000-0000-0004-0058-000000000003");
            public static final UUID UPDATE = UUID.fromString("00000000-0000-0004-0058-000000000004");
            public static final UUID DELETE = UUID.fromString("00000000-0000-0004-0058-000000000005");
        }
    }

    public static final class Link {
        public static final UUID GLOSSARY_SEE_ALSO = UUID.fromString("00000000-0000-0000-0019-000000000001");
    }

    public static final class DataList {
        public static final UUID GLOSSARY_CATEGORY = UUID.fromString("00000000-0000-0000-0020-000000000001");
    }

    public static final class DataListOption {
        public static final UUID GLOSSARY_CATEGORY_CORE           = UUID.fromString("00000000-0000-0020-0001-000000000001");
        public static final UUID GLOSSARY_CATEGORY_WORKFLOW       = UUID.fromString("00000000-0000-0020-0001-000000000002");
        public static final UUID GLOSSARY_CATEGORY_MULTI_TENANCY  = UUID.fromString("00000000-0000-0020-0001-000000000003");
        public static final UUID GLOSSARY_CATEGORY_PERMISSIONS    = UUID.fromString("00000000-0000-0020-0001-000000000004");
        public static final UUID GLOSSARY_CATEGORY_CONTENT        = UUID.fromString("00000000-0000-0020-0001-000000000005");
        public static final UUID GLOSSARY_CATEGORY_CROSS_CUTTING  = UUID.fromString("00000000-0000-0020-0001-000000000006");
        public static final UUID GLOSSARY_CATEGORY_FIELDS         = UUID.fromString("00000000-0000-0020-0001-000000000007");
        public static final UUID GLOSSARY_CATEGORY_VALIDATION     = UUID.fromString("00000000-0000-0020-0001-000000000008");
        public static final UUID GLOSSARY_CATEGORY_OTHER          = UUID.fromString("00000000-0000-0020-0001-000000000009");
    }

    public static final class Featurer {
        public static final UUID TWIN_ATTACHMENT_EXTERNAL_URI_STORAGER = UUID.fromString("00000000-0000-0000-0013-000000000002");
    }

    public static final class I18n {
        public static final class UserField {
            public static final UUID EMAIL_NAME         = UUID.fromString("00000000-0000-0000-0012-000000000001");
            public static final UUID AVATAR_NAME        = UUID.fromString("00000000-0000-0000-0012-000000000002");
            public static final UUID EMAIL_DESCRIPTION  = UUID.fromString("00000000-0000-0000-0012-000000000012");
            public static final UUID AVATAR_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000013");
        }

        public static final class GlobalAncestorField {
            public static final UUID NAME_NAME                  = UUID.fromString("00000000-0000-0000-0012-000000000003");
            public static final UUID DESCRIPTION_NAME           = UUID.fromString("00000000-0000-0000-0012-000000000004");
            public static final UUID EXTERNAL_ID_NAME           = UUID.fromString("00000000-0000-0000-0012-000000000005");
            public static final UUID OWNER_USER_NAME            = UUID.fromString("00000000-0000-0000-0012-000000000006");
            public static final UUID ASSIGNEE_NAME              = UUID.fromString("00000000-0000-0000-0012-000000000007");
            public static final UUID CREATOR_NAME               = UUID.fromString("00000000-0000-0000-0012-000000000008");
            public static final UUID HEAD_NAME                  = UUID.fromString("00000000-0000-0000-0012-000000000009");
            public static final UUID STATUS_NAME                = UUID.fromString("00000000-0000-0000-0012-000000000010");
            public static final UUID CREATED_AT_NAME            = UUID.fromString("00000000-0000-0000-0012-000000000011");
            public static final UUID ID_NAME                    = UUID.fromString("00000000-0000-0000-0012-000000000027");
            public static final UUID TWIN_CLASS_ID_NAME         = UUID.fromString("00000000-0000-0000-0012-000000000028");
            public static final UUID ALIASES_NAME               = UUID.fromString("00000000-0000-0000-0012-000000000029");
            public static final UUID TAGS_NAME                  = UUID.fromString("00000000-0000-0000-0012-000000000030");
            public static final UUID MARKERS_NAME               = UUID.fromString("00000000-0000-0000-0012-000000000031");
            public static final UUID FLAVOR_NAME                = UUID.fromString("00000000-0000-0000-0012-000000000053");

            public static final UUID NAME_DESCRIPTION           = UUID.fromString("00000000-0000-0000-0012-000000000014");
            public static final UUID DESCRIPTION_DESCRIPTION    = UUID.fromString("00000000-0000-0000-0012-000000000015");
            public static final UUID EXTERNAL_ID_DESCRIPTION    = UUID.fromString("00000000-0000-0000-0012-000000000016");
            public static final UUID OWNER_USER_DESCRIPTION     = UUID.fromString("00000000-0000-0000-0012-000000000017");
            public static final UUID ASSIGNEE_DESCRIPTION       = UUID.fromString("00000000-0000-0000-0012-000000000018");
            public static final UUID CREATOR_DESCRIPTION        = UUID.fromString("00000000-0000-0000-0012-000000000019");
            public static final UUID HEAD_DESCRIPTION           = UUID.fromString("00000000-0000-0000-0012-000000000020");
            public static final UUID STATUS_DESCRIPTION         = UUID.fromString("00000000-0000-0000-0012-000000000021");
            public static final UUID CREATED_AT_DESCRIPTION     = UUID.fromString("00000000-0000-0000-0012-000000000022");
            public static final UUID ID_DESCRIPTION             = UUID.fromString("00000000-0000-0000-0012-000000000032");
            public static final UUID TWIN_CLASS_ID_DESCRIPTION  = UUID.fromString("00000000-0000-0000-0012-000000000033");
            public static final UUID ALIASES_DESCRIPTION        = UUID.fromString("00000000-0000-0000-0012-000000000034");
            public static final UUID TAGS_DESCRIPTION           = UUID.fromString("00000000-0000-0000-0012-000000000035");
            public static final UUID MARKERS_DESCRIPTION        = UUID.fromString("00000000-0000-0000-0012-000000000036");
            public static final UUID FLAVOR_DESCRIPTION         = UUID.fromString("00000000-0000-0000-0012-000000000054");
        }

        public static final class UserStatus {
            public static final UUID NAME        = UUID.fromString("00000000-0000-0000-0012-000000000023");
            public static final UUID DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000025");
        }

        public static final class BusinessAccountStatus {
            public static final UUID NAME        = UUID.fromString("00000000-0000-0000-0012-000000000024");
            public static final UUID DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000026");
        }

        public static final class FacePageStatus {
            public static final UUID NAME        = UUID.fromString("00000000-0000-0000-0012-000000000037");
            public static final UUID DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000038");
        }

        public static final class GlossaryStatus {
            public static final UUID INIT_NAME           = UUID.fromString("00000000-0000-0000-0012-000000000047");
            public static final UUID INIT_DESCRIPTION    = UUID.fromString("00000000-0000-0000-0012-000000000048");
            public static final UUID DELETED_NAME        = UUID.fromString("00000000-0000-0000-0012-000000000049");
            public static final UUID DELETED_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000050");
        }

        public static final class GlossaryLink {
            public static final UUID SEE_ALSO_FORWARD  = UUID.fromString("00000000-0000-0000-0012-000000000051");
            public static final UUID SEE_ALSO_BACKWARD = UUID.fromString("00000000-0000-0000-0012-000000000052");
        }

        public static final class GlossaryDataList {
            public static final UUID CATEGORY_NAME        = UUID.fromString("00000000-0000-0000-0012-000000000060");
            public static final UUID CATEGORY_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000061");
        }
    }
}
