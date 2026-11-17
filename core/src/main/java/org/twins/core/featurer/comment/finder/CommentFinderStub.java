package org.twins.core.featurer.comment.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5901,
        name = "Stub",
        description = "")
public class CommentFinderStub extends CommentFinder {
    @Override
    public void concatSearch(Properties properties, CommentSearch commentSearch, Map<String, String> namedParamsMap) throws ServiceException {
    }
}
