package org.twins.face.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceWT004AccordionItemRepository extends CrudRepository<FaceWT004AccordionItemEntity, UUID>, JpaSpecificationExecutor<FaceWT004AccordionItemEntity> {
    Collection<FaceWT004AccordionItemEntity> findByFaceIdIn(Set<UUID> idSet);
}
