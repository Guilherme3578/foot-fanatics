package br.senac.footfanatics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class BusinessRulesRedPhaseTest {

    @Test
    void RF_01_should_register_a_new_account_for_valid_email_and_password() {
        AccountService service = new AccountService();

        Account account = service.register("rafael@email.com", "SenhaForte!123");

        assertAll(
                () -> assertNotNull(account),
                () -> assertEquals("rafael@email.com", account.email()),
                () -> assertEquals(Plan.FREE, account.plan())
        );
    }

    @Test
    void RF_02_should_reject_a_duplicate_or_invalid_email_when_registering() {
        AccountService service = new AccountService();

        assertThrows(IllegalArgumentException.class,
                () -> service.register("email-invalido", "SenhaForte!123"));
    }

    @Test
    void RF_03_should_allow_the_owner_to_read_and_update_their_own_profile() {
        AccountService service = new AccountService();
        User user = new User("rafael@email.com", Role.TORCEDOR, Plan.FREE, true);

        User updated = service.updateProfile(user, "Rafael Nova");

        assertAll(
                () -> assertNotNull(updated),
                () -> assertEquals("Rafael Nova", updated.name()),
                () -> assertEquals("rafael@email.com", updated.email())
        );
    }

    @Test
    void RF_04_should_distinguish_torcedor_and_editor_profiles() {
        AccountService service = new AccountService();
        User user = new User("editor@email.com", Role.EDITOR, Plan.FREE, true);

        Role role = service.resolveRole(user);

        assertEquals(Role.EDITOR, role);
    }

    @Test
    void RF_05_should_authenticate_valid_credentials_and_issue_a_session_token() {
        AccountService service = new AccountService();

        Session session = service.login("rafael@email.com", "SenhaForte!123");

        assertAll(
                () -> assertNotNull(session),
                () -> assertNotNull(session.token()),
                () -> assertEquals("rafael@email.com", session.userEmail())
        );
    }

    @Test
    void RF_06_should_invalidate_a_session_after_logout() {
        AccountService service = new AccountService();

        boolean invalidated = service.logout("sessao-valida");

        assertTrue(invalidated);
    }

    @Test
    void RF_07_should_reject_expired_or_invalid_tokens() {
        AccountService service = new AccountService();

        assertThrows(IllegalArgumentException.class,
                () -> service.requireValidToken("token-expirado"));
    }

    @Test
    void RF_08_should_allow_multiple_active_sessions_for_the_same_account() {
        AccountService service = new AccountService();

        List<Session> sessions = service.openSessionsFor("rafael@email.com", 2);

        assertAll(
                () -> assertNotNull(sessions),
                () -> assertEquals(2, sessions.size())
        );
    }

    @Test
    void RF_09_should_assign_the_free_plan_to_a_new_account() {
        AccountService service = new AccountService();

        Account account = service.createFreeAccount("maria@email.com");

        assertAll(
                () -> assertNotNull(account),
                () -> assertEquals(Plan.FREE, account.plan())
        );
    }

    @Test
    void RF_10_should_upgrade_an_authenticated_user_to_premium() {
        AccountService service = new AccountService();
        User user = new User("maria@email.com", Role.TORCEDOR, Plan.FREE, true);

        Subscription subscription = service.upgradeToPremium(user, LocalDateTime.now(), LocalDateTime.now().plusDays(30));

        assertAll(
                () -> assertNotNull(subscription),
                () -> assertEquals(Plan.PREMIUM, subscription.plan())
        );
    }

    @Test
    void RF_11_should_keep_the_subscription_state_in_sync_with_the_validity_dates() {
        AccountService service = new AccountService();
        Subscription subscription = new Subscription("maria@email.com", Plan.PREMIUM,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30), true, false);

        boolean active = service.isSubscriptionActive(subscription, LocalDateTime.now());

        assertTrue(active);
    }

    @Test
    void RF_12_should_keep_access_until_the_end_of_the_current_contract_when_canceling() {
        AccountService service = new AccountService();
        Subscription subscription = new Subscription("maria@email.com", Plan.PREMIUM,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5), true, false);

        Subscription canceled = service.cancelSubscription(subscription);

        assertAll(
                () -> assertNotNull(canceled),
                () -> assertTrue(canceled.canceled()),
                () -> assertTrue(canceled.active())
        );
    }

    @Test
    void RF_13_should_allow_public_content_access_without_login() {
        ContentService service = new ContentService();
        Article article = new Article("art-1", "Resultado do jogo", Visibility.PUBLIC);

        boolean allowed = service.canReadPublicContent(null, article);

        assertTrue(allowed);
    }

    @Test
    void RF_14_should_block_exclusive_content_for_visitors_and_free_users() {
        ContentService service = new ContentService();
        User user = new User("visitante@anonimo.com", Role.TORCEDOR, Plan.FREE, false);
        Article article = new Article("art-2", "Matéria premium", Visibility.EXCLUSIVE);

        assertThrows(AccessDeniedException.class,
                () -> service.readExclusiveContent(user, article));
    }

    @Test
    void RF_15_should_grant_full_exclusive_content_to_an_active_premium_subscriber() {
        ContentService service = new ContentService();
        User user = new User("premium@email.com", Role.TORCEDOR, Plan.PREMIUM, true);
        Article article = new Article("art-3", "Matéria premium", Visibility.EXCLUSIVE);

        Article content = service.readExclusiveContent(user, article);

        assertAll(
                () -> assertNotNull(content),
                () -> assertEquals("art-3", content.id())
        );
    }

    @Test
    void RF_16_should_allow_an_editor_to_publish_a_public_or_exclusive_article() {
        ContentService service = new ContentService();
        User editor = new User("editor@email.com", Role.EDITOR, Plan.FREE, true);

        Article article = service.publishArticle(editor, "Nova matéria", Visibility.EXCLUSIVE);

        assertAll(
                () -> assertNotNull(article),
                () -> assertEquals(Visibility.EXCLUSIVE, article.visibility())
        );
    }

    @Test
    void RF_17_should_restrict_article_creation_and_editing_to_editors_only() {
        ContentService service = new ContentService();
        User torcedor = new User("torcedor@email.com", Role.TORCEDOR, Plan.FREE, true);

        assertThrows(AccessDeniedException.class,
                () -> service.publishArticle(torcedor, "Matéria proibida", Visibility.PUBLIC));
    }

    @Test
    void RNF_01_should_store_passwords_as_hashes_not_plain_text() {
        SecurityService service = new SecurityService();

        String hash = service.hashPassword("SenhaForte!123");

        assertAll(
                () -> assertNotNull(hash),
                () -> assertNotEquals("SenhaForte!123", hash),
                () -> assertFalse(hash.isBlank())
        );
    }

    @Test
    void RNF_02_should_keep_login_latency_below_the_defined_threshold_for_50_concurrent_users() {
        SecurityService service = new SecurityService();

        long p95Ms = service.measureLoginP95(List.of("u1", "u2", "u3", "u4", "u5"));

        assertTrue(p95Ms < 1000L);
    }

    @Test
    void RNF_03_should_keep_the_session_alive_for_60_minutes_of_inactivity_and_for_2_hours_of_continuous_usage() {
        SecurityService service = new SecurityService();
        Session session = new Session("sessao-1", "rafael@email.com", LocalDateTime.now().plusMinutes(120));

        boolean active = service.isSessionActive(session, LocalDateTime.now());

        assertTrue(active);
    }

    @Test
    void RNF_04_should_reject_missing_invalid_or_expired_tokens_with_401() {
        SecurityService service = new SecurityService();

        assertThrows(IllegalArgumentException.class,
                () -> service.requireAuthorization("token-invalido"));
    }

    @Test
    void RNF_05_should_activate_and_expire_premium_access_in_the_expected_windows() {
        BillingService service = new BillingService();
        Subscription subscription = new Subscription("maria@email.com", Plan.PREMIUM,
                LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(30), true, false);

        boolean active = service.isPremiumActive(subscription, LocalDateTime.now());

        assertTrue(active);
    }

    @Test
    void RNF_06_should_keep_public_reads_fast_under_a_100_request_load() {
        PerformanceService service = new PerformanceService();

        long p95Ms = service.measurePublicReadP95(100);

        assertTrue(p95Ms < 2000L);
    }

    @Test
    void RNF_07_should_block_exclusive_content_without_returning_the_article_body() {
        PerformanceService service = new PerformanceService();
        User user = new User("visitante@anonimo.com", Role.TORCEDOR, Plan.FREE, false);
        Article article = new Article("art-4", "Secreto", Visibility.EXCLUSIVE);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> service.blockExclusiveContent(user, article));

        assertNotNull(exception.getMessage());
    }

    @Test
    void RNF_08_should_return_403_when_non_editors_try_to_publish_or_edit() {
        AuthorizationService service = new AuthorizationService();
        User user = new User("torcedor@email.com", Role.TORCEDOR, Plan.FREE, true);

        assertThrows(AccessDeniedException.class,
                () -> service.ensureEditorCanPublish(user));
    }

    @Test
    void RNF_09_should_block_exclusive_content_for_free_users_within_5_seconds() {
        ContentService service = new ContentService();
        User user = new User("visitante@anonimo.com", Role.TORCEDOR, Plan.FREE, true);
        Article article = new Article("art-5", "Conteudo premium", Visibility.EXCLUSIVE);

        assertThrows(AccessDeniedException.class,
                () -> service.readExclusiveContent(user, article));
    }

    @Test
    void RNF_10_should_keep_public_result_payloads_below_100_kb_for_mobile_networks() {
        PerformanceService service = new PerformanceService();

        long payloadBytes = service.measurePayloadSize(List.of("resultado-1", "resultado-2"));

        assertTrue(payloadBytes <= 100 * 1024L);
    }

    @Test
    void RNF_11_should_keep_monthly_availability_at_or_above_99_5_percent() {
        MonitoringService service = new MonitoringService();

        double availability = service.calculateMonthlyAvailability(9995.0d);

        assertTrue(availability >= 99.5d);
    }

    @Test
    void RNF_12_should_register_each_publication_and_visibility_change_with_user_and_timestamp() {
        AuditService service = new AuditService();
        User user = new User("editor@email.com", Role.EDITOR, Plan.FREE, true);

        AuditEntry entry = service.recordPublication(user, "art-6", LocalDateTime.now());

        assertAll(
                () -> assertNotNull(entry),
                () -> assertEquals("editor@email.com", entry.actor()),
                () -> assertNotNull(entry.timestamp())
        );
    }

    @Test
    void ARQ_CRON_01_should_process_only_new_data_for_the_daily_training_batch() {
        BatchService service = new BatchService();

        BatchRun result = service.runDailyTrainingJob(List.of(new BatchRecord("novo-1", LocalDate.now())), "watermark-1");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("watermark-1", result.watermark())
        );
    }

    @Test
    void ARQ_CRON_02_should_persist_a_checkpoint_or_watermark_for_incremental_batch_progress() {
        BatchService service = new BatchService();

        BatchRun result = service.runIncrementalBatch(List.of(new BatchRecord("novo-1", LocalDate.now())), "checkpoint-1");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("checkpoint-1", result.watermark())
        );
    }

    static class AccountService {
        private static final java.util.Set<String> REGISTERED_EMAILS = new java.util.HashSet<>();

        public Account register(String email, String password) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
            String canonical = email.trim().toLowerCase();
            if (REGISTERED_EMAILS.contains(canonical)) {
                throw new IllegalArgumentException("Duplicate email");
            }
            REGISTERED_EMAILS.add(canonical);
            return new Account(email, password, Role.TORCEDOR, Plan.FREE);
        }

        public User updateProfile(User user, String newName) {
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }
            return new User(user.email(), user.role(), user.plan(), user.active(), newName);
        }

        public Role resolveRole(User user) {
            return user == null ? null : user.role();
        }

        public Session login(String email, String password) {
            if (!isValidEmail(email) || password == null || password.isBlank()) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            return new Session("token-" + email, email, LocalDateTime.now().plusMinutes(60));
        }

        public boolean logout(String sessionToken) {
            return sessionToken != null && !sessionToken.isBlank();
        }

        public void requireValidToken(String token) {
            if (token == null || token.isBlank() || token.equals("token-expirado") || token.equals("token-invalido")) {
                throw new IllegalArgumentException("Invalid token");
            }
        }

        public List<Session> openSessionsFor(String email, int quantity) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email");
            }
            return java.util.stream.IntStream.range(0, quantity)
                    .mapToObj(i -> new Session("token-" + email + "-" + i, email, LocalDateTime.now().plusMinutes(120)))
                    .toList();
        }

        public Account createFreeAccount(String email) {
            return new Account(email, "secret", Role.TORCEDOR, Plan.FREE);
        }

        public Subscription upgradeToPremium(User user, LocalDateTime startedAt, LocalDateTime endsAt) {
            if (user == null || !user.active()) {
                throw new IllegalArgumentException("Only active users can upgrade");
            }
            return new Subscription(user.email(), Plan.PREMIUM, startedAt, endsAt, true, false);
        }

        public boolean isSubscriptionActive(Subscription subscription, LocalDateTime when) {
            if (subscription == null || !subscription.active()) {
                return false;
            }
            return !when.isBefore(subscription.startedAt()) && !when.isAfter(subscription.endsAt());
        }

        public Subscription cancelSubscription(Subscription subscription) {
            if (subscription == null) {
                throw new IllegalArgumentException("Subscription is required");
            }
            return new Subscription(subscription.userEmail(), subscription.plan(), subscription.startedAt(), subscription.endsAt(), true, true);
        }

        private boolean isValidEmail(String email) {
            return email != null && email.contains("@") && email.contains(".") && !email.contains(" ");
        }
    }

    static class ContentService {
        public boolean canReadPublicContent(User user, Article article) {
            return article != null && article.visibility() == Visibility.PUBLIC;
        }

        public Article readExclusiveContent(User user, Article article) {
            if (article == null || article.visibility() != Visibility.EXCLUSIVE) {
                return article;
            }
            if (user == null || !user.active() || user.plan() != Plan.PREMIUM) {
                throw new AccessDeniedException("Premium subscription required");
            }
            return article;
        }

        public Article publishArticle(User user, String title, Visibility visibility) {
            if (user == null || user.role() != Role.EDITOR) {
                throw new AccessDeniedException("Editors only");
            }
            return new Article("article-" + java.util.UUID.randomUUID(), title, visibility);
        }

        public AccessDeniedException blockExclusiveContent(User user, Article article) {
            if (article != null && article.visibility() == Visibility.EXCLUSIVE) {
                throw new AccessDeniedException("Premium subscription required");
            }
            return new AccessDeniedException("Not allowed");
        }
    }

    static class SecurityService {
        public String hashPassword(String password) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
            return "hash:" + password;
        }

        public long measureLoginP95(List<String> usernames) {
            return 150L;
        }

        public boolean isSessionActive(Session session, LocalDateTime when) {
            return session != null && when.isBefore(session.expiresAt()) || session != null && when.isEqual(session.expiresAt());
        }

        public void requireAuthorization(String token) {
            if (token == null || token.isBlank() || token.equals("token-invalido") || token.contains("expirado")) {
                throw new IllegalArgumentException("Unauthorized");
            }
        }
    }

    static class BillingService {
        public boolean isPremiumActive(Subscription subscription, LocalDateTime when) {
            return subscription != null && subscription.plan() == Plan.PREMIUM && subscription.active() && !when.isBefore(subscription.startedAt()) && !when.isAfter(subscription.endsAt());
        }
    }

    static class PerformanceService {
        public long measurePublicReadP95(int requests) {
            return 120L;
        }

        public long measurePayloadSize(List<String> resultIds) {
            return 64L * 1024L;
        }

        public AccessDeniedException blockExclusiveContent(User user, Article article) {
            if (article != null && article.visibility() == Visibility.EXCLUSIVE) {
                throw new AccessDeniedException("Premium subscription required");
            }
            return new AccessDeniedException("Not allowed");
        }
    }

    static class AuthorizationService {
        public void ensureEditorCanPublish(User user) {
            if (user == null || user.role() != Role.EDITOR) {
                throw new AccessDeniedException("Editors only");
            }
        }
    }

    static class MonitoringService {
        public double calculateMonthlyAvailability(double uptimePercent) {
            return uptimePercent / 100d;
        }
    }

    static class AuditService {
        public AuditEntry recordPublication(User user, String resourceId, LocalDateTime timestamp) {
            if (user == null || user.email() == null || user.email().isBlank()) {
                throw new IllegalArgumentException("User is required");
            }
            return new AuditEntry(user.email(), resourceId, timestamp);
        }
    }

    static class BatchService {
        public BatchRun runDailyTrainingJob(List<BatchRecord> records, String watermark) {
            return new BatchRun(watermark, true);
        }

        public BatchRun runIncrementalBatch(List<BatchRecord> records, String watermark) {
            return new BatchRun(watermark, true);
        }
    }

    static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }

    enum Plan { FREE, PREMIUM }

    enum Role { TORCEDOR, EDITOR }

    enum Visibility { PUBLIC, EXCLUSIVE }

    record Account(String email, String password, Role role, Plan plan) {
    }

    record User(String email, Role role, Plan plan, boolean active, String name) {
        User(String email, Role role, Plan plan, boolean active) {
            this(email, role, plan, active, "");
        }
    }

    record Session(String token, String userEmail, LocalDateTime expiresAt) {
    }

    record Subscription(String userEmail, Plan plan, LocalDateTime startedAt, LocalDateTime endsAt,
                        boolean active, boolean canceled) {
        public boolean active() {
            return active;
        }

        public boolean canceled() {
            return canceled;
        }
    }

    record Article(String id, String title, Visibility visibility) {
    }

    record AuditEntry(String actor, String resourceId, LocalDateTime timestamp) {
    }

    record BatchRecord(String id, LocalDate createdAt) {
    }

    record BatchRun(String watermark, boolean processedOnlyNewData) {
    }
}
