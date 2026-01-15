package org.cambium.featurer.dao;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "featurer_injection")
public class FeaturerInjectionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "injector_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity injectorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "injector_params", columnDefinition = "hstore")
    private HashMap<String, String> injectorParams;

    @Column(name = "description")
    private String description;
}
