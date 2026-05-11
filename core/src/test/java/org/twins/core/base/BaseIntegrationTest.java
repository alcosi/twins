package org.twins.core.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.twins.core.containers.PostgresContainers;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ImportTestcontainers(PostgresContainers.class)
public abstract class BaseIntegrationTest {
}
