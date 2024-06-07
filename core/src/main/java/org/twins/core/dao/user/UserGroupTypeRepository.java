package org.twins.core.dao.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupTypeRepository extends CrudRepository<UserGroupTypeEntity, String> {
    @Override
    List<UserGroupTypeEntity> findAll();
}
