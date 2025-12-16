package org.twins.core.enums.history;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum HistoryType {
    twinCreated("twinCreated"),
    headChanged("headChanged"),
    statusChanged("statusChanged"),
    nameChanged("nameChanged"),
    descriptionChanged("descriptionChanged"),
    createdByChanged("createdByChanged"),
    assigneeChanged("assigneeChanged"),
    ownerChanged("ownerChanged"),
    externalIdChanged("externalIdChanged"),

    fieldCreated("fieldCreated"),
    fieldChanged("fieldChanged"),
    fieldDeleted("fieldDeleted"),
    markerChanged("markerChanged"),
    tagChanged("tagChanged"),
    attachmentCreate("attachmentCreate"),
    attachmentDelete("attachmentDelete"),
    attachmentUpdate("attachmentUpdate"),
    commentCreate("commentCreate"),
    permissionSchemaChanged("permissionSchemaChanged"),
    linkCreated("linkCreated"), // 2 history records will be created
    linkUpdated("linkUpdated"), // 3 history records will be created
    linkDeleted("linkDeleted"), // 2 history records will be created
    twinDeleted("twinDeleted"),
    unknown("unknown");

    final String id;

    public static HistoryType valueOd(String type) {
        return Arrays.stream(HistoryType.values()).filter(t -> t.id.equals(type)).findAny().orElse(unknown);
    }
}
