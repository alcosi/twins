package org.twins.core.featurer.classfield.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4001,
        name = "Unsorted",
        description = "")
public class FieldSorterStub extends FieldSorter {
    @Override
    public Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> createSort(Properties properties) throws ServiceException {
        return null;
    }
}
