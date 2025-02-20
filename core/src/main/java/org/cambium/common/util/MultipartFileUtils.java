package org.cambium.common.util;

import org.springframework.web.multipart.MultipartFile;
import org.twins.core.domain.file.DomainFile;

import java.io.IOException;

public class MultipartFileUtils {
    public static DomainFile convert(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) return null;
        var inputStream = multipartFile.getInputStream();
        var originalFileName = multipartFile.getOriginalFilename();
        var fileSize = multipartFile.getSize() > 0 ? multipartFile.getSize() : null;
        return new DomainFile(inputStream, originalFileName, fileSize);
    }
}
