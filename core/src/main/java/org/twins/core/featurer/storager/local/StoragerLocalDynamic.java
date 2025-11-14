package org.twins.core.featurer.storager.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_2902,
        name = "StoragerLocalDynamicController",
        description = "Service to save files in local file system and return their URL as '$selfHostDomainBaseUri'+'$relativeFileUri'"
)
@Slf4j
public class StoragerLocalDynamic extends StoragerLocalStatic {
    @FeaturerParam(name = "relativeFileUri", description = "Relative uri of controller to provide files",
            optional = true,
            defaultValue = "/public/static-resource/{id}/v1",
            exampleValues = {"/public/static-resource/{id}/v1", "/public/resource/{id}/v2"}
    )
    public static final FeaturerParamString relativeFileUri = new FeaturerParamString("relativeFileUri");


    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String relativePath = relativeFileUri.extract(properties);
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + removeDoubleSlashes(addSlashAtStartIfNeeded(contextPath) + addSlashAtStartIfNeeded(relativePath));
    }

}
