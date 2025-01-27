package org.twins.core.featurer.resource.s3;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.featurer.resource.AbstractCheckedStorageFileService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
abstract class AbstractS3StorageFileService extends AbstractCheckedStorageFileService {
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


    protected S3Client getS3Client(HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String region = s3Region.extract(properties);
        S3Client s3Client = S3Client
                .builder()
                //AWS_GLOBAL should work with s3 comparable storages
                .region(region == null ? Region.AWS_GLOBAL : Region.of(region))
                .forcePathStyle(true)
                .endpointOverride(URI.create(s3Uri.extract(properties)))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey.extract(properties), s3SecretKey.extract(properties))))
                .build();
        return s3Client;
    }

    protected S3AsyncClient getS3AsyncClient(HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String region = s3Region.extract(properties);
        S3AsyncClient s3Client = S3AsyncClient
                .builder()
                //AWS_GLOBAL should work with s3 comparable storages
                .region(region == null ? Region.AWS_GLOBAL : Region.of(region))
                .forcePathStyle(true)
                .endpointOverride(URI.create(s3Uri.extract(properties)))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey.extract(properties), s3SecretKey.extract(properties))))
                .build();
        return s3Client;
    }

    @Override
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            S3AsyncClient s3Client = getS3AsyncClient(params, context);
            Properties properties = featurerService.extractProperties(this, params, context);
            String bucket = s3Bucket.extract(properties);
            if (!bucketExists(s3Client, bucket, params, context)) {
                try {
                    s3Client.createBucket(request -> request.bucket(bucket)).get();
                } catch (Throwable t) {
                    log.error("Error trying to create bucket {}: {}", bucket, t.getMessage(), t);
                }
            }
            BlockingInputStreamAsyncRequestBody body = AsyncRequestBody.forBlockingInputStream(null);
            try {
                CompletableFuture<PutObjectResponse> responseFuture = s3Client.putObject(rq -> rq.bucket(bucket).key(fileKey), body);
                body.writeInputStream(fileStream);
                responseFuture.get();
            } catch (InterruptedException | ExecutionException exception) {
                if (exception.getCause() != null) {
                    throw exception.getCause();
                }
                log.trace("Error trying upload file {}: {}", fileKey, exception.getMessage(), exception);
            }
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to create or save file to S3");
        }
    }

    @SneakyThrows
    protected boolean bucketExists(S3AsyncClient s3Client, String
            bucketName, HashMap<String, String> params, HashMap<String, Object> context) {
        try {
            try {
                s3Client.headBucket(request -> request.bucket(bucketName)).get();
                return true;
            } catch (InterruptedException | ExecutionException exception) {
                if (exception.getCause() != null) {
                    throw exception.getCause();
                }
                log.trace("Error trying to check if bucket {} exists: {}", bucketName, exception.getMessage(), exception);
                return false;

            }
        } catch (NoSuchBucketException exception) {
            return false;
        }
    }


    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            S3Client s3Client = getS3Client(params, context);
            Properties properties = featurerService.extractProperties(this, params, context);
            ResponseInputStream<GetObjectResponse> s3ClientObject = s3Client.getObject(request ->
                    request
                            .bucket(s3Bucket.extract(properties))
                            .key(fileKey));
            return s3ClientObject;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unable to get file from S3");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            S3Client s3Client = getS3Client(params, context);
            Properties properties = featurerService.extractProperties(this, params, context);
            s3Client.deleteObject(request ->
                    request
                            .bucket(s3Bucket.extract(properties))
                            .key(fileKey));
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete file from S3");
        }
    }

    @Override
    public String generateFileKey(UUID
                                          fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String baseLocalPathString = addSlashAtTheEndIfNeeded(basePath.extract(properties));
        String businessDomain = addSlashAtTheEndIfNeeded(context.containsKey(CONTEXT_ATTRIBUTE_BUSINESS_DOMAIN) ? context.get(CONTEXT_ATTRIBUTE_BUSINESS_DOMAIN).toString() : "defaultDomain");
        String businessAccount = addSlashAtTheEndIfNeeded(context.containsKey(CONTEXT_ATTRIBUTE_BUSINESS_ACCOUNT) ? context.get(CONTEXT_ATTRIBUTE_BUSINESS_ACCOUNT).toString() : "defaultBusinessAccount");
        return baseLocalPathString + businessDomain + businessAccount + fileId;
    }
}
