package com.saas.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saas.backend.models.*;
import com.saas.backend.repositories.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DataInitializer
 * Automatically seeds baseline platform and company entities when the application starts.
 * Ensures any developer who clones the project can immediately authenticate and test all role workflows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;
    private final PropertyCategoryRepository propertyCategoryRepository;
    private final PriceTierRepository priceTierRepository;
    private final PropertyRepository propertyRepository;
    private final ClientRepository clientRepository;
    private final SafariRepository safariRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String DEFAULT_PASSWORD = "Password123!";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding default system data...");

        // 1. Roles
        Role superAdminRole = getOrCreateRole("SUPER_ADMIN", "super admin", "Platform Super Administrator");
        Role adminRole = getOrCreateRole("ADMIN", "admin", "Company Administrator");
        Role rmRole = getOrCreateRole("RESERVATION_MANAGER", "reservation manager", "Reservation Manager");
        Role saleRole = getOrCreateRole("SALES_PERSON", "sales person", "Sales Person / Travel Consultant");
        Role guideRole = getOrCreateRole("GUIDE", "guide", "Safari Tour Guide");

        // 2. Subscription Plans
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode starterFeatures = mapper.createObjectNode();
        starterFeatures.put("max_users", 3);
        starterFeatures.put("safaris_monthly", 15);
        starterFeatures.put("storage", "5GB");
        starterFeatures.put("ai_assistant", false);
        starterFeatures.put("priority_support", false);
        SubscriptionPlan starterPlan = getOrCreatePlan("Starter Plan", new BigDecimal("49.00"), 3, starterFeatures);

        ObjectNode proFeatures = mapper.createObjectNode();
        proFeatures.put("max_users", 15);
        proFeatures.put("safaris_monthly", 60);
        proFeatures.put("storage", "50GB");
        proFeatures.put("ai_assistant", true);
        proFeatures.put("priority_support", true);
        SubscriptionPlan proPlan = getOrCreatePlan("Professional Plan", new BigDecimal("149.00"), 15, proFeatures);

        ObjectNode entFeatures = mapper.createObjectNode();
        entFeatures.put("max_users", 100);
        entFeatures.put("safaris_monthly", "unlimited");
        entFeatures.put("storage", "500GB");
        entFeatures.put("ai_assistant", true);
        entFeatures.put("priority_support", true);
        entFeatures.put("custom_branding", true);
        SubscriptionPlan entPlan = getOrCreatePlan("Enterprise Plan", new BigDecimal("399.00"), 100, entFeatures);

        // 3. Default Companies (with 3 different subscription tiers)
        Company serengeti = getOrCreateCompany(
                "Serengeti Expeditions Ltd",
                "serengeti-expeditions",
                "contact@serengetiexpeditions.com",
                "+255 754 100 200",
                "Tanzania",
                "Africa/Dar_es_Salaam",
                entPlan
        );

        Company kilimanjaro = getOrCreateCompany(
                "Kilimanjaro Trekking & Safaris",
                "kilimanjaro-safaris",
                "info@kilimanjarosafaris.com",
                "+255 754 300 400",
                "Tanzania",
                "Africa/Dar_es_Salaam",
                proPlan
        );

        Company zanzibar = getOrCreateCompany(
                "Zanzibar Blue Travel",
                "zanzibar-travel",
                "hello@zanzibartravel.com",
                "+255 754 500 600",
                "Tanzania",
                "Africa/Dar_es_Salaam",
                starterPlan
        );

        // 4. Platform Super Admin
        seedPlatformAdmin(
                "superadmin@platform.com",
                DEFAULT_PASSWORD,
                "Platform",
                "SuperAdmin",
                "+255 700 000 001",
                PlatformRole.SUPER_ADMIN
        );

        // 5. Company Users for all Roles
        // -- Company 1: Serengeti Expeditions (Enterprise)
        seedUser("admin@serengeti.com", DEFAULT_PASSWORD, "Amara", "Kiprotich", Gender.FEMALE, "+255 754 111 001", adminRole, serengeti);
        seedUser("rm@serengeti.com", DEFAULT_PASSWORD, "David", "Mollel", Gender.MALE, "+255 754 111 002", rmRole, serengeti);
        User salesUser = seedUser("sales@serengeti.com", DEFAULT_PASSWORD, "Neema", "Mwangi", Gender.FEMALE, "+255 754 111 003", saleRole, serengeti);
        seedUser("guide@serengeti.com", DEFAULT_PASSWORD, "Juma", "Baraka", Gender.MALE, "+255 754 111 004", guideRole, serengeti);
        seedUser("superadmin@serengeti.com", DEFAULT_PASSWORD, "Kavit", "SuperAdmin", Gender.MALE, "+255 754 111 000", superAdminRole, serengeti);

        // -- Company 2: Kilimanjaro Trekking (Professional)
        seedUser("admin@kilimanjaro.com", DEFAULT_PASSWORD, "Kelvin", "Tarimo", Gender.MALE, "+255 754 333 001", adminRole, kilimanjaro);
        seedUser("sales@kilimanjaro.com", DEFAULT_PASSWORD, "Amina", "Mushi", Gender.FEMALE, "+255 754 333 002", saleRole, kilimanjaro);

        // -- Company 3: Zanzibar Blue Travel (Starter)
        seedUser("admin@zanzibar.com", DEFAULT_PASSWORD, "Fatma", "Said", Gender.FEMALE, "+255 754 555 001", adminRole, zanzibar);

        // 6. Sample Operational Telemetry & Seed Entities
        seedOperationalData(serengeti, salesUser);

        log.info("Default system data seeded successfully.");
    }

    private Role getOrCreateRole(String canonicalName, String alternateName, String description) {
        return roleRepository.findByNameIgnoreCase(canonicalName)
                .or(() -> roleRepository.findByNameIgnoreCase(alternateName))
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(canonicalName)
                            .description(description)
                            .build();
                    log.info("Seeding role: {}", canonicalName);
                    return roleRepository.save(role);
                });
    }

    private SubscriptionPlan getOrCreatePlan(String name, BigDecimal price, int maxUsers, ObjectNode features) {
        return subscriptionPlanRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    SubscriptionPlan plan = SubscriptionPlan.builder()
                            .name(name)
                            .price(price)
                            .currency("USD")
                            .maxUsers(maxUsers)
                            .features(features)
                            .status(SubscriptionStatus.ACTIVE)
                            .build();
                    log.info("Seeding subscription plan: {}", name);
                    return subscriptionPlanRepository.save(plan);
                });
    }

    private Company getOrCreateCompany(String name, String slug, String email, String phone, String country, String timezone, SubscriptionPlan plan) {
        return companyRepository.findBySlug(slug)
                .or(() -> companyRepository.findByNameIgnoreCase(name))
                .orElseGet(() -> {
                    Company company = Company.builder()
                            .name(name)
                            .slug(slug)
                            .email(email)
                            .phone(phone)
                            .country(country)
                            .timezone(timezone)
                            .subscriptionPlan(plan)
                            .status(SubscriptionStatus.ACTIVE)
                            .build();
                    log.info("Seeding company: {} [{}]", name, slug);
                    return companyRepository.save(company);
                });
    }

    private void seedPlatformAdmin(String email, String rawPassword, String firstName, String lastName, String phone, PlatformRole role) {
        if (platformAdminRepository.findByEmail(email).isEmpty()) {
            PlatformAdmin admin = PlatformAdmin.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(phone)
                    .platformRole(role)
                    .status(PlatformAdminStatus.ACTIVE)
                    .build();
            log.info("Seeding Platform Admin: {}", email);
            platformAdminRepository.save(admin);
        }
    }

    private User seedUser(String email, String rawPassword, String firstName, String lastName, Gender gender, String phone, Role role, Company company) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(rawPassword))
                            .firstName(firstName)
                            .lastName(lastName)
                            .gender(gender)
                            .phone(phone)
                            .role(role)
                            .company(company)
                            .status(UserStatus.ACTIVE)
                            .build();
                    log.info("Seeding user: {} (Role: {}, Company: {})", email, role.getName(), company.getName());
                    return userRepository.save(user);
                });
    }

    private void seedOperationalData(Company company, User salesPerson) {
        try {
            // Property Categories
            PropertyCategory lodgeCategory = propertyCategoryRepository.findByNameIgnoreCase("Safari Lodge")
                    .orElseGet(() -> propertyCategoryRepository.save(PropertyCategory.builder()
                            .name("Safari Lodge")
                            .description("Permanent luxury safari lodges in national parks")
                            .build()));

            PropertyCategory tentCategory = propertyCategoryRepository.findByNameIgnoreCase("Tented Camp")
                    .orElseGet(() -> propertyCategoryRepository.save(PropertyCategory.builder()
                            .name("Tented Camp")
                            .description("Luxury safari tented camps under canvas")
                            .build()));

            // Price Tiers
            PriceTier luxuryTier = priceTierRepository.findByNameIgnoreCase("Luxury")
                    .orElseGet(() -> priceTierRepository.save(PriceTier.builder()
                            .name("Luxury")
                            .minPrice(new BigDecimal("500.00"))
                            .maxPrice(new BigDecimal("1500.00"))
                            .currency("USD")
                            .build()));

            // Properties
            if (!propertyRepository.existsByName("Serengeti Serena Safari Lodge")) {
                Property prop1 = Property.builder()
                        .name("Serengeti Serena Safari Lodge")
                        .slug("serengeti-serena-safari-lodge")
                        .location("Central Serengeti")
                        .region("Serengeti National Park")
                        .country("Tanzania")
                        .category(lodgeCategory)
                        .priceTier(luxuryTier)
                        .description("Iconic lodge overlooking the vast Serengeti plains.")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build();
                propertyRepository.save(prop1);
            }

            if (!propertyRepository.existsByName("Ngorongoro Crater Lodge")) {
                Property prop2 = Property.builder()
                        .name("Ngorongoro Crater Lodge")
                        .slug("ngorongoro-crater-lodge")
                        .location("Crater Rim")
                        .region("Ngorongoro Conservation Area")
                        .country("Tanzania")
                        .category(tentCategory)
                        .priceTier(luxuryTier)
                        .description("Architectural masterpiece perched on the edge of the Ngorongoro Crater.")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build();
                propertyRepository.save(prop2);
            }

            // Clients
            Client client1 = null;
            if (!clientRepository.existsByCompanyIdAndEmail(company.getId(), "johnathan.davis@example.com")) {
                client1 = Client.builder()
                        .company(company)
                        .salesPerson(salesPerson)
                        .firstName("Johnathan")
                        .lastName("Davis")
                        .gender(Gender.MALE)
                        .email("johnathan.davis@example.com")
                        .phone("+1 555 234 5678")
                        .nationality("American")
                        .preferredLanguage("English")
                        .countryOfResidence("United States")
                        .notes("Interested in 7-day Great Migration safari package.")
                        .status(ClientStatus.ACTIVE)
                        .build();
                client1 = clientRepository.save(client1);
            }

            if (!clientRepository.existsByCompanyIdAndEmail(company.getId(), "sarah.jenkins@example.com")) {
                Client client2 = Client.builder()
                        .company(company)
                        .salesPerson(salesPerson)
                        .firstName("Sarah")
                        .lastName("Jenkins")
                        .gender(Gender.FEMALE)
                        .email("sarah.jenkins@example.com")
                        .phone("+44 20 7946 0912")
                        .nationality("British")
                        .preferredLanguage("English")
                        .countryOfResidence("United Kingdom")
                        .notes("Family wildlife safari with hot air balloon experience.")
                        .status(ClientStatus.ACTIVE)
                        .build();
                clientRepository.save(client2);
            }

            // Safaris
            if (client1 != null && !safariRepository.existsByReferenceNumber("SAF-2026-001")) {
                Safari safari1 = Safari.builder()
                        .client(client1)
                        .salesPerson(salesPerson)
                        .referenceNumber("SAF-2026-001")
                        .startDate(LocalDate.now().plusDays(10))
                        .endDate(LocalDate.now().plusDays(17))
                        .numberOfPassengers(2)
                        .status(SafariStatus.CONFIRMED)
                        .notes("7-Day Serengeti & Ngorongoro Migration Safari.")
                        .build();
                safariRepository.save(safari1);
            }
        } catch (Exception e) {
            log.warn("Operational sample data seeding skipped or partially applied: {}", e.getMessage());
        }
    }
}
