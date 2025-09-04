package org.twins.core.featurer.user.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4201,
        name = "Unsorted",
        description = "")
public class UserSorterStub extends UserSorter {
    @Override
    public Function<Specification<UserEntity>, Specification<UserEntity>> createSort(Properties properties) throws ServiceException {
        return null;
    }
}
