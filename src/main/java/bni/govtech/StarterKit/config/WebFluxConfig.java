package bni.govtech.StarterKit.config;

import bni.govtech.StarterKit.handler.GlobalExceptionHandler;
import bni.govtech.StarterKit.util.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class WebFluxConfig {

    @Value("${app.isOtp:false}")
    private boolean isOtp;

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "app.isOtp", havingValue = "true")
    public ApplicationRunner initMailSender(JavaMailSender mailSender) {
        return args -> EmailUtil.setMailSender(mailSender);
    }

    @Bean
    public Scheduler emailScheduler() {
        return Schedulers.newBoundedElastic(
                50, // Adjust based on your SMTP server capacity
                10000, // Queue size
                "email-pool"
        );
    }
}