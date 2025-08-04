package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TwinPointerRepository extends JpaRepository<TwinPointerEntity, UUID> {
}
