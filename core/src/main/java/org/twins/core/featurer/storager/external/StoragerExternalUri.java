package org.twins.core.featurer.storager.external;

import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import io.github.breninsul.springHttpMessageConverter.inputStream.ContentDispositionType;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponseExtensionKt;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static org.cambium.common.util.UrlUtils.toURI;

@Component
@Featurer(id = FeaturerTwins.ID_2903,
        name = "StoragerExternalUri",
        description = "Service to keep and work with external uri")
@Slf4j
public class StoragerExternalUri extends StoragerAbstractChecked {
    @FeaturerParam(name = "downloadExternalFileConnectionTimeout",
            description = "When file is added as external URI, basically there is no need to download it.\n But if Mime-Type or Size limit check is set, file should be partly downloaded to perform check.",
            optional = true,
            defaultValue = "60000",
            exampleValues = {"60000", "1000"}
    )
    public static final FeaturerParamInt downloadExternalFileConnectionTimeout = new FeaturerParamInt("downloadExternalFileConnectionTimeout");

    @Override
    protected Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        Integer extracted = downloadExternalFileConnectionTimeout.extract(properties);
        return Duration.ofMillis(extracted == null || extracted < 1 ? 60000 : extracted.longValue());
    }
    @Override
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params) throws ServiceException {
        return toURI(fileKey);
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
    public InputStreamResponse getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(toURI(fileKey), params);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Failed to retrieve the file: HTTP Status " + response.statusCode());
            }
            InputStream inputStream = response.body();
            String fileName = Arrays.stream(fileKey.split("\\/")).toList().getLast();
            return InputStreamResponseExtensionKt.toInputStreamResponse(
                    inputStream,
                    fileName,
                    null,
                    true,
                    ContentDispositionType.INLINE,
                    true,
                    null
            );
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
            Integer fileSizeLimit = getFileSizeLimit(params);
            Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
            boolean haveToCheckSize = fileSizeLimit != null && fileSizeLimit > -1 && fileSizeLimit < Integer.MAX_VALUE;
            boolean haveToCheckMimeType = supportedMimeTypes != null && !supportedMimeTypes.isEmpty();
            if (!haveToCheckSize && !haveToCheckMimeType) {
                return new AddedFileKey(externalUri, -1);
            }
            //Have to make request
            HttpResponse<InputStream> response = getInputStreamHttpResponse(toURI(externalUri), params);
            InputStream fileStream = response.body();
            Long contentLengthHeader = response.headers().firstValue(HttpHeaders.CONTENT_LENGTH).map(Long::valueOf).orElse(-1L);
            if (haveToCheckSize && contentLengthHeader > fileSizeLimit) {
                fileStream.close();
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + contentLengthHeader + ")");
            }
            try (InputStream is = checkMimeTypeAndCacheStream(fileStream, params)) {
                if (contentLengthHeader > -1) {
                    return new AddedFileKey(externalUri, contentLengthHeader);
                }
                //Chunked response, have to check content length by downloading file =(
                CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(is, fileSizeLimit, 0);
                int byteBufferSize = 16384;
                //Read all bytes by chunks till the end
                while (sizeLimitedStream.read() > -1) {
                    sizeLimitedStream.readNBytes(byteBufferSize);
                }
                return new AddedFileKey(externalUri, sizeLimitedStream.bytesRead());
            }

        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to add file");
        }
    }

}
