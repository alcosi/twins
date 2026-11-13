package org.twins.core.domain.resource;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.file.FileData;

import java.util.UUID;

/**
 * Batch update item for resource. Null fields keep the current entity values.
 * File replacement is carried either as a resolved multipart file ({@link #file})
 * or as an external uri ({@link #externalResourceUri}).
 */
@Data
@Accessors(chain = true)
public class ResourceUpdate {
    private UUID id;
    private String originalFileName;
    private Boolean isPublic;
    private FileData file;
    private String externalResourceUri;
}
