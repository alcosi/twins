package org.twins.core.dao.view;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.AccessOrder;

import java.util.UUID;

@Entity
@Data
@Table(name = "view")
public class ViewEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "access_order")
    @Enumerated(EnumType.STRING)
    private AccessOrder accessOrder;
}
