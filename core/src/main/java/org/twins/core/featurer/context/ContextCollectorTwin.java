package org.twins.core.featurer.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_4904,
        name = "Context collector twin",
        description = "Collect form twin (id, name, description)")
@Slf4j
public class ContextCollectorTwin extends ContextCollectorTwinBase {

    @Override
    protected TwinEntity resolveTwin(ContextRoot root, Properties properties) {
        return root.getTwin();
    }
}
