package org.twins.core.mappers.rest.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldPlugBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldPlugRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldPlugBaseDTOv1, TwinClassFieldPlugEntity> {

    private final EntityManager entityManager;

    @Override
    public void map(TwinClassFieldPlugBaseDTOv1 src, TwinClassFieldPlugEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setTwinClassFieldId(src.getTwinClassFieldId())
                // setting references to use save method without problems
                .setTwinClass(src.getTwinClassId() != null ? entityManager.getReference(TwinClassEntity.class, src.getTwinClassId()) : null)
                .setTwinClassField(src.getTwinClassFieldId() != null ? entityManager.getReference(TwinClassFieldEntity.class, src.getTwinClassFieldId()) : null);
    }
}
