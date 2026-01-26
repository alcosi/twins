package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;
import org.twins.core.service.space.SpaceRoleUserService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
    protected void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException {
        recipientIds.addAll(spaceRoleUserService.getUsers(history.getTwin().getId(), spaceRoleIds.extract(properties)));
    }
}
