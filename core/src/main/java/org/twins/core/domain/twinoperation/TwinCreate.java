package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.enums.twin.TwinCreateStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinCreate extends TwinSave {
    private List<TwinAttachmentEntity> attachmentEntityList;
    private List<TwinLinkEntity> linksEntityList;
    private List<TwinFieldAttributeEntity> twinFieldAttributeEntityList;
    private boolean checkCreatePermission = false;
    private Boolean sketchMode; // this flag will be set after processing createStrategy
    private TwinCreateStrategy createStrategy = TwinCreateStrategy.STRICT;

    // TemporalId support fields
    private String temporalId; // for tracking during batch creation
    private String headTwinRef; // original headTwinId reference for later resolution
    private Map<String, String> fieldRefs; // field temporalId references for later resolution
    private List<LinkRef> linksRefList; // original link references for later resolution

    // Helper class for storing unresolved link references
    @Data
    @Accessors(chain = true)
    public static class LinkRef {
        private UUID linkId;
        private String dstTwinIdRef; // can be "temporalId:XXX" or UUID string
    }

    public TwinCreate addLink(TwinLinkEntity link) {
        linksEntityList = CollectionUtils.safeAdd(linksEntityList, link);
        return this;
    }

    public TwinCreate addAttachment(TwinAttachmentEntity attachment) {
        attachmentEntityList = CollectionUtils.safeAdd(attachmentEntityList, attachment);
        return this;
    }

    public TwinCreate addLinkRef(LinkRef linkRef) {
        if (linksRefList == null) {
            linksRefList = new ArrayList<>();
        }
        linksRefList.add(linkRef);
        return this;
    }

    // Explicit getters/setters for fieldRefs to avoid Lombok issues
    public Map<String, String> getFieldRefs() {
        return fieldRefs;
    }

    public TwinCreate setFieldRefs(Map<String, String> fieldRefs) {
        this.fieldRefs = fieldRefs;
        return this;
    }

    // Explicit getters/setters for linksRefList to avoid Lombok issues
    public List<LinkRef> getLinksRefList() {
        return linksRefList;
    }

    public TwinCreate setLinksRefList(List<LinkRef> linksRefList) {
        this.linksRefList = linksRefList;
        return this;
    }

    @Override
    public UUID nullifyUUID() {
        return null;
    }
}
