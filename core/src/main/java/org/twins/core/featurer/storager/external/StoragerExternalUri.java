package org.twins.core.featurer.storager.external;

import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import io.github.breninsul.springHttpMessageConverter.inputStream.ContentDispositionType;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponseExtensionKt;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
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
            Response response = getInputStreamHttpResponse(toURI(fileKey), params);
            InputStream inputStream = response.body().byteStream();
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
    protected AddedFileKey addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) {
        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External URI service is not configured to store file bytes!");
    }

    protected AddedFileKey checkAndAddUriInternal(UUID fileId, String externalUri, HashMap<String, String> params) throws ServiceException {
        try {
            Integer fileSizeLimit = getFileSizeLimit(params);
            Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
            boolean haveToCheckSize = fileSizeLimit != null && fileSizeLimit > -1 && fileSizeLimit < Integer.MAX_VALUE;
            boolean haveToCheckMimeType = supportedMimeTypes != null && !supportedMimeTypes.isEmpty();
            if (!haveToCheckSize && !haveToCheckMimeType) {
                return new AddedFileKey(externalUri, -1, Collections.emptyList());
            }
            //Have to make request
            Response response = getInputStreamHttpResponse(toURI(externalUri), params);
            try (InputStream fileStream = response.body().byteStream()) {
                long contentLengthHeader = response.headers(HttpHeaders.CONTENT_LENGTH).stream().findFirst().map(Long::valueOf).orElse(-1L);
                if (haveToCheckSize && contentLengthHeader > fileSizeLimit) {
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + contentLengthHeader + ")");
                }
                try (InputStream is = checkMimeTypeAndCacheStream(fileStream, params).fileStream()) {
                    if (contentLengthHeader > -1) {
                        return new AddedFileKey(externalUri, contentLengthHeader, Collections.emptyList());
                    }
                    //Chunked response, have to check content length by downloading file =(
                    CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(is, fileSizeLimit, 0);
                    int byteBufferSize = 16384;
                    //Read all bytes by chunks till the end
                    while (sizeLimitedStream.read() > -1) {
                        sizeLimitedStream.readNBytes(byteBufferSize);
                    }
                    return new AddedFileKey(externalUri, sizeLimitedStream.bytesRead(), Collections.emptyList());
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to add file");
        }
    }

}
