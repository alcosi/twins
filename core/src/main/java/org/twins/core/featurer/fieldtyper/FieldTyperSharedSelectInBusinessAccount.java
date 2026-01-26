package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1308,
        name = "Select (shared in business account)",
        description = "")
@Slf4j
public class FieldTyperSharedSelectInBusinessAccount extends FieldTyperList {
    @Autowired
    AuthService authService;

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        FieldDescriptorList fieldDescriptorList = (FieldDescriptorList) super.getFieldDescriptor(twinClassFieldEntity, properties);
        fieldDescriptorList
                .supportCustom(false)
                .multiple(false)
                .options(dataListService.findByDataListIdAndNotUsedInBusinessAccount(dataListId.extract(properties), twinClassFieldEntity.getId(), getBusinessAccountId(twinClassFieldEntity)));
        return fieldDescriptorList;
    }

    @Override
    public UUID checkOptionAllowed(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        if (dataListService.findByDataListIdAndNotUsedInBusinessAccount(dataListOptionEntity.getDataListId(), twinClassFieldEntity.getId(), getBusinessAccountId(twinClassFieldEntity))
                .stream().noneMatch(o -> o.getId().equals(dataListOptionEntity.getId())))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_ALREADY_IN_USE, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be filled with optionId[" + dataListOptionEntity.getId() + "] cause it is already in use in businessAccount");
        return super.checkOptionAllowed(twinEntity, twinClassFieldEntity, dataListOptionEntity);
    }

    public UUID getBusinessAccountId(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.isBusinessAccountSpecified())
            throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be processed without businessAccount");
        return apiUser.getBusinessAccountId();
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
