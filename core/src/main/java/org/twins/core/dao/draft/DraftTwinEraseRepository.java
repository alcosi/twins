package org.twins.core.dao.draft;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface DraftTwinEraseRepository extends CrudRepository<DraftTwinEraseEntity, DraftTwinEraseEntity.PK>, JpaSpecificationExecutor<DraftTwinEraseEntity> {

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "insert into draft_twin_erase (draft_id, time_in_millis, twin_id, draft_twin_erase_status_id, reason_twin_id, twin_erase_reason_id) " +
                   "select :draftId, EXTRACT(epoch FROM  current_timestamp) * 1000, id, 'UNDETECTED', :twinId, 'CHILD' " +
                   "from twin " +
                   "where hierarchy_tree ~ cast(:twin_ltree as lquery) " +
                   "  and not hierarchy_tree <@ " + // we will exclude childes with changed head
                   "    any (select t.hierarchy_tree " +
                   "         from draft_twin_persist dtp, " +
                   "              twin t " +
                   "         where dtp.twin_id = t.id " +
                   "           and t.head_twin_id = :twinId " +
                   "           and dtp.draft_id = :draftId " +
                   "           and dtp.create_else_update = false " +
                   "           and dtp.head_twin_id != t.head_twin_id) on conflict do nothing;")
   void addChildTwins(@Param("draftId") UUID draftId, @Param("twinId") UUID twinId, @Param("twin_ltree") String twinLTree);

   @Transactional(propagation = Propagation.REQUIRES_NEW)
   @Modifying
   @Query(nativeQuery = true, value =
           "insert into draft_twin_erase (draft_id, time_in_millis, twin_id, draft_twin_erase_status_id, reason_twin_id, twin_erase_reason_id) " +
                   "select :draftId, EXTRACT(epoch FROM  current_timestamp) * 1000, tl.src_twin_id, 'UNDETECTED', :twinId, 'LINK' " +
                   "from twin_link tl, " +
                   "     link l " +
                   "where tl.dst_twin_id = :twinId " +
                   "  and tl.link_id = l.id\n" +
                   "  and l.link_strength_id in ('MANDATORY', 'OPTIONAL_BUT_DELETE_CASCADE') " +
                   "  and tl.id not in (select dtl.twin_link_id " +
                   "                    from draft_twin_link dtl, " +
                   "                         twin_link tl " +
                   "                    where dtl.twin_link_id = tl.id " +
                   "                      and dtl.dst_twin_id != tl.dst_twin_id " +
                   "                    and dtl.cud_id = 'UPDATE') on conflict do nothing;")
   void addLinked(@Param("draftId") UUID draftId, @Param("twinId") UUID twinId);

   List<DraftTwinEraseEntity> findByDraftIdAndStatus(UUID draftId, DraftTwinEraseEntity.Status status);

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "delete from twin where id in (select twin_id from draft_twin_erase where draft_id = :draftId and erase_twin_status_id is null)")
   int commitEraseIrrevocable(@Param("draftId") UUID draftId);

   @Query(nativeQuery = true, value =
           "select string_agg(cast(twin_id as varchar), ', ') AS ids " +
                   "from draft_twin_erase where draft_id = :draftId and erase_twin_status_id is null " +
                   "group by draft_id;")
   String getIrrevocableDeleteIds(@Param("draftId") UUID draftId);

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "update twin set twin_status_id = erase_twin_status_id " +
                   "from draft_twin_erase dte " +
                   "where dte.draft_id = :draftId and dte.twin_id = twin.id and dte.erase_twin_status_id is not null;")
   int commitEraseByStatus(@Param("draftId") UUID draftId);

   Slice<DraftTwinEraseEntity> findByDraftIdAndEraseTwinStatusIdIsNotNullOrderByEraseTwinStatusId(UUID draftId, Pageable pageable);

}
