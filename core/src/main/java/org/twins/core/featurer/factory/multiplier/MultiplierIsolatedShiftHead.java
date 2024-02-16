package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 2204,
        name = "MultiplierIsolatedShiftHead",
        description = "Output twin for each input. Output twin will be loaded from head")
public class MultiplierIsolatedShiftHead extends Multiplier {
    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            twinService.loadHeadForTwin(inputTwin);
            if (inputTwin.getHeadTwin() == null) {
                log.error(inputTwin.logShort() + " no head twin. Skipped by multiplier");
                continue;
            }
            TwinEntity headTwin = inputTwin.getHeadTwin();
            TwinUpdate twinUpdate = new TwinUpdate();
            twinUpdate
                    .setDbTwinEntity(headTwin) // original twin
                    .setTwinEntity(headTwin.clone()); // collecting updated in new twin
            ret.add(new FactoryItem()
                    .setOutput(twinUpdate)
                    .setContextFactoryItemList(List.of(inputItem)));
        }
        return ret;
    }
}
