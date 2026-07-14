package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;

/**
 * Like {@link FillerBasicsAssigneeFromContext} (2324), but resolves {@code assigneeField} on the
 * <strong>output</strong> twin of the factory item (DB + in-batch uncommitted values via {@link org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputFields}),
 * not on the context twin. Use when the user id lives on the same twin being updated.
 */
@Component
@Featurer(
        id = FeaturerTwins.ID_2352,
        name = "Basics assignee from item output field",
        description = "Fills output twin assignee from a user field on the same item output twin"
)
public class FillerBasicsAssigneeFromItemOutput extends FillerBasicsAssigneeFromContext {

    @Override
    public FieldLookuperNearest getLookuper() throws ServiceException {
        return fieldLookupers.getFromItemOutputFields();
    }
}
