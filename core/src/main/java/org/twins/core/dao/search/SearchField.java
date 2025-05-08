package org.twins.core.dao.search;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SearchField {
    twinId("twinId"),
    twinNameLike("twinNameLike"),
    twinDescriptionLike("twinDescriptionLike***"),
    twinClassId("twinClassId"),
    headTwinId("headTwinId"),
    statusId("statusId"),
    assigneeUserId("assigneeUserId"),
    createdByUserId("createdByUserId"),
    linkId("linkId"),
    tagDataListOptionId("tagDataListOptionId"),
    markerDataListOptionId("markerDataListOptionId"),
    hierarchyTreeContainsId("hierarchyTreeContainsId"),
    fieldNumeric("fieldNumeric"),
    fieldDate("fieldDate"),
    fieldId("fieldId"),
    fieldText("fieldText"),
    fieldList("fieldList");



    private final String id;

    SearchField(String id) {
        this.id = id;
    }
    public static SearchField valueOd(String field) {
        return Arrays.stream(SearchField.values()).filter(t -> t.id.equals(field)).findAny().orElse(null);
    }
}
