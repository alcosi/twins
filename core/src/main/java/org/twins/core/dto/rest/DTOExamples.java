package org.twins.core.dto.rest;

public interface DTOExamples {
    String TERNARY = "ANY";
    String BOOLEAN_TRUE = "true";
    String NAME = "Some name";
    String DESCRIPTION = "Some description";
    String COUNT = "3";
    String UUID_COLLECTION = "[\"11a4c3f8-c780-4421-9aee-9e7eec20c67d\", \"83050c4b-ef17-4d7d-8792-6e5a86557ab9\"]";
    String STRING_COLLECTION = "[\"String 1\", \"String 2\"]";
    String TRANSLATION = "translation";
    String TRANSLATION_MAP = "{\"en\":\"translation\",\n\"pl\":\"tłumaczenie\",\n\"ru\":\"перевод\"}";
    String TWIN_CLASS_ID = "458c6d7d-99c8-4d87-89c6-2f72d0f5d673";
    String TWIN_CLASS_KEY = "TOOL";
    String TWIN_CLASS_NAME = "Tool";
    String TWIN_CLASS_DESCRIPTION = "Professional tool class";
    String TWIN_CLASS_HEAD_CLASS_ID = "c2854a55-1dfe-41cd-bb36-f71eeaf16f81";
    String TWIN_STATUS_ID = "a1178c4a-b974-449b-b51b-9a2bc54c5ea5";
    String TWIN_STATUS_KEY = "toDo";
    String TWIN_STATUS_NAME = "To Do";
    String TWIN_STATUS_DESCRIPTION = "Need to be done";
    String TWIN_TOUCH = "WATCHED";
    String HEAD_TWIN_ID = "5d956a15-6858-40ba-b0aa-b123c54e250d";
    String TWIN_CLASS_FIELD_ID = "2fe95272-afcb-40ee-a6a8-87c5da4d5b8d";
    String TWIN_CLASS_FIELD_KEY = "serialNumber";
    String TWIN_CLASS_FIELD_NAME = "Serial number";
    String TWIN_CLASS_FIELD_DESCRIPTION = "Manufacture serial number";
    String TWIN_CLASS_FIELD_SHARED_IN_HEAD_ID = "2fe95272-afcb-40ee-a6a8-87c5da4d5b8d";
    String TWIN_CLASS_OWNER_TYPE = "SYSTEM";
    String WIDGET_ID = "4245e338-3c09-4390-8a03-435d1da4e311";
    String TWIN_ID = "1b2091e3-971a-41bc-b343-1f980227d02f";
    String TWIN_COMMENT_ID = "be44e826-ce24-4881-a227-f3f72d915a20";
    String TWIN_HISTORY_ID = "1b2091e3-971a-41bc-b343-1f980227d02f";
    String TWIN_NAME = "Oak";
    String TWIN_ALIAS = "LESNAYA9-2";
    String TWIN_FIELD_ID = "cf8b1aec-c07c-4131-b834-8024462cfc93";
    String TWIN_TAG_ID = "cf8b1aec-c07c-4131-b834-8024462cfc93";
    String TWIN_FIELD_KEY = "brand";
    String TWIN_FIELD_VALUE = "alcosi";
    String DATA_LIST_ID = "e844a4e5-1c09-474e-816f-05cdb1f093ed";
    String DATA_LIST_KEY = "country";
    String DATA_LIST_OPTION_ID = "7de977d4-df6d-4250-9cb2-088363d139a1";
    String DATA_LIST_OPTION_STATUS = "active";
    String DATA_LIST_ATTRIBUTE_KEY = "color";
    String USER_ID = "608c6d7d-99c8-4d87-89c6-2f72d0f5d673";
    String USER_GROUP_ID = "e155e05b-f353-49ff-9869-da1e62aab1793";
    String USER_GROUP_NAME = "Manager";
    String USER_GROUP_TYPE = "domainScopeDomainManage";
    String DOMAIN_ID = "f67ad556-dd27-4871-9a00-16fb0e8a4102";
    String DOMAIN_KEY = "alcosi";
    String DOMAIN_TYPE = "basic";
    String DOMAIN_DESCRIPTION = "some domain";
    String BUSINESS_ACCOUNT_ID = "9a3f6075-f175-41cd-a804-934201ec969c";
    String BUSINESS_ACCOUNT_NAME = "Business account name";
    String AUTH_TOKEN = USER_ID + "," + BUSINESS_ACCOUNT_ID;
    String PERMISSION_SCHEMA_ID = "af143656-9899-4e1f-8683-48795cdefeac";
    String PERMISSION_GRANT_USER_ID = "9e8641f2-dda1-4a43-9a23-8786124cdb6b";
    String PERMISSION_GRANT_ASSIGNEE_PROPAGATION_ID = "a7485d77-16bc-440e-a88e-1a576954a839";
    String TWINFLOW_SCHEMA_ID = "2c618b09-e8dc-4712-a433-2e18915ee70d";
    String TWINFLOW_ID = "34618b09-e8dc-4712-a433-2e18915ee70d";
    String DRAFT_ID = "34618b09-e8dc-4712-a433-2e18915ee70d";
    String TWINFLOW_NAME = "Default twinflow";
    String TWINFLOW_TRANSITION_ID = "f6606fa2-c047-4ba9-a92c-84051df681ab";
    String TWINFLOW_TRANSITION_ALIAS = "start";
    String TWIN_CLASS_SCHEMA_ID = "8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b";
    String CHANNEL = "WEB";
    String ATTACHMENT_STORAGE_LINK = "https://test.filestorage.by/JFUjEFWksfqwf";
    String ATTACHMENT_EXTERNAL_ID = "JD999weqw9f";
    String ATTACHMENT_TITLE = "cert.pdf";
    String ATTACHMENT_DESCRIPTION = "fresh certificate";
    String ATTACHMENT_ID = "553ef9bc-3b48-430d-90d3-bdee516c3d87";
    String PERMISSION_ID = "abdeef68-7d6d-4385-9906-e3b701d2c503";
    String PERMISSION_KEY = "DENY_ALL";
    String PERMISSION_GROUP_ID = "7efd9df0-cae7-455f-a721-eaec455105a4";
    String PERMISSION_GROUP_KEY = "LOCAL_PERMISSION";
    String PERMISSION_GRANT_USER_GROUP_ID = "12fd2df0-cae7-455f-a721-eaec415105a4";
    String PERMISSION_GRANT_TWIN_ROLE_ID = "22fd2df0-cae7-455f-a721-eaec415105a4";
    String INSTANT = "2023-09-13T09:32:08";
    String LINK_ID = "f6606fa2-c047-4ba9-a92c-84051df681ab";
    String ROLE_ID = "793e3120-e14a-4a22-ab09-060b9fedee35";
    String LOCALE = "en";
    String LAZY_RELATION_MODE_ON = " Will be filled only if lazyRelations mode is true";
    String LAZY_RELATION_MODE_OFF = " Will be filled only in lazyRelations mode is false";
    String SPACE_ID = "5d956a15-6858-40ba-b0aa-b123c54e250d";
    String SPACE_ROLE = "creator";
    String SPACE_ROLE_USER_ID = "275bf3c4-951a-4d26-bb82-5e18361d301c";
    String SEARCH_ALIAS = "tools";
    String SEARCH_ID = "8c580967-c050-47cf-ac27-4096c6dda2d1";
    String FEATURER_ID = "1000";
    String FEATURER_NAME = "InjectorImpl";
    String FEATURER_PARAM_NAME = "listUUID";
    String FEATURER_PARAM = "{\"linkId\"=>\"6e42ef74-3015-4400-946e-1326bcb4cf48\",\n\"GTEvalue\"=>\"2\"}";
    String COLOR_HEX = "#ff00ff";
    String FACTORY_ID = "5d956a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_ERASER_ID = "47991b35-e9fb-454e-a9b1-d715b2e6c71e";
    String FACTORY_CONDITION_SET_ID = "69856a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_KEY = "taskReassign";
    String TRIGGER_ID = "9d956a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_PIPELINE_ID = "5d956a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_PIPELINE_STEP_ID = "99856a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_PARAMS_MAP = "{\"outputTwinClassId\"=>\"da69c441-9c8f-4e73-a07e-b5648f8f4396\",\n\"copyHead\"=>\"true\"}";
    String MULTIPLIER_ID = "66956a15-6858-40ba-b0aa-b123c54e250d";
    String FACTORY_BRANCH_ID = "99956a15-6858-40ba-b0aa-b123c54e250d";
    String ERASER_ACTION = "NOT_SPECIFIED";
    String SIMPLE_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII";
    String RESOURCE_STORAGE_ID = "00000000-0000-0000-0007-000000000001";
    String RESOURCE_ID = "09cd9a50-dcbe-4c73-b39e-65d2000a8e85";



}
