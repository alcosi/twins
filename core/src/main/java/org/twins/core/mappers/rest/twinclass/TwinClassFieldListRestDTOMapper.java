package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.mappers.rest.RestDTOConverter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TwinClassFieldListRestDTOMapper implements RestDTOConverter<List<TwinClassFieldEntity>, List<TwinClassFieldDTOv1>> {
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    public List<TwinClassFieldDTOv1> convert(List<TwinClassFieldEntity> twinClassFieldEntityList) throws ServiceException {
        List<TwinClassFieldDTOv1> ret = new ArrayList<>();
        for (TwinClassFieldEntity entity : twinClassFieldEntityList) {
            ret.add(twinClassFieldRestDTOMapper.convert(entity));
        }
        return ret;
    }
}
