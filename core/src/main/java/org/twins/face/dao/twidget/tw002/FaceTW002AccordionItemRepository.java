package org.twins.face.dao.twidget.tw002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceTW002AccordionItemRepository extends CrudRepository<FaceTW002AccordionItemEntity, UUID>, JpaSpecificationExecutor<FaceTW002AccordionItemEntity> {
    Collection<FaceTW002AccordionItemEntity> findByFaceIdIn(Set<UUID> idSet);
}
