package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv2;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TemporalIdContext;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkAddTemporalRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv2, TwinLinkEntity> {

    private final AuthService authService;
    private final TemporalIdContext temporalIdContext;

    @Override
    public void map(TwinLinkAddDTOv2 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setLinkId(src.getLinkId())
                .setCreatedByUserId(apiUser.getUser().getId());
        if (StringUtils.isBlank(src.getDstTwinId()))
            throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                    "Invalid dstTwinId format: " + src.getDstTwinId() + ". Expected UUID or temporalId:XXX reference.");
        if (src.getDstTwinId().startsWith(TemporalIdContext.TEMPORAL_ID_PREFIX)) {
            String key = src.getDstTwinId().substring(TemporalIdContext.TEMPORAL_ID_PREFIX.length());
            UUID resolvedId = temporalIdContext.resolve(key);
            if (resolvedId == null) {
                throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                        "Temporal ID reference not found: " + key);
            }
            dst.setDstTwinId(resolvedId);
        } else {
            try {
                UUID dstTwinId = UUID.fromString(src.getDstTwinId());
                dst.setDstTwinId(dstTwinId);
            } catch (IllegalArgumentException e) {
                throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                        "Invalid dstTwinId format: " + src.getDstTwinId() + ". Expected UUID or temporalId:XXX reference.");
            }
        }
    }
}
