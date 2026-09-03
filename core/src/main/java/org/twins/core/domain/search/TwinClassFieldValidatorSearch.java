package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@FieldNameConstants
public class TwinClassFieldValidatorSearch extends EntitySearch<TwinClassFieldValidatorEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinClassFieldIdList;
    private Set<UUID> twinClassFieldIdExcludeList;
    private Set<Integer> fieldValidatorFeaturerIdList;
    private Set<Integer> fieldValidatorFeaturerIdExcludeList;
}
