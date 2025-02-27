package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dao.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassUpdate {
//        private TwinClassEntity updateTwinClassEntity;
    private String key;
    private I18nEntity nameI18n;
    private I18nEntity descriptionI18n;
    private Boolean permissionSchemaSpace;
    private Boolean twinflowSchemaSpace;
    private Boolean twinClassSchemaSpace;
    private Boolean aliasSpace;
    private UUID viewPermissionId;
    private UUID editPermissionId;
    private UUID createPermissionId;
    private UUID deletePermissionId;
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
