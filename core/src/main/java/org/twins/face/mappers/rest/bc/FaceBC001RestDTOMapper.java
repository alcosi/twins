package org.twins.face.mappers.rest.bc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.bc.FaceBC001Entity;
import org.twins.face.dto.rest.bc.FaceBC001DTOv1;
import org.twins.face.service.bc.FaceBC001Service;

@Component
@RequiredArgsConstructor
public class FaceBC001RestDTOMapper extends RestSimpleDTOMapper<FaceBC001Entity, FaceBC001DTOv1> {

    protected final FaceRestDTOMapper faceRestDTOMapper;
    protected final FaceBC001Service faceBC001Service;
    protected final FaceBC001ItemRestDTOMapper faceBC001ItemRestDTOMapper;

    @Override
    public void map(FaceBC001Entity src, FaceBC001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        var listOfPairs = faceBC001Service.getBC001ItemToTwinPairs(src);
        dst.setItems(faceBC001ItemRestDTOMapper.convertCollection(listOfPairs, mapperContext));
    }
}
