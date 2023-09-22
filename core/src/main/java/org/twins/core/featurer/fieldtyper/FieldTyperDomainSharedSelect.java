package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.EntitySmartService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1307,
        name = "FieldTyperDomainSharedSelect",
        description = "")
@Slf4j
public class FieldTyperDomainSharedSelect extends FieldTyperList {
    @Autowired
    public FieldTyperDomainSharedSelect(DataListOptionRepository dataListOptionRepository, EntitySmartService entitySmartService) {
        super(dataListOptionRepository, entitySmartService);
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
