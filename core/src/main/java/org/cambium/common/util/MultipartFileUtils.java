package org.cambium.common.util;

import org.cambium.common.file.FileData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileUtils {
    public static FileData convert(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) return null;
        var inputStream = multipartFile.getInputStream();
        var originalFileName = multipartFile.getOriginalFilename();
        var fileSize = multipartFile.getSize() > 0 ? multipartFile.getSize() : null;
        return new FileData(inputStream, originalFileName, fileSize);
    }
}
