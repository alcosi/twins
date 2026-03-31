package org.twins.core.featurer.user.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;

@FeaturerType(id = FeaturerTwins.TYPE_42,
        name = "UserSorter",
        description = "Order users search")
@Slf4j
public abstract class UserSorter extends FeaturerTwins {
    public Function<Specification<UserEntity>, Specification<UserEntity>> createSort(HashMap<String, String> userSorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, userSorterParams);
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Function<Specification<UserEntity>, Specification<UserEntity>> createSort(Properties properties) throws ServiceException;

}
