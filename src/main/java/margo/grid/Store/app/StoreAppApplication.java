package margo.grid.store.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("margo.grid.store.app.entity")
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories("margo.grid.store.app.repository")
public class StoreAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreAppApplication.class, args);
	}

}
