package org.twins.face.dao.navbar.nb001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceNB001MenuItemRepository extends CrudRepository<FaceNB001MenuItemEntity, UUID>, JpaSpecificationExecutor<FaceNB001MenuItemEntity> {
    Collection<FaceNB001MenuItemEntity> findByFaceIdInAndParentFaceMenuItemIdIsNull(Set<UUID> idSet);

    Collection<FaceNB001MenuItemEntity> findByParentFaceMenuItemIdIn(Set<UUID> idSet);
}
