package org.twins.core.dao.specifications;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommonSpecificationIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = TestUuidEntity.class)
    @EnableJpaRepositories(basePackageClasses = TestUuidRepository.class)
    static class Config {
    }

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestUuidRepository repository;

//    @BeforeAll
//    public static void init(@Autowired DataSource dataSource) throws Exception {
//        try (var conn = dataSource.getConnection()) {
//            var stmt = conn.createStatement();
//            stmt.execute("CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array uuid[]) RETURNS boolean AS $$\n" +
//                    "SELECT uuid_val = ANY(uuid_array);\n" +
//                    "$$ LANGUAGE sql IMMUTABLE;");
//            stmt.execute("CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array text) RETURNS boolean AS $$\n" +
//                    "SELECT uuid_val = ANY(uuid_array::uuid[]);\n" +
//                    "$$ LANGUAGE sql IMMUTABLE;");
//        }
//    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Test
    public void testCheckUuidIn_RealExecution() {
        UUID matchUuid = UUID.randomUUID();
        UUID otherUuid = UUID.randomUUID();

        TestUuidEntity e1 = new TestUuidEntity();
        e1.setId(matchUuid);
        repository.save(e1);

        TestUuidEntity e2 = new TestUuidEntity();
        e2.setId(otherUuid);
        repository.save(e2);

        Specification<TestUuidEntity> spec = CommonSpecification.checkUuidIn(Collections.singletonList(matchUuid), false, false, "id");
        List<TestUuidEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(matchUuid, result.get(0).getId());
    }

    @Test
    public void testCheckUuidIn_Not_RealExecution() {
        UUID matchUuid = UUID.randomUUID();
        UUID otherUuid = UUID.randomUUID();

        TestUuidEntity e1 = new TestUuidEntity();
        e1.setId(matchUuid);
        repository.save(e1);

        TestUuidEntity e2 = new TestUuidEntity();
        e2.setId(otherUuid);
        repository.save(e2);

        Specification<TestUuidEntity> spec = CommonSpecification.checkUuidIn(Collections.singletonList(matchUuid), true, false, "id");
        List<TestUuidEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(otherUuid, result.get(0).getId());
    }
}

@Entity
@Table(name = "test_uuid_entity")
class TestUuidEntity {
    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

interface TestUuidRepository extends JpaRepository<TestUuidEntity, UUID>, JpaSpecificationExecutor<TestUuidEntity> {
}