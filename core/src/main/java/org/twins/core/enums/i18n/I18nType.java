package org.twins.core.enums.i18n;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum I18nType  {
    TWIN_CLASS_NAME("twinClassName", "Twin class name", Category.TEXT),
    TWIN_CLASS_DESCRIPTION("twinClassDescription", "Twin class description", Category.TEXT),
    TWIN_CLASS_FREEZE_NAME("twinClassFreezeName", "Twin class freeze name", Category.TEXT),
    TWIN_CLASS_FREEZE_DESCRIPTION("twinClassFreezeDescription", "Twin class freeze description", Category.TEXT),
    TWIN_STATUS_NAME("twinStatusName", "Twin status name", Category.TEXT),
    TWIN_STATUS_DESCRIPTION("twinStatusDescription", "Twin status description", Category.TEXT),
    TWIN_CLASS_FIELD_NAME("twinClassFieldName", "Twin class field name", Category.TEXT),
    TWIN_CLASS_FIELD_DESCRIPTION("twinClassFieldDescription", "Twin class field description", Category.TEXT),
    TWIN_CLASS_FIELD_FE_VALIDATION_ERROR("twinClassFieldFeValidationError", "Twin class field FE validation error", Category.TEXT),
    TWIN_CLASS_FIELD_BE_VALIDATION_ERROR("twinClassFieldBeValidationError", "Twin class field BE validation error", Category.TEXT),
    TWIN_CLASS_OWNER_TYPE_NAME("twinClassOwnerTypeName", "Twin class owner type name", Category.TEXT),
    TWIN_CLASS_OWNER_TYPE_DESCRIPTION("twinClassOwnerTypeDescription", "Twin class owner type description", Category.TEXT),
    CARD_NAME("cardName", "Twin card name", Category.TEXT),
    UNKNOWN("unknown", "Unknown", Category.TEXT),
    LINK_FORWARD_NAME("linkForwardName", "Twin link forward name", Category.TEXT),
    LINK_BACKWARD_NAME("linkBackwardName", "Twin link backward name", Category.TEXT),
    TWINFLOW_NAME("twinflowName", "Twinflow name", Category.TEXT),
    TWINFLOW_DESCRIPTION("twinflowDescription", "Twinflow description", Category.TEXT),
    TWINFLOW_MESSAGE("twinflowMessage", "Twinflow message", Category.TEXT),
    TWINFLOW_TRANSITION_NAME("twinflowTransitionName", "Twinflow transition name", Category.TEXT),
    TWINFLOW_TRANSITION_DESCRIPTION("twinflowTransitionDescription", "Twinflow transition description", Category.TEXT),
    TWINFLOW_TRANSITION_MESSAGE("twinflowTransitionMessage", "Twinflow transition message", Category.TEXT),
    SPACE_ROLE_NAME("spaceRoleName", "Space role name", Category.TEXT),
    PERMISSION_NAME("permissionName", "Permission name", Category.TEXT),
    PERMISSION_DESCRIPTION("permissionDescription", "Permission description", Category.TEXT),
    TWIN_FACTORY_NAME("twinFactoryName", "Twin factory name", Category.TEXT),
    TWIN_FACTORY_DESCRIPTION("twinFactoryDescription", "Twin factory description", Category.TEXT),
    DATA_LIST_NAME("dataListName", "Data list name", Category.TEXT),
    DATA_LIST_DESCRIPTION("dataListDescription", "Data list description", Category.TEXT),
    DATA_LIST_OPTION_VALUE("dataListOptionValue", "Data list option value", Category.TEXT),
    DATA_LIST_OPTION_DESCRIPTION("dataListOptionDescription", "Data list option description", Category.TEXT),
    DATA_LIST_ATTRIBUTE_NAME("dataListAttributeName", "Data list attribute name", Category.TEXT),
    USER_GROUP_NAME("userGroupName", "User group name", Category.TEXT),
    USER_GROUP_DESCRIPTION("userGroupDescription", "User group description", Category.TEXT),
    FACE_ELEMENT("faceElement", "Face element", Category.TEXT),
    ERROR("error","Error", Category.TEXT),
    NOTIFICATION_EMAIL_SUBJECT("notificationEmailSubject","notification email subject", Category.TEXT),
    NOTIFICATION_EMAIL_BODY("notificationEmailBody","notification email body", Category.TEXT);

    private final String id;
    private final String description;
    private final Category category;

    I18nType(String id, String description, Category category) {
        this.id = id;
        this.description = description;
        this.category = category;
    }

    public static I18nType valueOd(String type) {
        return Arrays.stream(I18nType.values()).filter(t -> t.id.equals(type)).findAny().orElse(UNKNOWN);
    }

    public boolean isText() {
        return category == Category.TEXT || category == Category.STYLED_TEXT;
    }

    public boolean isStyledText() {
        return category == Category.STYLED_TEXT;
    }

    public boolean isImage() {
        return category == Category.IMAGE;
    }

    public boolean isExternalId() {
        return category == Category.EXTERNAL_ID;
    }

    public static List<String> getAllDescriptions() {
        return Arrays.stream(I18nType.values()).map(v -> v.description).collect(Collectors.toList());
    }

    public static I18nType getByDescription(String description) {
        return Arrays.stream(I18nType.values()).filter(e -> e.description.contains(description)).findAny().orElseThrow(NullPointerException::new);
    }

    public static List<String> getTextTypesIds() {
        return Arrays.stream(I18nType.values())
                .filter(type -> type.category.equals(Category.TEXT) || type.category.equals(Category.STYLED_TEXT))
                .map(type -> type.id)
                .collect(Collectors.toList());
    }

    public static List<String> getImageTypesIds() {
        return Arrays.stream(I18nType.values())
                .filter(type -> type.category.equals(Category.IMAGE))
                .map(type -> type.id)
                .collect(Collectors.toList());
    }

    public static List<String> getExternalIdTypesIds() {
        return Arrays.stream(I18nType.values())
                .filter(type -> type.category.equals(Category.EXTERNAL_ID))
                .map(type -> type.id)
                .collect(Collectors.toList());
    }

    public enum Category {
        TEXT, STYLED_TEXT, EXTERNAL_ID, IMAGE;
    }
}
