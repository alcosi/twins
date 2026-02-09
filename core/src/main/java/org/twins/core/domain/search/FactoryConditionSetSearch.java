package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryConditionSetSearch {
   Set<UUID> idList;
   Set<UUID> idExcludeList;
   Set<UUID> twinFactoryIdList;
   Set<UUID> twinFactoryIdExcludeList;
   Set<String> nameLikeList;
   Set<String> nameNotLikeList;
   Set<String> descriptionLikeList;
   Set<String> descriptionNotLikeList;
}
