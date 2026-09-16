package org.twins.core.mappers.rest.usage;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.usage.Usage;
import org.twins.core.dto.rest.usage.UsageDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UsagesMode;

@Component
@MapperModeBinding(modes = UsagesMode.class)
public class UsageRestDTOMapper extends RestSimpleDTOMapper<Usage, UsageDTOv1> {

    @Override
    public void map(Usage src, UsageDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(UsagesMode.SHORT)) {
            case SHORT, DETAILED ->
                    dst
                            .setId(src.getId())
                            .setUsageType(src.getUsageType());
        }
        if (mapperContext.hasMode(UsagesMode.DETAILED) && src.getEntity() != null)
            mapperContext.addRelatedObject(src.getEntity());
    }
}
