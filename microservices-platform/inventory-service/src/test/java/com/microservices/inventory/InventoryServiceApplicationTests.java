package com.microservices.inventory;

import com.microservice.inventory.InventoryServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = InventoryServiceApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class InventoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
