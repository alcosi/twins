package org.twins.core.dao.usergroup;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserGroupMapRepository extends CrudRepository<UserGroupMapEntity, UUID>, JpaSpecificationExecutor<UserGroupMapEntity> {
    @Query(value = "with expected as (" +
            "    select" +
            "        a.user_group_id," +
            "        tc.domain_id," +
            "        t.owner_business_account_id as business_account_id," +
            "        t.assigner_user_id as user_id," +
            "        count(*) as expected_involves" +
            "    from twin t" +
            "             join twin_class tc" +
            "                  on tc.id = t.twin_class_id" +
            "             join user_group_involve_assignee a" +
            "                  on a.propagation_by_twin_class_id = t.twin_class_id" +
            "                      and (a.propagation_by_twin_status_id is null" +
            "                          or a.propagation_by_twin_status_id = t.twin_status_id)" +
            "    where t.assigner_user_id is not null" +
            "    group by" +
            "        a.user_group_id," +
            "        tc.domain_id," +
            "        t.owner_business_account_id," +
            "        t.assigner_user_id" +
            ")," +
            "     actual as (" +
            "         select" +
            "             user_group_id," +
            "             domain_id," +
            "             business_account_id," +
            "             user_id," +
            "             involves_count" +
            "         from user_group_map" +
            "     ) " +
            "select count(*) " +
            "from expected e" +
            "         full join actual a" +
            "                   on a.user_group_id = e.user_group_id" +
            "                       and a.domain_id = e.domain_id" +
            "                       and a.business_account_id = e.business_account_id" +
            "                       and a.user_id = e.user_id " +
            "where coalesce(a.involves_count, 0) <> coalesce(e.expected_involves, 0);", nativeQuery = true)
    long countInvalidInvolvesCount();

    @Query("select ugm.id from UserGroupMapEntity ugm where ugm.businessAccountId = :businessAccountId and ugm.userGroup.domainId = :domainId and ugm.userGroup.userGroupTypeId = :type")
    List<UUID> findAllByBusinessAccountIdAndDomainIdAndType(UUID businessAccountId, UUID domainId, String type);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndBusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID businessAccountId, UUID domainId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID domainId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndDomainId(UUID userId, UUID userGroupId, UUID domainId);

    @Query("""
                select distinct ugm.userId
                from UserGroupMapEntity ugm
                where ugm.userGroupId in :userGroupIds
                  and (ugm.addedManually or ugm.involvesCount > 0)
                  and ugm.domainId = :domainId
                  and (
                        ugm.businessAccountId is null
                        or ugm.businessAccountId = :businessAccountId
                      )
            """)
    Set<UUID> getUsers(UUID domainId, UUID businessAccountId, Set<UUID> userGroupIds);

    @Query("""
                select ugm
                from UserGroupMapEntity ugm
                where ugm.userId in :userIdSet
                  and (ugm.addedManually or ugm.involvesCount > 0)
                  and ugm.domainId = :domainId
                  and (
                        ugm.businessAccountId is null
                        or ugm.businessAccountId = :businessAccountId
                      )
            """)
    List<UserGroupMapEntity> getGroups(UUID domainId, UUID businessAccountId, Set<UUID> userIdSet);
}
