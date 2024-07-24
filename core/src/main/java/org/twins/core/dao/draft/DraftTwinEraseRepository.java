package org.twins.core.dao.draft;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DraftTwinEraseRepository extends CrudRepository<DraftTwinEraseEntity, DraftTwinEraseEntity.PK>, JpaSpecificationExecutor<DraftTwinEraseEntity> {

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "insert into draft_twin_erase (draft_id, twin_id, erase_ready, reason_twin_id, twin_erase_reason_id) " +
                   "select :draftId, id, false, :twinId, 'CHILD' " +
                   "from twin " +
                   "where hierarchy_tree ~ :twin_ltree::lquery " +
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

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "insert into draft_twin_erase (draft_id, twin_id, erase_ready, reason_twin_id, twin_erase_reason_id) " +
                   "select :draftId, tl.src_twin_id, false, :twinId, 'LINK' " +
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
                   "                    and dtl.cud_id = 'UPDATE')")
   void addLinked(@Param("draftId") UUID draftId, @Param("twinId") UUID twinId);

   List<DraftTwinEraseEntity> findByDraftIdAndEraseReadyFalse(UUID draftId);
}
