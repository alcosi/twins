package org.twins.core.dao.twinclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_owner_type")
@FieldNameConstants
public class TwinClassOwnerTypeEntity {
    @Id
    private String id;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;
}
