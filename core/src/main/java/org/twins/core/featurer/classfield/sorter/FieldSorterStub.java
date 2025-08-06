package org.twins.core.featurer.classfield.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4001,
        name = "Unsorted",
        description = "")
public class FieldSorterStub extends FieldSorter {
    @Override
    public Sort createSort(Properties properties) throws ServiceException {
        return Sort.unsorted();
    }
}
