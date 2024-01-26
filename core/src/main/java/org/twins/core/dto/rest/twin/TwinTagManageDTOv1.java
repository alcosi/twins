package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinTagManageV1")
public class TwinTagManageDTOv1  extends TwinTagAddDTOv1{
    @Schema(description = "delete already existing tags by their ids",  example = DTOExamples.TWIN_TAG_ID)
    public Set<UUID> deleteTags;

}
