package org.cambium.common.file;

import java.io.InputStream;

public record FileData (
        InputStream content,
        String originalFileName,
        Long fileSize
) {
}
