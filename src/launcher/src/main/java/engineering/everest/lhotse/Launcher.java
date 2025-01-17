package engineering.everest.lhotse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    scanBasePackages = {
        "engineering.everest.starterkit",
        "engineering.everest.axon",
        "engineering.everest.lhotse"
    })
@EnableScheduling
@SuppressWarnings("PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal")
public class Launcher {

    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
    }
}
