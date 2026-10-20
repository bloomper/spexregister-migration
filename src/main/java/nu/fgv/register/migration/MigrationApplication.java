package nu.fgv.register.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.Objects;

@SpringBootApplication
public class MigrationApplication {

	static void main(String[] args) {
		final String spexregisterServer = Objects.requireNonNullElse(System.getenv("SPEXREGISTER_SERVER"), "../spexregister-server");
		System.setProperty("SPEXREGISTER_SERVER", Path.of(spexregisterServer).toAbsolutePath().normalize().toString());
		SpringApplication.run(MigrationApplication.class, args);
	}

}
