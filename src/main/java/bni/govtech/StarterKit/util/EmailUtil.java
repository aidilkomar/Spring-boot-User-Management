package bni.govtech.StarterKit.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EmailUtil {

    @Setter
    private static JavaMailSender mailSender;

    // Configuration for throughput optimization
    private static final int MAX_CONCURRENT_EMAILS = 20;
    private static final AtomicInteger activeEmailCount = new AtomicInteger(0);
    private static final Scheduler EMAIL_SCHEDULER =
            Schedulers.newBoundedElastic(
                    MAX_CONCURRENT_EMAILS,
                    1000,
                    "email-scheduler"
            );

    public static Mono<Void> sendEmail(
            List<String> toEmails,
            String subject,
            String body,
            List<String> ccEmails,
            List<String> bccEmails,
            String replyTo,
            boolean isHtml) {

        return validateAndExecute(() -> {
            if (isHtml || ccEmails != null || bccEmails != null || replyTo != null) {
                sendMimeEmail(toEmails, subject, body, ccEmails, bccEmails, replyTo, isHtml, null);
            } else {
                sendSimpleEmail(toEmails, subject, body);
            }
        });
    }

    public static Mono<Void> sendEmailWithAttachments(
            List<String> toEmails,
            String subject,
            String body,
            boolean isHtml,
            List<String> ccEmails,
            List<String> bccEmails,
            String replyTo,
            Map<String, InputStreamSource> attachments) {

        return validateAndExecute(() -> {
            sendMimeEmail(toEmails, subject, body, ccEmails, bccEmails, replyTo, isHtml, attachments);
        });
    }

    public static Mono<Void> sendVerificationEmail(String toEmail, String token, String baseUrl) {
        String subject = "Verify your email";
        String verificationLink = baseUrl + "/verify?token=" + token;
        String body = String.format("""
            Please verify your account by clicking this link:
            %s
            
            If you didn't request this, please ignore this email.
            """, verificationLink);

        return sendEmail(List.of(toEmail), subject, body, null, null, null, false);
    }

    public static Mono<Void> sendOtpEmail(String toEmail, String otpCode, int validMinutes) {
        String subject = "Your OTP Code";
        String body = String.format("""
            Your OTP code is: %s
            Valid for %d minutes.
            
            If you didn't request this, please ignore this email.
            """, otpCode, validMinutes);

        return sendEmail(List.of(toEmail), subject, body, null, null, null, false);
    }

    private static Mono<Void> validateAndExecute(EmailOperation operation) {
        if (mailSender == null) {
            log.error("MailSender not initialized");
            return Mono.error(new IllegalStateException("MailSender is not initialized"));
        }

        if (activeEmailCount.get() >= MAX_CONCURRENT_EMAILS) {
            log.warn("Email service busy - active emails: {}", activeEmailCount.get());
            return Mono.error(new IllegalStateException("Email service temporarily unavailable"));
        }

        return Mono.<Void>fromRunnable(() -> {
                    activeEmailCount.incrementAndGet();
                    try {
                        operation.execute();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    } finally {
                        activeEmailCount.decrementAndGet();
                    }
                })
                .subscribeOn(EMAIL_SCHEDULER)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(e -> e instanceof MessagingException))
                .doOnError(e -> log.error("Failed to send email", e))
                .onErrorResume(e -> {
                    // Consider adding failed emails to a queue here
                    return Mono.empty(); // or implement your fallback strategy
                });
    }

    private static void sendSimpleEmail(List<String> toEmails, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmails.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private static void sendMimeEmail(
            List<String> toEmails,
            String subject,
            String body,
            List<String> ccEmails,
            List<String> bccEmails,
            String replyTo,
            boolean isHtml,
            Map<String, InputStreamSource> attachments) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, attachments != null, "UTF-8");

        helper.setTo(toEmails.toArray(new String[0]));
        if (ccEmails != null && !ccEmails.isEmpty()) {
            helper.setCc(ccEmails.toArray(new String[0]));
        }
        if (bccEmails != null && !bccEmails.isEmpty()) {
            helper.setBcc(bccEmails.toArray(new String[0]));
        }
        if (replyTo != null && !replyTo.isBlank()) {
            helper.setReplyTo(replyTo);
        }

        helper.setSubject(subject);
        helper.setText(body, isHtml);

        if (attachments != null) {
            for (Map.Entry<String, InputStreamSource> entry : attachments.entrySet()) {
                helper.addAttachment(entry.getKey(), entry.getValue());
            }
        }

        mailSender.send(mimeMessage);
    }

    @FunctionalInterface
    private interface EmailOperation {
        void execute() throws MessagingException;
    }
}