package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "twin_field")
@SqlResultSetMapping(
        name="TwinFieldsAllResult",
        entities={
                @EntityResult(
                        entityClass = TwinFieldEntity.class,
                        fields={
                                @FieldResult(name="id", column="id"),
                                @FieldResult(name="twinId", column="twin_id"),
                                @FieldResult(name="twinClassFieldId", column="twin_class_field_id"),
                                @FieldResult(name="value", column="value"),
                        })})
@NamedNativeQuery(name = "twinAllFields", query = "select v.id as id, :twin_id as twin_id, c.id as twin_class_field_id, v.value as value " +
        "from twin_class_field as c left join (select * from twin_field where twin_field.twin_id = :twin_id) v on v.twin_class_field_id = c.id " +
        "where c.twin_class_id = :twin_class_Id", resultClass = TwinFieldEntity.class) // not working, because mapping of null-id entities is not possible
public class TwinFieldEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;
}
