package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.ArrayUtils.concatArray;
import static org.twins.core.dao.specifications.CommonSpecification.*;

/**
 * Builds the object-level authorization {@link Specification} for an entity that references a
 * {@link TwinEntity} through a JPA path (e.g. a twin link's src/dst twin, a comment's twin).
 * Encapsulates the visibility rules previously duplicated across {@code CommentSearchService} and
 * {@code TwinLinkSearchService}, mirroring {@code TwinSearchServiceV2}:
 * <ul>
 *   <li>with {@link Permissions#DOMAIN_TWINS_VIEW_ALL} the twin is visible when it belongs to the
 *       caller's domain (resolved via {@code twin -> twinClass.domainId});</li>
 *   <li>otherwise the twin must be individually authorized via {@code checkPermissions}
 *       (view-permission grants) + {@code checkClass} (owner-type / domain).</li>
 * </ul>
 * Call once per twin endpoint and combine the results with {@code .and(...)} — a relation is visible
 * only when ALL of its twin endpoints are authorized.
 */
@Component
@RequiredArgsConstructor
public class TwinAccessSpecificationFactory {
    private final PermissionService permissionService;
    private final UserGroupService userGroupService;

    public <T> Specification<T> checkTwinAccess(ApiUser apiUser, String... twinEntityFieldPath) throws ServiceException {
        UUID domainId = apiUser.getDomainId();
        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL)) {
            // twin.twinClass.domainId = domainId
            return checkFieldUuid(domainId, concatArray(twinEntityFieldPath, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.domainId));
        }
        userGroupService.loadGroupsForCurrentUser();
        return Specification.allOf(
                checkPermissions(domainId, apiUser.getBusinessAccountId(), apiUser.getUserId(),
                        apiUser.getUser().getUserGroupsFootprint(), twinEntityFieldPath),
                checkClass(List.of(), apiUser, DBUMembershipCheck.BLOCKED, twinEntityFieldPath)
        );
    }
}
