package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.link.LinkValidatorEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@FieldNameConstants
public class LinkValidatorSearch extends EntitySearch<LinkValidatorEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> linkIdList;
    private Set<UUID> linkIdExcludeList;
    private Set<Integer> linkerFeaturerIdList;
    private Set<Integer> linkerFeaturerIdExcludeList;
}
