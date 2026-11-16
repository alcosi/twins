package org.twins.core.dao.comment;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinCommentSearchRepository extends CrudRepository<TwinCommentSearchEntity, UUID>, JpaSpecificationExecutor<TwinCommentSearchEntity> {
}
