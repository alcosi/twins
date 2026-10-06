package org.twins.core.mappers.rest.usage;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.usage.Usage;
import org.twins.core.dto.rest.usage.UsageDTOv1;
import org.twins.core.enums.usage.UsageType;
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
                            .setUsageType(convertUsageType(src.getUsageType()));
        }
        if (mapperContext.hasMode(UsagesMode.DETAILED) && src.getEntity() != null) {
            // the referencing entity must carry an explicit show mode of its own mapper class —
            // mappers with hideMode = hasModeOrEmpty(HIDE) drop entities that have no mode at all.
            // defaultShowMode is applied only when the caller did not configure that mode itself.
            mapperContext
                    .fork()
                    .setModeIfNotPresent(src.getUsageType().getDefaultShowMode())
                    .addRelatedObject(src.getEntity());
        }
    }

    /**
     * Explicit switch (no default) on purpose: adding a constant to the domain UsageType without
     * a client twin fails here at compile time instead of blowing up at runtime like valueOf(name).
     */
    private static UsageType convertUsageType(org.twins.core.domain.usage.UsageType usageType) {
        return switch (usageType) {
            case FACTORY_PIPELINE_NEXT_FACTORY -> UsageType.FACTORY_PIPELINE_NEXT_FACTORY;
            case FACTORY_PIPELINE_AFTER_COMMIT_FACTORY -> UsageType.FACTORY_PIPELINE_AFTER_COMMIT_FACTORY;
            case FACTORY_BRANCH_NEXT_FACTORY -> UsageType.FACTORY_BRANCH_NEXT_FACTORY;
            case TWINFLOW_TRANSITION_INBUILT_FACTORY -> UsageType.TWINFLOW_TRANSITION_INBUILT_FACTORY;
            case TWINFLOW_FACTORY_LAUNCHER -> UsageType.TWINFLOW_FACTORY_LAUNCHER;
        };
    }
}
