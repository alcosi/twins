package org.twins.core.featurer.twin.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4101,
        name = "Unsorted",
        description = "")
public class TwinSorterStub extends TwinSorter {
    @Override
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties, org.twins.core.dao.twinclass.TwinClassFieldEntity twinClassFieldEntity, org.hibernate.query.SortDirection direction) throws ServiceException {
        return baseSpec -> baseSpec; // no sorting
    }

    @Override
    public boolean checkCompatibleSorter(FieldTyper fieldTyper) {
        return true;
    }
}
