package org.twins.core.dao.history;

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

    fieldChanged("fieldChanged"),
    markerChanged("markerChanged"),
    tagChanged("tagChanged"),
    attachmentCreate("attachmentCreate"),
    attachmentDelete("attachmentDelete"),
    attachmentUpdate("attachmentUpdate"),
    linkCreate("linkCreate"),
    linkUpdate("linkUpdate"),
    linkDelete("linkDelete"),
    twinDeleted("twinDeleted"),
    unknown("unknown");

    final String id;

    public static HistoryType valueOd(String type) {
        return Arrays.stream(HistoryType.values()).filter(t -> t.id.equals(type)).findAny().orElse(unknown);
    }
}
