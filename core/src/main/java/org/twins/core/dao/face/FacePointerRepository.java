package org.twins.core.dao.face;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacePointerRepository extends JpaRepository<FacePointerEntity, UUID> {
}
