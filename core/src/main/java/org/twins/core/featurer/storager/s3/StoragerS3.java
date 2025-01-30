package org.twins.core.featurer.storager.s3;

import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@Featurer(id = FeaturerTwins.ID_2904,
        name = "StoragerS3",
        description = "Service to save files to S3 and serve in controller")
@Slf4j
public class StoragerS3 extends StoragerAbstractChecked {
    protected final Long DEFAULT_PART_SIZE = 10485760L;

    @FeaturerParam(name = "s3Uri", description = "Uri to work with s3")
    public static final FeaturerParamString s3Uri = new FeaturerParamString("s3Uri");

    @FeaturerParam(name = "s3Region", description = "Region config for s3")
    public static final FeaturerParamString s3Region = new FeaturerParamString("s3Region");

    @FeaturerParam(name = "s3Bucket", description = "Bucket of s3")
    public static final FeaturerParamString s3Bucket = new FeaturerParamString("s3Bucket");

    @FeaturerParam(name = "s3AccessKey", description = "Access key for s3")
    public static final FeaturerParamString s3AccessKey = new FeaturerParamString("s3AccessKey");

    @FeaturerParam(name = "s3SecretKey", description = "Secret key for s3")
    public static final FeaturerParamString s3SecretKey = new FeaturerParamString("s3SecretKey");

    @FeaturerParam(name = "basePath", description = "Base path of directory(key) where to save files")
    public static final FeaturerParamString basePath = new FeaturerParamString("basePath");


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
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        try {
            try (InputStream is = fileStream) {
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
                                .stream(is, -1L, DEFAULT_PART_SIZE).build());
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
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            MinioClient s3Client = getS3MinioClient(params);
            Properties properties = extractProperties(params, false);
            GetObjectResponse object = s3Client.getObject(GetObjectArgs.builder().bucket(s3Bucket.extract(properties)).object(fileKey).build());
            return object;
        } catch (Throwable t) {
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
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete file from S3");
        }
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String baseLocalPathString = addSlashAtTheEndIfNeeded(basePath.extract(properties));
        String businessDomain = addSlashAtTheEndIfNeeded(getDomainId().map(UUID::toString).orElse("defaultDomain"));
        String businessAccount = addSlashAtTheEndIfNeeded(getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount"));
        return baseLocalPathString + businessDomain + businessAccount + fileId;
    }
}
