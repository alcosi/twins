package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field_search")
@FieldNameConstants
public class TwinClassFieldSearchEntity {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "force_sorting")
    private boolean forceSorting;

    @Column(name = "field_sorter_featurer_id")
    private int fieldSorterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_sorter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldSorterParams;
}