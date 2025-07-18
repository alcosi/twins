package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueId;


@Component
@Featurer(id = FeaturerTwins.ID_1334,
        name = "Twin class id field",
        description = "Field typer for twin class id field")
public class FieldTyperTwinClassIdField   {
}
