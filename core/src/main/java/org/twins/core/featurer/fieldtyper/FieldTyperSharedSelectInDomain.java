package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.EntitySmartService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1307,
        name = "FieldTyperSharedSelectInDomain",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInDomain extends FieldTyperList {
    public FieldTyperSharedSelectInDomain(DataListOptionRepository dataListOptionRepository, DataListRepository dataListRepository, EntitySmartService entitySmartService) {
        super(dataListOptionRepository, dataListRepository, entitySmartService);
    }

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        UUID listId = listUUID.extract(properties);
        return new FieldDescriptorList()
                .supportCustom(false)
                .multiple(false)
                .options(dataListOptionRepository.findByDataListIdAndNotUsedInDomain(listId, twinClassFieldEntity.getId()));
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
