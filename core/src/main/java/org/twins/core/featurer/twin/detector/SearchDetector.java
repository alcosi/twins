package org.twins.core.featurer.twin.detector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.search.TwinSearchAliasEntity;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_28,
        name = "SearchBatcher",
        description = "Encapsulate logic to select searches from one alias into one batch")
@Slf4j
public abstract class SearchDetector extends FeaturerTwins {

    @Autowired
    AuthService authService;

    @Lazy
    @Autowired
    PermissionService permissionService;

    public List<TwinSearchEntity> detect(TwinSearchAliasEntity aliasEntity, List<TwinSearchEntity> allAliasSearches) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, aliasEntity.getTwinSearchDetectorParams(), new HashMap<>());
        return detect(properties, allAliasSearches);
    }

    public abstract List<TwinSearchEntity> detect(Properties properties, List<TwinSearchEntity> allAliasSearches) throws ServiceException;

}
