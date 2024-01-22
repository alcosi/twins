package org.twins.core.dto.rest.history;

public enum HistoryType {
    twinCreated,
    headChanged,
    statusChanged,
    nameChanged,
    descriptionChanged,
    createdByChanged,
    assigneeChanged,

    fieldChanged, //todo what field?
    markerChanged,
    tagChanged,
    attachmentCreate,
    attachmentDelete,
    attachmentUpdate,
    linkCreate,
    linkUpdate,
    linkDelete,
    twinDeleted
}
