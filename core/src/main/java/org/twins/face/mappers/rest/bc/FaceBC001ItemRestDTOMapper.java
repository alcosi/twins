package org.twins.face.mappers.rest.bc;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.bc.FaceBC001ItemEntity;
import org.twins.face.dto.rest.bc.FaceBC001ItemDTOv1;

@Component
@RequiredArgsConstructor
public class FaceBC001ItemRestDTOMapper extends RestSimpleDTOMapper<Pair<FaceBC001ItemEntity, TwinEntity>, FaceBC001ItemDTOv1> {
    private final ResourceService resourceService;

    @Override
    public void map(Pair<FaceBC001ItemEntity, TwinEntity> src, FaceBC001ItemDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity twin = src.getSecond();
        FaceBC001ItemEntity item = src.getFirst();

        dst
                .setId(item.getId())
                .setOrder(item.getOrder())
                .setTwinId(twin.getId())
                .setLabel(I18nCacheHolder.addId(item.getLabelId()))
                .setIconUrl(resourceService.getResourceUri(item.getIconResource()));
    }
}
