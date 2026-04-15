package org.twins.core.dao.specifications;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class PostgresTestContainerTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    public static void setup() throws SQLException {
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array uuid[]) RETURNS boolean AS $$\n" +
                    "SELECT uuid_val = ANY(uuid_array);\n" +
                    "$$ LANGUAGE sql IMMUTABLE;");
            stmt.execute("CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array text) RETURNS boolean AS $$\n" +
                    "SELECT uuid_val = ANY(uuid_array::uuid[]);\n" +
                    "$$ LANGUAGE sql IMMUTABLE;");
        }
    }

    @Test
    public void testUuidInArray_WithUuidArray() throws SQLException {
        UUID uuid = UUID.randomUUID();
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT uuid_in_array(?::uuid, ?::uuid[])");
            pstmt.setObject(1, uuid);
            pstmt.setArray(2, conn.createArrayOf("uuid", new Object[]{uuid, UUID.randomUUID()}));
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertTrue(rs.getBoolean(1));
        }
    }

    @Test
    public void testUuidInArray_WithTextArray() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String textArray = "{" + uuid + "," + UUID.randomUUID() + "}";
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT uuid_in_array(?::uuid, ?)");
            pstmt.setObject(1, uuid);
            pstmt.setString(2, textArray);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertTrue(rs.getBoolean(1));
        }
    }

    @Test
    public void testUuidInArray_NoMatch() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String textArray = "{" + UUID.randomUUID() + "," + UUID.randomUUID() + "}";
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT uuid_in_array(?::uuid, ?)");
            pstmt.setObject(1, uuid);
            pstmt.setString(2, textArray);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertFalse(rs.getBoolean(1));
        }
    }
}
