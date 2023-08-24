package org.twins.core.dao.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "user")
public class UserEntity {
    @Id
    private UUID id;
}

