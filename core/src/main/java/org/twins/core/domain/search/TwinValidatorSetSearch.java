package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorSetSearch extends EntitySearch<TwinValidatorSetEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Ternary invert;
    private IntegerRange usageCountRange;
}
