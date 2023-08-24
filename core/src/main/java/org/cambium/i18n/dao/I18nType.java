package org.cambium.i18n.dao;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum I18nType  {
    UNKNOWN("unknown", "Unknown", Category.TEXT),
    TWIN_STATUS_NAME("twinStatusName", "Twin status name", Category.TEXT),
    TWIN_STATUS_DESCRIPTION("twinStatusDescription", "Twin status name", Category.TEXT)
        ;

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
