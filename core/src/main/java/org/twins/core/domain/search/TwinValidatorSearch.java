package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinValidatorSetIdList;
    private Set<UUID> twinValidatorSetIdExcludeList;
    private Set<Integer> validatorFeaturerIdList;
    private Set<Integer> validatorFeaturerIdExcludeList;
    private Ternary invert;
    private Ternary active;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private IntegerRange order;
}
