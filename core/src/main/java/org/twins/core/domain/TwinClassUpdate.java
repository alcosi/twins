package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassUpdate {
//        private TwinClassEntity updateTwinClassEntity;
    private String key;
    private Boolean permissionSchemaSpace;
    private Boolean twinflowSchemaSpace;
    private Boolean twinClassSchemaSpace;
    private Boolean aliasSpace;
    private UUID viewPermissionId;
    private Boolean abstractt;
    private String logo;
    private Integer headHunterFeaturerId;
    private HashMap<String, String> headHunterParams;

    private TwinClassEntity dbTwinClassEntity;
    private EntityRelinkOperation markerDataListUpdate;
    private EntityRelinkOperation tagDataListUpdate;
    private EntityRelinkOperation extendsTwinClassUpdate;
    private EntityRelinkOperation headTwinClassUpdate;
}
