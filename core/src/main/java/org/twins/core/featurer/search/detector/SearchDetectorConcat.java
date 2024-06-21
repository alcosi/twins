package org.twins.core.featurer.search.detector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Featurer(id = FeaturerTwins.ID_2802,
        name = "SearchDetectorConcat",
        description = "")
@Component
public class SearchDetectorConcat extends SearchDetector {

    @Override
    public List<SearchEntity> detect(Properties properties, List<SearchEntity> allAliasSearches) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        permissionService.loadUserPermissions(apiUser);
        List<SearchEntity> ret = new ArrayList<>();
        for (SearchEntity searchByAliasEntity : allAliasSearches) { //many searches can be linked to one alias
            if (searchByAliasEntity.getPermissionId() == null || apiUser.getPermissions().contains(searchByAliasEntity.getPermissionId())) {
                ret.add(searchByAliasEntity);
            }
        }
        return ret;
    }
}
