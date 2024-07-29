package org.twins.core.domain.draft;

import lombok.Getter;

@Getter
public class DraftCounters {
    private int twinEraseCount = 0;
    private int twinEraseIrrevocableCount = 0;
    private int twinPersistCount = 0;
    private int twinLinkCreateCount = 0;
    private int twinLinkUpdateCount = 0;
    private int twinLinkDeleteCount = 0;
    private int twinAttachmentCreateCount = 0;
    private int twinAttachmentUpdateCount = 0;
    private int twinAttachmentDeleteCount = 0;
    private int twinMarkerCreateCount = 0;
    private int twinMarkerDeleteCount = 0;
    private int twinTagCreateCount = 0;
    private int twinTagDeleteCount = 0;
    private int twinTwinFieldSimpleCreateCount = 0;
    private int twinTwinFieldSimpleUpdateCount = 0;
    private int twinTwinFieldSimpleDeleteCount = 0;
    private int twinTwinFieldUserCreateCount = 0;
    private int twinTwinFieldUserUpdateCount = 0;
    private int twinTwinFieldUserDeleteCount = 0;
    private int twinTwinFieldDataListCreateCount = 0;
    private int twinTwinFieldDataListUpdateCount = 0;
    private int twinTwinFieldDataListDeleteCount = 0;

    public void incrementTwinErase() {
        twinEraseCount++;
    }

    public void incrementTwinEraseIrrevocable() {
        twinEraseIrrevocableCount++;
    }

    public void incrementTwinPersist() {
        twinPersistCount++;
    }

    public void incrementTwinLinkCreate() {
        twinLinkCreateCount++;
    }

    public void incrementTwinLinkUpdate() {
        twinLinkUpdateCount++;
    }

    public void incrementTwinLinkDelete() {
        twinLinkDeleteCount++;
    }

    public void incrementTwinAttachmentCreate() {
        twinAttachmentCreateCount++;
    }

    public void incrementTwinAttachmentUpdate() {
        twinAttachmentUpdateCount++;
    }

    public void incrementTwinAttachmentDelete() {
        twinAttachmentDeleteCount++;
    }

    public void incrementTwinMarkerCreate() {
        twinMarkerCreateCount++;
    }

    public void incrementTwinMarkerDelete() {
        twinMarkerDeleteCount++;
    }

    public void incrementTwinTagCreate() {
        twinTagCreateCount++;
    }

    public void incrementTwinTagDelete() {
        twinTagDeleteCount++;
    }

    public void incrementTwinTwinFieldSimpleCreate() {
        twinTwinFieldSimpleCreateCount++;
    }

    public void incrementTwinTwinFieldSimpleUpdate() {
        twinTwinFieldSimpleUpdateCount++;
    }

    public void incrementTwinTwinFieldSimpleDelete() {
        twinTwinFieldSimpleDeleteCount++;
    }

    public void incrementTwinTwinFieldUserCreate() {
        twinTwinFieldUserCreateCount++;
    }

    public void incrementTwinTwinFieldUserUpdate() {
        twinTwinFieldUserUpdateCount++;
    }

    public void incrementTwinTwinFieldUserDelete() {
        twinTwinFieldUserDeleteCount++;
    }

    public void incrementTwinTwinFieldDataListCreate() {
        twinTwinFieldDataListCreateCount++;
    }

    public void incrementTwinTwinFieldDataListUpdate() {
        twinTwinFieldDataListUpdateCount++;
    }

    public void incrementTwinTwinFieldDataListDelete() {
        twinTwinFieldDataListDeleteCount++;
    }

    public int getTwinLinkCount() {
        return twinLinkCreateCount + twinLinkUpdateCount + twinLinkDeleteCount;
    }

    public int getTwinAttachmentCount() {
        return twinAttachmentCreateCount + twinAttachmentUpdateCount + twinAttachmentDeleteCount;
    }

    public int getTwinMarkerCount() {
        return twinMarkerCreateCount + twinMarkerDeleteCount;
    }

    public int getTwinTagCount() {
        return twinTagCreateCount + twinTagDeleteCount;
    }

    public int getTwinTwinFieldSimpleCount() {
        return twinTwinFieldSimpleCreateCount + twinTwinFieldSimpleUpdateCount + twinTwinFieldSimpleDeleteCount;
    }

    public int getTwinTwinFieldDataListCount() {
        return twinTwinFieldDataListCreateCount + twinTwinFieldDataListUpdateCount + twinTwinFieldDataListDeleteCount;
    }

    public int getTwinTwinFieldUserCount() {
        return twinTwinFieldUserCreateCount + twinTwinFieldUserUpdateCount + twinTwinFieldUserDeleteCount;
    }
}
