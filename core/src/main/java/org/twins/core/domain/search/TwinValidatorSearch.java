package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.validator.TwinValidatorEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorSearch extends EntitySearch<TwinValidatorEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinValidatorSetIdList;
    private Set<UUID> twinValidatorSetIdExcludeList;
    private Set<Integer> validatorFeaturerIdList;
    private Set<Integer> validatorFeaturerIdExcludeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Ternary invert;
    private Ternary active;
}
