package org.twins.core.domain.resource;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.file.FileData;

/**
 * Batch create item for resource. The content source is either a resolved multipart
 * file ({@link #file}) or an external uri ({@link #externalResourceUri}).
 */
@Data
@Accessors(chain = true)
public class ResourceCreate {
    private String title;
    private Boolean isPublic;
    private FileData file;
    private String externalResourceUri;
}
