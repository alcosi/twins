package org.twins.core.featurer.comment.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_6001,
        name = "Unsorted",
        description = "")
public class CommentSorterStub extends CommentSorter {

    @Override
    public Function<Specification<TwinCommentEntity>, Specification<TwinCommentEntity>> createSort(Properties properties) throws ServiceException {
        return null;
    }
}
