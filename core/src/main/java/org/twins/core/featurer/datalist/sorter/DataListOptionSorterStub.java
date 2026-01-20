package org.twins.core.featurer.datalist.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5201,
        name = "Unsorted",
        description = "")
public class DataListOptionSorterStub extends DataListOptionSorter {

    @Override
    public Function<Specification<DataListOptionEntity>, Specification<DataListOptionEntity>> createSort(Properties properties) throws ServiceException {
        return null;
    }
}
