package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.dto.rest.attachment.AttachmentQuotasBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentQuotasMode.class})
public class AttachmentQuotasRestDTOMapper extends RestSimpleDTOMapper<AttachmentQuotas, AttachmentQuotasBaseDTOv1> {

    @Override
    public void map(AttachmentQuotas src, AttachmentQuotasBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentQuotasMode.DETAILED)) {
            case DETAILED:
                dst
                        .setQuotaCount(src.getQuotaCount())
                        .setQuotaSize(src.getQuotaSize())
                        .setUsedCount(src.getUsedCount())
                        .setUsedSize(src.getUsedSize());
            case SHORT:
                dst
                        .setQuotaCount(src.getQuotaCount())
                        .setQuotaSize(src.getQuotaSize());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(AttachmentQuotasMode.HIDE);
    }

}
