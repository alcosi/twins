package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.BasicSearch;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinSearchRqDTOMapper extends RestSimpleDTOMapper<TwinSearchRqDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchRqDTOv1 src, BasicSearch dst, MapperProperties mapperProperties) throws Exception {
        dst
                .setTwinClassIdList(src.getTwinClassIdList())
                .setStatusIdList(src.getStatusIdList())
                .setAssignerUserIdList(src.getAssignerUserIdList())
                .setSpaceTwinIdList(src.getSpaceTwinIdList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList());
    }

}
