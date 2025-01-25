package org.twins.core.featurer.resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.twins.core.featurer.FeaturerTwins;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "StorageResourceService",
        description = "Services for resource(file) uploading")
@Slf4j
public abstract class StorageResourceService extends FeaturerTwins {
    @FeaturerParam(name = "selfHostDomainBaseUri", description = "external URI/domain of twins application to create resource links")
    public static final FeaturerParamString selfHostDomainBaseUri = new FeaturerParamString("selfHostDomainBaseUri");
    @FeaturerParam(name = "fileSizeLimit", description = "Limit of file size")
    public static final FeaturerParamInt fileSizeLimit = new FeaturerParamInt("fileSizeLimit");
    @FeaturerParam(name = "supportedMimeTypes", description = "List of supported mime types")
    public static final FeaturerParamWordList supportedMimeTypes = new FeaturerParamWordList("supportedMimeTypes");

    protected String getSelfHostBaseDomainUri(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        return selfHostDomainBaseUri.extract(properties);
    }
    protected Integer getFileSizeLimit(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        return fileSizeLimit.extract(properties);
    }
    abstract InputStream getResourceAsStream(String resourceKey,HashMap<String, String> params);

    @SneakyThrows
    byte[] getResourceBytes(String resourceKey,HashMap<String, String> params) {
        try (InputStream stream = getResourceAsStream(resourceKey,params)) {
            return stream.readAllBytes();
        }
    }

    abstract URI getResourceUri(String resourceKey,HashMap<String, String> params) throws ServiceException ;

    public void saveResource(String resourceKey, InputStream resourceStream,HashMap<String, String> params) throws ServiceException {
         saveResourceInternal(resourceKey, resourceStream, params);
    }
    public void saveResource(String resourceKey, byte[] resource,HashMap<String, String> params) throws ServiceException {
        Integer fileSizeLimit=getFileSizeLimit(params);
        if (resource.length>fileSizeLimit){
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Resource size limit "+fileSizeLimit+" exceeded ("+resource.length+")");
        }
        saveResource(resourceKey, new ByteArrayInputStream(resource),params);
    }

    protected void  saveResourceInternal(String resourceKey, InputStream resourceStream,HashMap<String, String> params) throws ServiceException {
        Integer fileSizeLimit=getFileSizeLimit(params);
        InputStream wrappedStream=(fileSizeLimit!=null&& fileSizeLimit>0 &&fileSizeLimit!=Integer.MAX_VALUE)?new LimitedSizeInputStream(resourceStream,fileSizeLimit):resourceStream;
        try {
            saveResource(resourceKey, resourceStream, params);
        } catch (LimitedSizeInputStream.SizeExceededException ex){
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Resource size limit "+ex.limit+" exceeded ("+ex.bytesRead+")");
        }
    }

    abstract void deleteResource(String resourceKey, HashMap<String, String> params ) throws ServiceException ;
}
