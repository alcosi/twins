package org.twins.core.featurer.search.detector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.SearchEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 2801,
        name = "SearchDetectorPreferProtected",
        description = "")
@RequiredArgsConstructor
public class SearchDetectorPreferProtected extends SearchDetector {
    final AuthService authService;
    final PermissionService permissionService;

    @Override
    public List<SearchEntity> detect(Properties properties, List<SearchEntity> allAliasSearches) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        permissionService.loadUserPermissions(apiUser);
        SearchEntity searchEntity = null;
        for (SearchEntity searchByAliasEntity : allAliasSearches) { //many searches can be linked to one alias
            if (searchByAliasEntity.getPermissionId() == null || apiUser.getPermissions().contains(searchByAliasEntity.getPermissionId())) {
                if (searchEntity == null)
                    searchEntity = searchByAliasEntity;
                else
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_NOT_UNIQ);
            }
        }
        if (searchEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        return List.of(searchEntity);
    }
}
