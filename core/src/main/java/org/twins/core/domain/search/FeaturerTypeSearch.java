package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.featurer.dao.FeaturerTypeEntity;

import java.util.Set;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class FeaturerTypeSearch extends EntitySearch<FeaturerTypeEntity> {
    private Set<Integer> idList;
    private Set<Integer> idExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
}
