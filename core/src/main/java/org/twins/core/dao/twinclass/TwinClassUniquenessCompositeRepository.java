package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface TwinClassUniquenessCompositeRepository extends JpaRepository<TwinClassUniquenessEntity, UUID> {

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM twin t
            WHERE t.id != :excludeTwinId
              AND t.twin_class_id IN :scopeClassIds
              AND (:ownerUserId IS NULL OR t.owner_user_id = :ownerUserId)
              AND (:ownerBusinessAccountId IS NULL OR t.owner_business_account_id = :ownerBusinessAccountId)
              AND NOT EXISTS (
                  SELECT 1 FROM twin_status ts
                  WHERE ts.id = t.twin_status_id AND ts.deleted = true
              )
              AND (
                  SELECT COUNT(DISTINCT tfs.twin_class_field_id)
                  FROM twin_field_simple tfs
                  WHERE tfs.twin_id = t.id
                    AND tfs.twin_class_field_id IN :fieldIds
                    AND tfs.value IN :values
              ) = :fieldCount
        )
        """, nativeQuery = true)
    boolean existsBySimpleFieldsComposite(
            @Param("excludeTwinId") UUID excludeTwinId,
            @Param("scopeClassIds") Collection<UUID> scopeClassIds,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("values") Collection<String> values,
            @Param("fieldCount") int fieldCount
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM twin t
            WHERE t.id != :excludeTwinId
              AND t.twin_class_id IN :scopeClassIds
              AND (:ownerUserId IS NULL OR t.owner_user_id = :ownerUserId)
              AND (:ownerBusinessAccountId IS NULL OR t.owner_business_account_id = :ownerBusinessAccountId)
              AND NOT EXISTS (
                  SELECT 1 FROM twin_status ts
                  WHERE ts.id = t.twin_status_id AND ts.deleted = true
              )
              AND (
                  SELECT COUNT(DISTINCT tfd.twin_class_field_id)
                  FROM twin_field_decimal tfd
                  WHERE tfd.twin_id = t.id
                    AND tfd.twin_class_field_id IN :fieldIds
                    AND tfd.value IN :values
              ) = :fieldCount
        )
        """, nativeQuery = true)
    boolean existsByDecimalFieldsComposite(
            @Param("excludeTwinId") UUID excludeTwinId,
            @Param("scopeClassIds") Collection<UUID> scopeClassIds,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("values") Collection<String> values,
            @Param("fieldCount") int fieldCount
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM twin t
            WHERE t.id != :excludeTwinId
              AND t.twin_class_id IN :scopeClassIds
              AND (:ownerUserId IS NULL OR t.owner_user_id = :ownerUserId)
              AND (:ownerBusinessAccountId IS NULL OR t.owner_business_account_id = :ownerBusinessAccountId)
              AND NOT EXISTS (
                  SELECT 1 FROM twin_status ts
                  WHERE ts.id = t.twin_status_id AND ts.deleted = true
              )
              AND (
                  SELECT COUNT(DISTINCT tft.twin_class_field_id)
                  FROM twin_field_timestamp tft
                  WHERE tft.twin_id = t.id
                    AND tft.twin_class_field_id IN :fieldIds
                    AND tft.value IN :values
              ) = :fieldCount
        )
        """, nativeQuery = true)
    boolean existsByTimestampFieldsComposite(
            @Param("excludeTwinId") UUID excludeTwinId,
            @Param("scopeClassIds") Collection<UUID> scopeClassIds,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("values") Collection<String> values,
            @Param("fieldCount") int fieldCount
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM twin t
            WHERE t.id != :excludeTwinId
              AND t.twin_class_id IN :scopeClassIds
              AND (:ownerUserId IS NULL OR t.owner_user_id = :ownerUserId)
              AND (:ownerBusinessAccountId IS NULL OR t.owner_business_account_id = :ownerBusinessAccountId)
              AND NOT EXISTS (
                  SELECT 1 FROM twin_status ts
                  WHERE ts.id = t.twin_status_id AND ts.deleted = true
              )
              AND (
                  SELECT COUNT(DISTINCT tfd.twin_class_field_id)
                  FROM twin_field_datalist tfd
                  WHERE tfd.twin_id = t.id
                    AND tfd.twin_class_field_id IN :fieldIds
                    AND tfd.datalist_option_id IN :optionIds
              ) = :fieldCount
        )
        """, nativeQuery = true)
    boolean existsByDatalistFieldsComposite(
            @Param("excludeTwinId") UUID excludeTwinId,
            @Param("scopeClassIds") Collection<UUID> scopeClassIds,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("optionIds") Collection<UUID> optionIds,
            @Param("fieldCount") int fieldCount
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM twin t
            WHERE t.id != :excludeTwinId
              AND t.twin_class_id IN :scopeClassIds
              AND (:ownerUserId IS NULL OR t.owner_user_id = :ownerUserId)
              AND (:ownerBusinessAccountId IS NULL OR t.owner_business_account_id = :ownerBusinessAccountId)
              AND NOT EXISTS (
                  SELECT 1 FROM twin_status ts
                  WHERE ts.id = t.twin_status_id AND ts.deleted = true
              )
              AND (
                  SELECT COUNT(DISTINCT tfu.twin_class_field_id)
                  FROM twin_field_user tfu
                  WHERE tfu.twin_id = t.id
                    AND tfu.twin_class_field_id IN :fieldIds
                    AND tfu.user_id IN :userIds
              ) = :fieldCount
        )
        """, nativeQuery = true)
    boolean existsByUserFieldsComposite(
            @Param("excludeTwinId") UUID excludeTwinId,
            @Param("scopeClassIds") Collection<UUID> scopeClassIds,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("userIds") Collection<UUID> userIds,
            @Param("fieldCount") int fieldCount
    );
}
