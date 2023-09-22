package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1308,
        name = "FieldTyperBusinessAccountSharedSelect",
        description = "")
@Slf4j
public class FieldTyperBusinessAccountSharedSelect extends FieldTyperList {
    final AuthService authService;

    @Autowired
    public FieldTyperBusinessAccountSharedSelect(DataListOptionRepository dataListOptionRepository, EntitySmartService entitySmartService, @Lazy AuthService authService) {
        super(dataListOptionRepository, entitySmartService);
        this.authService = authService;
    }

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.getBusinessAccount() == null)
            throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassFieldEntity.logShort() + " can not be displayed without of businessAccount");
        return new FieldDescriptorList()
                .supportCustom(false)
                .multiple(false)
                .options(dataListOptionRepository.findByDataListIdAndNotUsedInBusinessAccount(listId, twinClassFieldEntity.getId(), apiUser.getBusinessAccount().getId()));
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return false;
    }
}
