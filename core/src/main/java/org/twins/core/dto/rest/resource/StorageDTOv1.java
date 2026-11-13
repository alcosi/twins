package org.twins.core.dto.rest.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "StorageV1")
public class StorageDTOv1 {
    @Schema(description = "id", example = DTOExamples.RESOURCE_STORAGE_ID)
    public UUID id;

    @Schema(description = "description")
    public String description;

    @Schema(description = "storager featurer id", example = DTOExamples.FEATURER_ID)
    @RelatedObject(type = FeaturerDTOv1.class, name = "storageFeaturer")
    public Integer storageFeaturerId;

    @Schema(description = "storager params")
    public Map<String, String> storagerParams;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "updated at", example = DTOExamples.INSTANT)
    public LocalDateTime updatedAt;
}
