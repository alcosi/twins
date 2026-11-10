package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.Properties;

/**
 * Asks the field typer storage for the count GROUP BY expression.
 * Which column to group by lives on the storage, so a new storage must implement {@link TwinFieldStorage#countGroup}.
 */
@Component
@RequiredArgsConstructor
public class TwinFieldCountGroup {
    private final FeaturerService featurerService;

    public GroupExpressionProvider<TwinEntity> create(TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinFieldStorage storage = fieldTyper.getStorage(field);
        GroupExpressionProvider<TwinEntity> group = storage.countGroup(field.getId(), isMultipleSelect(fieldTyper, field));
        if (group == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " is not countable; " + field.logShort());
        }
        return group;
    }

    /**
     * A multiple select stores one row per chosen option, so the same twin matches the join
     * more than once. The datalist storage counts that twin once inside each option group.
     */
    private boolean isMultipleSelect(FieldTyper fieldTyper, TwinClassFieldEntity field) throws ServiceException {
        if (!(fieldTyper instanceof FieldTyperSelect)) {
            return false;
        }
        Properties properties = featurerService.extractProperties(fieldTyper, field.getFieldTyperParams());
        return FieldTyperSelect.multiple.extract(properties);
    }
}
