package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.service.SystemIdLookup;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Keeps twins of users joined to the requested twin's space (space_role_user),
 * e.g. the project team for a project page widget.
 * Configured {@code twinClassIds} select the twin representation of a member:
 * the system USER class mirrors user ids directly, other user classes (e.g. a
 * domain user class) are resolved through owner_user_id.
 */
@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2730,
        name = "By space role user id (requested)",
        description = "Keeps twins of the users joined to the requested twin's space")
public class TwinFinderBySpaceRoleUserRequested extends TwinFinderRequested {
    @FeaturerParam(name = "Param key", description = "", order = 1, optional = true, defaultValue = PARAM_TWIN_ID)
    public static final FeaturerParamString paramKey = new FeaturerParamString("paramKey");

    @FeaturerParam(name = "Twin class ids", description = "Scoped classes (e.g. USER or a domain user class): required for the system-class DBU search check, otherwise system twins are excluded from the result", order = 2, optional = true)
    public static final FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIds");

    @FeaturerParam(name = "Space users limit", description = "Max space members to resolve; overflow is warned and truncated", order = 3, optional = true, defaultValue = "1000")
    public static final FeaturerParamInt spaceUsersLimit = new FeaturerParamInt("spaceUsersLimit");

    @Autowired
    private SpaceRoleUserRepository spaceRoleUserRepository;

    @Autowired
    private TwinRepository twinRepository;


    @Override
    protected void concatSearch(Properties properties, TwinSearch twinSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID spaceTwinId = getRequestedId(paramKey, properties, namedParamsMap);
        if (spaceTwinId == null)
            return;
        Set<UUID> classIds = twinClassIds.extract(properties);
        boolean userMirror = CollectionUtils.isEmpty(classIds) // no classes configured -> system USER twins mirror user ids
                || classIds.stream().anyMatch(SystemIdLookup::isTwinClassForUser);
        if (CollectionUtils.isNotEmpty(classIds)) // must be present before the system-class search check runs
            twinSearch.addTwinClassId(classIds, false);
        int limit = spaceUsersLimit.extract(properties);
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "spaceUsersLimit must be positive");
        Set<UUID> userIds = spaceRoleUserRepository.findUserIdsByTwinId(spaceTwinId, limit + 1); // one extra row detects overflow
        if (CollectionUtils.isEmpty(userIds)) { // empty team must show no twins, not the whole class
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        if (userIds.size() > limit) {
            log.warn("Space members of twin[{}] exceeded limit[{}], capped - users beyond the cap are missed", spaceTwinId, limit);
            userIds = userIds.stream().limit(limit).collect(Collectors.toSet());
        }
        if (userMirror) { // system USER twins share their id with the user row
            twinSearch.addTwinId(userIds, false);
            return;
        }
        Set<UUID> ownerTwinIds = twinRepository.findIdsByOwnerIdInAndTwinClassIdIn(userIds, classIds);
        if (CollectionUtils.isEmpty(ownerTwinIds)) {
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        twinSearch.addTwinId(ownerTwinIds, false);
    }
}
