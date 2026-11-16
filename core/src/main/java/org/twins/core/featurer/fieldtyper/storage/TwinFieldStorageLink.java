package org.twins.core.featurer.fieldtyper.storage;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageLink extends TwinFieldStorage {
    private final TwinLinkService twinLinkService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final LinkService linkService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        twinLinkService.loadTwinLinks(twinsKit.getCollection());
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinLinks() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinLinks(TwinLinkService.FindTwinLinksResult.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
//        twinLinkService.deleteTwinLinks();
    };

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }

    @Override
    public GroupExpressionProvider<TwinEntity> countGroup(TwinClassFieldEntity field) throws ServiceException {
        FieldTyperLink typer = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyperLink.class);
        UUID linkId = typer.getLinkId(field.getFieldTyperParams());
        twinClassFieldService.loadTwinClass(field);
        LinkEntity link = linkService.findEntitySafe(linkId);
        LinkService.LinkDirection direction = linkService.detectLinkDirection(link, field.getTwinClass());
        if (direction == LinkService.LinkDirection.invalid) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " link direction cannot be detected");
        }
        boolean forward = direction == LinkService.LinkDirection.forward;
        return new GroupExpressionProvider<>() {
            @Override
            public Expression<?> apply(Root<TwinEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                String joinName = forward
                        ? TwinEntity.Fields.linksBySrcTwinIdSpecOnly
                        : TwinEntity.Fields.linksByDstTwinIdSpecOnly;
                Join<TwinEntity, TwinLinkEntity> join = root.join(joinName, JoinType.LEFT);
                join.on(cb.equal(join.get(TwinLinkEntity.Fields.linkId), linkId));
                return join.get(forward ? TwinLinkEntity.Fields.dstTwinId : TwinLinkEntity.Fields.srcTwinId);
            }

            @Override
            public boolean isDistinctGroup() {
                return true;
            }
        };
    }
}
