package org.twins.core.domain.resource;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.InputStream;

/**
 * One file to store via {@link org.twins.core.service.resource.ResourceUploadService}.
 * Either content (multipart bytes stream) or externalUri (link to download) must be set, not both.
 */
@Data
@Accessors(chain = true)
public class ResourceUploadItem {
    private String fileName;
    private InputStream content;
    private String externalUri;
}
