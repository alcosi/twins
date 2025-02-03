package org.twins.core.domain.file;

import java.io.InputStream;

public record DomainFile(
        InputStream content,
        String originalFileName,
        Long fileSize
) {
}