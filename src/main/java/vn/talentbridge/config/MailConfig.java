package vn.talentbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class MailConfig {

    @Bean(name = "passwordResetRequestExecutor")
    public Executor passwordResetRequestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("password-reset-request-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Bean(name = "passwordResetMailExecutor")
    public Executor passwordResetMailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("password-reset-mail-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}")
            String host,
            @Value("${spring.mail.port:587}")
            int port,
            @Value("${spring.mail.username:}")
            String username,
            @Value("${spring.mail.password:}")
            String password,
            @Value("${spring.mail.properties.mail.smtp.connectiontimeout:10000}")
            int connectionTimeout,
            @Value("${spring.mail.properties.mail.smtp.timeout:10000}")
            int readTimeout,
            @Value("${spring.mail.properties.mail.smtp.writetimeout:10000}")
            int writeTimeout
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
        properties.put("mail.smtp.connectiontimeout", Integer.toString(connectionTimeout));
        properties.put("mail.smtp.timeout", Integer.toString(readTimeout));
        properties.put("mail.smtp.writetimeout", Integer.toString(writeTimeout));
        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        return mailSender;
    }
}
