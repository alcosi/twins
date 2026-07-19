package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FactoryConditionExportService extends EntityExportService<TwinFactoryConditionEntity> {

    public String exportCollectionToSql(Collection<TwinFactoryConditionEntity> conditions) throws ServiceException {
        if (CollectionUtils.isEmpty(conditions)) return "";
        var sqlParts = new StringList();
        sqlParts.addNotBlank(buildInsertsSorted(conditions, TwinFactoryConditionEntity::getId));
        return String.join("\n", sqlParts);
    }
}
