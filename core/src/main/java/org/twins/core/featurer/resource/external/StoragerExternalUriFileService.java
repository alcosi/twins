package org.twins.core.featurer.resource.external;

import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.resource.AddedFileKey;
import org.twins.core.featurer.resource.StoragerAbstractCheckedFileService;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2903,
        name = "StoragerExternalUriFileService",
        description = "Service to keep and work with external uri")
@Slf4j
public class StoragerExternalUriFileService extends StoragerAbstractCheckedFileService {

    @Override
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params) throws ServiceException {
        return URI.create(fileKey);
    }

    @Override
    public AddedFileKey addExternalUrlFile(UUID fileId, String externalUrl, HashMap<String, String> params) throws ServiceException {
        return checkAndAddUriInternal(fileId, externalUrl, params);
    }

    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        return "";
    }

    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(URI.create(fileKey), params);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Failed to retrieve the file: HTTP Status " + response.statusCode());
            }
            return response.body();
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get file");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        //External resource, no need to delete anything
    }

    @Override
    protected String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        return "";
    }

    @Override
    @SneakyThrows
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External URI service is not configured to store file bytes!");
    }

    protected AddedFileKey checkAndAddUriInternal(UUID fileId, String externalUri, HashMap<String, String> params) throws ServiceException {
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(URI.create(externalUri), params);
            InputStream fileStream = response.body();
            Integer fileSizeLimit = getFileSizeLimit(params);
            Optional<Long> contentLengthHeader = response.headers().firstValue(HttpHeaders.CONTENT_LENGTH).map(Long::valueOf);
            //Chunked response, have to check content length by downloading file =(
            if (contentLengthHeader.isEmpty() || contentLengthHeader.get() < 0) {
                CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(fileStream, fileSizeLimit, 0);
                if (sizeLimitedStream != null) {
                    int byteBufferSize = Short.MAX_VALUE;
                    //Read all bytes by chunks till the end
                    while (sizeLimitedStream.read() > -1) {
                        sizeLimitedStream.readNBytes(byteBufferSize);
                    }
                }
                return new AddedFileKey(externalUri, sizeLimitedStream.bytesRead());
            }
            if (fileSizeLimit != null && fileSizeLimit > -1 && contentLengthHeader.get() > fileSizeLimit) {
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + contentLengthHeader.get() + ")");

            }
            try (InputStream is = checkMimeTypeAndCacheStream(fileStream, params)) {
                return new AddedFileKey(externalUri, contentLengthHeader.get());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to add file");
        }
    }

}
