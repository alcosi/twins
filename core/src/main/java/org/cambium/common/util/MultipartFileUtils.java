package org.cambium.common.util;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class MultipartFileUtils {
    public static FileData convert(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) return null;
        var inputStream = multipartFile.getInputStream();
        var originalFileName = multipartFile.getOriginalFilename();
        var fileSize = multipartFile.getSize() > 0 ? multipartFile.getSize() : null;
        return new FileData(inputStream, originalFileName, fileSize);
    }

    /**
     * Resolves a {@code multipart://<part_name>} link against the files of the same multipart request.
     * Returns {@code null} when the link is absent or not a multipart link (external links are the caller's concern).
     * Throws {@link ServiceException} with MULTIPART_FILE_IS_NOT_PRESENTED when the referenced part is missing or unreadable.
     */
    public static FileData resolveMultipartFile(String link, Map<String, MultipartFile> files) throws ServiceException {
        if (link == null || link.isBlank() || !link.toLowerCase().startsWith("multipart://"))
            return null;
        String fileKey = link.replaceFirst("multipart://", "");
        MultipartFile file = files.get(fileKey);
        if (file == null)
            throw new ServiceException(ErrorCodeCommon.MULTIPART_FILE_IS_NOT_PRESENTED, "File not found " + fileKey);
        try {
            return new FileData(file.getInputStream(), file.getOriginalFilename(), file.getSize());
        } catch (Throwable t) {
            log.error("Error while processing multipart link {}", link, t);
            throw new ServiceException(ErrorCodeCommon.MULTIPART_FILE_IS_NOT_PRESENTED, "Error while processing multipart link " + link + ". " + t.getClass().getSimpleName());
        }
    }
}
