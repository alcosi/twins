package org.twins.core.featurer.twin.detector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.List;
import java.util.Properties;

@Slf4j
@Featurer(id = FeaturerTwins.ID_2801,
        name = "SearchDetectorPreferProtected",
        description = "")
@Component
public class SearchDetectorPreferProtected extends SearchDetector {

    @Override
    public List<TwinSearchEntity> detect(Properties properties, List<TwinSearchEntity> allAliasSearches) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        permissionService.loadCurrentUserPermissions();
        TwinSearchEntity twinSearchEntity = null;
        for (TwinSearchEntity searchByAliasEntity : allAliasSearches) { //many searches can be linked to one alias
            if (searchByAliasEntity.getPermissionId() == null || apiUser.getPermissions().contains(searchByAliasEntity.getPermissionId())) {
                if (twinSearchEntity == null)
                    twinSearchEntity = searchByAliasEntity;
                else
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_NOT_UNIQ);
            }
        }
        if (twinSearchEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        return List.of(twinSearchEntity);
    }
}
