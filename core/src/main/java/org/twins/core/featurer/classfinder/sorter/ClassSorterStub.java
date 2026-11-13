package org.twins.core.featurer.classfinder.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5801,
        name = "Unsorted",
        description = "")
public class ClassSorterStub extends ClassSorter {
    @Override
    public Function<Specification<TwinClassEntity>, Specification<TwinClassEntity>> createSort(Properties properties) throws ServiceException {
        return null;
    }
}
