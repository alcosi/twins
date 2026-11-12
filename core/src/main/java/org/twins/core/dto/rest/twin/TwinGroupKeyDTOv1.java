package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.Map;
import java.util.UUID;

public abstract class TwinGroupKeyDTOv1 {
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupAssignerKeyV1")
    public static class Assigner extends TwinGroupKeyDTOv1 {
        @Schema(description = "Assigner user id. Null is the group of twins without an assignee", example = DTOExamples.UUID_ID)
        @RelatedObject(type = UserDTOv1.class, name = "assignerUser")
        public UUID assignerUserId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupOwnerKeyV1")
    public static class Owner extends TwinGroupKeyDTOv1 {
        @Schema(description = "Owner user id. Null is the group of twins without an owner", example = DTOExamples.UUID_ID)
        @RelatedObject(type = UserDTOv1.class, name = "ownerUser")
        public UUID ownerUserId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupCreatorKeyV1")
    public static class Creator extends TwinGroupKeyDTOv1 {
        @Schema(description = "Created-by user id. Null is the group of twins without a creator", example = DTOExamples.UUID_ID)
        @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
        public UUID createdByUserId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupStatusKeyV1")
    public static class Status extends TwinGroupKeyDTOv1 {
        @Schema(description = "Twin status id. Null is the group of twins without a status", example = DTOExamples.UUID_ID)
        @RelatedObject(type = TwinStatusDTOv1.class, name = "twinStatus")
        public UUID twinStatusId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupClassKeyV1")
    public static class TwinClass extends TwinGroupKeyDTOv1 {
        @Schema(description = "Twin class id", example = DTOExamples.UUID_ID)
        @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
        public UUID twinClassId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupHeadKeyV1")
    public static class Head extends TwinGroupKeyDTOv1 {
        @Schema(description = "Head twin id. Null is the group of twins without a head", example = DTOExamples.UUID_ID)
        @RelatedObject(type = TwinDTOv2.class, name = "headTwin")
        public UUID headTwinId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupFlavorKeyV1")
    public static class Flavor extends TwinGroupKeyDTOv1 {
        @Schema(description = "Flavor data list option id. Null is the group of twins without a flavor", example = DTOExamples.UUID_ID)
        @RelatedObject(type = DataListOptionDTOv1.class, name = "flavorDataListOption")
        public UUID flavorDataListOptionId;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = false)
    @JsonInclude(value = JsonInclude.Include.ALWAYS, content = JsonInclude.Include.ALWAYS)
    @Schema(name = "TwinGroupDynamicKeyV1")
    public static class Dynamic extends TwinGroupKeyDTOv1 {
        @Schema(description = "Dynamic field value this group is keyed by: twin class field id -> storage value. A null value is the group of twins without that field")
        public Map<UUID, Object> groupedFieldValues;

        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        public Map<UUID, Object> getGroupedFieldValues() {
            return groupedFieldValues;
        }
    }
}
