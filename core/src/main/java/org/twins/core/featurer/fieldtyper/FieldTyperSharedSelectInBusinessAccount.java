package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1308,
        name = "FieldTyperBusinessAccountSharedSelect",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInBusinessAccount extends FieldTyperList {
    @Autowired
    AuthService authService;

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        dataListService.findEntitySafe(listId);
        return new FieldDescriptorList()
                .supportCustom(false)
                .multiple(false)
                .options(dataListOptionRepository.findByDataListIdAndNotUsedInBusinessAccount(listId, twinClassFieldEntity.getId(), getBusinessAccountId(twinClassFieldEntity)));
    }

    @Override
    public UUID checkOptionAllowed(TwinFieldEntity twinFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        if (dataListOptionRepository.findByDataListIdAndNotUsedInBusinessAccount(dataListOptionEntity.getDataListId(), twinFieldEntity.getTwinClassFieldId(), getBusinessAccountId(twinFieldEntity.getTwinClassField()))
                .stream().noneMatch(o -> o.getId().equals(dataListOptionEntity.getId())))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " can not be filled with optionId[" + dataListOptionEntity.getId() + "] cause it is already in use in businessAccount");
        return super.checkOptionAllowed(twinFieldEntity, dataListOptionEntity);
    }

    public UUID getBusinessAccountId(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.getBusinessAccount() == null)
            throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be processed without businessAccount");
        return apiUser.getBusinessAccount().getId();
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
