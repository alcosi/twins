package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;
import org.twins.core.service.space.SpaceRoleUserService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves recipient users based on their roles within a specific space (e.g. task participants).
 * <p>Result is a query result (not a relation), so this resolver overrides {@link #resolveBatch}
 * directly (no {@link RecipientResolverAtomic}): {@code spaceRoleIds} are fixed by params, only the
 * space (twin id) varies per history. It runs ONE bulk query over the resolver group's twin ids
 * (provided by {@link RecipientResolveBatch#getTwinIds()}) and distributes the userIds per history.
 * The preload map is a local variable — thread-safe on the singleton bean.
 */
@Component
@Featurer(id = FeaturerTwins.ID_4703,
        name = "Space Role–based Recipient Resolver",
        description = "Resolves recipient users based on their roles within a specific space (for example, task participants)")
public class RecipientResolverSpaceRoles extends RecipientResolver {

    @FeaturerParam(name = "Space role ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet spaceRoleIds = new FeaturerParamUUIDSetUserId("spaceRoleIds");

    @Lazy
    @Autowired
    private SpaceRoleUserService spaceRoleUserService;

    @Override
    public void resolveBatch(RecipientResolveBatch batch, Properties properties) throws ServiceException {
        Set<UUID> paramSpaceRoleIds = spaceRoleIds.extract(properties);
        if (CollectionUtils.isEmpty(paramSpaceRoleIds)) {
            return;
        }
        Set<UUID> twinIds = batch.getTwinIds();
        if (twinIds.isEmpty()) {
            return;
        }
        // one bulk query → Map<twinId, Set<userId>>
        Map<UUID, Set<UUID>> userIdsByTwin = spaceRoleUserService.getUsersIn(twinIds, paramSpaceRoleIds);
        // distribute per history
        for (var entry : batch.getRecipientIdsByHistory().entrySet()) {
            UUID twinId = entry.getKey().getTwin().getId();
            Set<UUID> resolved = twinId == null ? null : userIdsByTwin.get(twinId);
            if (CollectionUtils.isNotEmpty(resolved)) {
                entry.getValue().addAll(resolved);
            }
        }
    }
}
