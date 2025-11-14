package org.twins.core.featurer.storager.s3;

import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import io.github.breninsul.springHttpMessageConverter.inputStream.ContentDispositionType;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponseMinIOExtensionKt;
import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@Featurer(id = FeaturerTwins.ID_2904,
        name = "StoragerS3",
        description = "Service to save files to S3 and return their URL as '$selfHostDomainBaseUri'+'public/resource/{id}/v1'")
@Slf4j
public class StoragerS3Static extends StoragerAbstractChecked {
    @Value("${server.servlet.context-path:}")
    protected String contextPath;
    protected final Long DEFAULT_PART_SIZE = 10485760L;
    @FeaturerParam(name = "downloadExternalFileConnectionTimeout",
            description = "If the File is added as external URI, it should be downloaded first.\nSo this params sets timout time in milliseconds for such download request.\n",
            optional = true,
            defaultValue = "60000",
            exampleValues = {"60000", "1000"}
    )
    public static final FeaturerParamInt downloadExternalFileConnectionTimeout = new FeaturerParamInt("downloadExternalFileConnectionTimeout");

    @FeaturerParam(name = "s3Uri", description = "URI of s3 server",
            optional = true,
            defaultValue = "https://s3.amazonaws.com",
            exampleValues = {"https://s3.amazonaws.com", "http://127.0.0.1:9000", "https://s3.us-west-2.amazonaws.com"})
    public static final FeaturerParamString s3Uri = new FeaturerParamString("s3Uri");

    @FeaturerParam(name = "s3Region", description = "Region config for s3.\n Can be 'aws-global' for S3 comparable storages lice MinIO",
            optional = true,
            defaultValue = "aws-global",
            exampleValues = {"aws-global", "us-west-2"})
    public static final FeaturerParamString s3Region = new FeaturerParamString("s3Region");

    @FeaturerParam(name = "s3Bucket", description = "S3 bucket", optional = false, exampleValues = {"documents", "resources", "twins"})
    public static final FeaturerParamString s3Bucket = new FeaturerParamString("s3Bucket");

    @FeaturerParam(name = "s3AccessKey", description = "Access key (username) for S3", optional = false, exampleValues = {"AKIAIOSFODNN7EXAMPLE"})
    public static final FeaturerParamString s3AccessKey = new FeaturerParamString("s3AccessKey");

    @FeaturerParam(name = "s3SecretKey", description = "Secret key (password) for S3", optional = false, exampleValues = {"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"})
    public static final FeaturerParamString s3SecretKey = new FeaturerParamString("s3SecretKey");

    @FeaturerParam(name = "basePath",
            description = "Prefix for file keys.\nPlaceholders {domainId} and {businessAccountId} can be used to make domain/account relevant path.",
            optional = true,
            defaultValue = "/twins-resources/{domainId}/{businessAccountId}",
            exampleValues = {"/twins-resources/{domainId}/{businessAccountId}", "/attachments/{domainId}", "/files"})
    public static final FeaturerParamString basePath = new FeaturerParamString("basePath");

    @Override
    protected Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        Integer extracted = downloadExternalFileConnectionTimeout.extract(properties);
        return Duration.ofMillis(extracted == null || extracted < 1 ? 60000 : extracted.longValue());
    }

    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        String controllerPath = "public/resource/{id}/v1";
        Properties properties = extractProperties(params, false);
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + removeDoubleSlashes(addSlashAtStartIfNeeded(contextPath) + addSlashAtStartIfNeeded(controllerPath));
    }

    @SneakyThrows
    protected MinioClient getS3MinioClient(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String region = s3Region.extract(properties);
        MinioClient s3Client = MinioClient
                .builder()
                //AWS_GLOBAL should work with s3 comparable storages
                .region(region == null ? "aws-global" : region)
                .endpoint(URI.create(s3Uri.extract(properties)).toURL())
                .credentials(s3AccessKey.extract(properties), s3SecretKey.extract(properties))
                .build();
        return s3Client;
    }


    @Override
    protected AddedFileKey addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) throws ServiceException {
        try {
            //Wrap to count bytes and limit if needed
            Integer fileSizeLimit = getFileSizeLimit(params);
            CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(fileStream, fileSizeLimit, 0);
            try (sizeLimitedStream) {
                MinioClient s3Client = getS3MinioClient(params);
                Properties properties = extractProperties(params, false);
                String bucket = s3Bucket.extract(properties);
                if (!bucketExists(s3Client, bucket)) {
                    try {
                        s3Client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    } catch (Throwable t) {
                        log.error("Error trying to create bucket {}: {}", bucket, t.getMessage(), t);
                    }
                }

                var response = s3Client.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(fileKey)
                                .contentType(mimeType)
                                .stream(sizeLimitedStream, -1L, DEFAULT_PART_SIZE).build());

                return new AddedFileKey(fileKey, sizeLimitedStream.bytesRead(), Collections.emptyList());
            }
        } catch (Throwable t) {
            log.error("Error trying to save file to S3: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to create or save file to S3");
        }
    }

    @SneakyThrows
    protected boolean bucketExists(MinioClient s3Client, String bucketName) {
        try {
            return s3Client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Throwable t) {
            log.error("Error trying to check if bucket {} exists: {}", bucketName, t.getMessage(), t);
            throw t;
        }
    }

    @Override
    public InputStreamResponse getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            MinioClient s3Client = getS3MinioClient(params);
            Properties properties = extractProperties(params, false);
            GetObjectResponse object = s3Client.getObject(GetObjectArgs.builder().bucket(s3Bucket.extract(properties)).object(fileKey).build());
            return InputStreamResponseMinIOExtensionKt.toMinIOResource(object, null, true, ContentDispositionType.INLINE, true);
        } catch (Throwable t) {
            log.error("Error trying to get file from S3: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unable to get file from S3");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            MinioClient s3Client = getS3MinioClient(params);
            Properties properties = extractProperties(params, false);
            s3Client.removeObject(RemoveObjectArgs.builder().bucket(s3Bucket.extract(properties)).object(fileKey).build());
        } catch (Throwable t) {
            log.error("Error trying to delete file from S3: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete file from S3");
        }
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String domainId = getDomainId().map(UUID::toString).orElse("defaultDomain");
        String businessAccount = getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount");
        String baseLocalPathString = addSlashAtTheEndIfNeeded(basePath.extract(properties));
        String key = baseLocalPathString
                .replace("{domainId}", domainId)
                .replace("{businessAccountId}", businessAccount) + fileId.toString();
        String removedDoubleSlashes = removeDoubleSlashes(key);
        if (removedDoubleSlashes.startsWith("/")) {
            return removedDoubleSlashes.substring(1);
        } else {
            return removedDoubleSlashes;
        }
    }
}
