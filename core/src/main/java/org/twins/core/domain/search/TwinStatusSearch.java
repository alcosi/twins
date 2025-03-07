package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.domain.twinclass.TwinClassIdsExtender;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinStatusSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private List<TwinClassIdsExtender> twinClassIdsExtenderList;
    private List<TwinClassIdsExtender> twinClassIdsExtenderExcludeList;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private Set<String> nameI18nLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18nLikeList;
    private Set<String> descriptionI18nNotLikeList;
}
