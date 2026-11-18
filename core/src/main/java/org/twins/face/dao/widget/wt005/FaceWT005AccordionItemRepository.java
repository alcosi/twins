package org.twins.face.dao.widget.wt005;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceWT005AccordionItemRepository extends CrudRepository<FaceWT005AccordionItemEntity, UUID>, JpaSpecificationExecutor<FaceWT005AccordionItemEntity> {
    Collection<FaceWT005AccordionItemEntity> findByFaceWT005IdIn(Set<UUID> idSet);
}
