package com.equipmentrental.organizationcustomer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SchemaMigrationTest {
    @Test
    void contextLoadsWithValidatedFlywaySchema() {}
}
