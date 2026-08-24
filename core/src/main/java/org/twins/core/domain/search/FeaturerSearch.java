package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.dao.FeaturerEntity;

import java.util.Set;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class FeaturerSearch extends EntitySearch<FeaturerEntity> {
    private Set<Integer> idList;
    private Set<Integer> idExcludeList;
    private Set<Integer> typeIdList;
    private Set<Integer> typeIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameOrIdLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Ternary deprecated;
}
