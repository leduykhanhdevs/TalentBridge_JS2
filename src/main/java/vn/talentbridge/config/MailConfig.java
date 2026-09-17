package vn.talentbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@EnableAsync
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}")
            String host,
            @Value("${spring.mail.port:587}")
            int port,
            @Value("${spring.mail.username:}")
            String username,
            @Value("${spring.mail.password:}")
            String password
    ) {
        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding(
                StandardCharsets.UTF_8.name()
        );

        Properties properties =
                mailSender.getJavaMailProperties();

        properties.put("mail.smtp.auth", "true");
        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        return mailSender;
    }
}