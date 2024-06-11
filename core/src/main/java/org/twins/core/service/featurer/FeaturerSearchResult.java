package org.twins.core.service.featurer;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class FeaturerSearchResult extends PageableResult {
    private List<FeaturerEntity> featurerList;
}
