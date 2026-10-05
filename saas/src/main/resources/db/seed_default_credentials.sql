-- ==============================================================================
-- Safari SaaS - Default System Seed Data
-- ==============================================================================
-- This script seeds default subscription plans, companies, roles, platform admin,
-- and team members across all operational roles for local testing and development.
-- Default Password for all seeded accounts: Password123!
-- ==============================================================================

-- 1. Roles
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES 
    (gen_random_uuid(), 'SUPER_ADMIN', 'Platform Super Administrator with global access', NOW(), NOW()),
    (gen_random_uuid(), 'ADMIN', 'Company Administrator managing operational resources and team', NOW(), NOW()),
    (gen_random_uuid(), 'RESERVATION_MANAGER', 'Reservation Manager handling lodge bookings and allocations', NOW(), NOW()),
    (gen_random_uuid(), 'SALES_PERSON', 'Sales Person managing safari inquiries, itineraries and leads', NOW(), NOW()),
    (gen_random_uuid(), 'GUIDE', 'Tour Guide conducting safari expeditions in the field', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET updated_at = NOW();

-- 2. Subscription Plans
INSERT INTO subscription_plans (id, name, price, currency, max_users, features, status, version, created_at, updated_at)
VALUES
    (
        gen_random_uuid(),
        'Starter Plan',
        49.00,
        'USD',
        3,
        '{"max_users": 3, "safaris_monthly": 15, "storage": "5GB", "ai_assistant": false, "priority_support": false}'::jsonb,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid(),
        'Professional Plan',
        149.00,
        'USD',
        15,
        '{"max_users": 15, "safaris_monthly": 60, "storage": "50GB", "ai_assistant": true, "priority_support": true}'::jsonb,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid(),
        'Enterprise Plan',
        399.00,
        'USD',
        100,
        '{"max_users": 100, "safaris_monthly": "unlimited", "storage": "500GB", "ai_assistant": true, "priority_support": true, "custom_branding": true}'::jsonb,
        'ACTIVE',
        0,
        NOW(),
        NOW()
    )
ON CONFLICT (name) DO UPDATE SET updated_at = NOW();

-- 3. Default Companies (3 companies with different subscription tiers)
INSERT INTO companies (id, name, slug, email, phone, country, timezone, subscription_plan_id, status, created_at, updated_at)
VALUES
    (
        gen_random_uuid(),
        'Serengeti Expeditions Ltd',
        'serengeti-expeditions',
        'contact@serengetiexpeditions.com',
        '+255 754 100 200',
        'Tanzania',
        'Africa/Dar_es_Salaam',
        (SELECT id FROM subscription_plans WHERE name = 'Enterprise Plan' LIMIT 1),
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid(),
        'Kilimanjaro Trekking & Safaris',
        'kilimanjaro-safaris',
        'info@kilimanjarosafaris.com',
        '+255 754 300 400',
        'Tanzania',
        'Africa/Dar_es_Salaam',
        (SELECT id FROM subscription_plans WHERE name = 'Professional Plan' LIMIT 1),
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid(),
        'Zanzibar Blue Travel',
        'zanzibar-travel',
        'hello@zanzibartravel.com',
        '+255 754 500 600',
        'Tanzania',
        'Africa/Dar_es_Salaam',
        (SELECT id FROM subscription_plans WHERE name = 'Starter Plan' LIMIT 1),
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (slug) DO UPDATE SET updated_at = NOW();

-- 4. Platform Admin (Platform Super Administrator)
-- BCrypt hash for "Password123!" is $2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK
INSERT INTO platform_admins (id, email, password_hash, first_name, last_name, phone, platform_role, status, created_at, updated_at)
VALUES
    (
        gen_random_uuid(),
        'superadmin@platform.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Platform',
        'SuperAdmin',
        '+255 700 000 001',
        'SUPER_ADMIN',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO UPDATE SET updated_at = NOW();

-- 5. Company Users for all Roles
-- 5a. Serengeti Expeditions Ltd (Enterprise)
INSERT INTO users (id, company_id, role_id, email, password_hash, first_name, last_name, gender, phone, status, created_at, updated_at)
VALUES
    -- Company Admin
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'serengeti-expeditions' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'ADMIN' OR name = 'admin' LIMIT 1),
        'admin@serengeti.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Amara',
        'Kiprotich',
        'FEMALE',
        '+255 754 111 001',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    -- Reservation Manager
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'serengeti-expeditions' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'RESERVATION_MANAGER' OR name = 'reservation manager' LIMIT 1),
        'rm@serengeti.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'David',
        'Mollel',
        'MALE',
        '+255 754 111 002',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    -- Sales Person
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'serengeti-expeditions' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'SALES_PERSON' OR name = 'sales person' LIMIT 1),
        'sales@serengeti.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Neema',
        'Mwangi',
        'FEMALE',
        '+255 754 111 003',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    -- Tour Guide
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'serengeti-expeditions' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'GUIDE' OR name = 'guide' LIMIT 1),
        'guide@serengeti.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Juma',
        'Baraka',
        'MALE',
        '+255 754 111 004',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    -- Super Admin (Company Tenant level)
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'serengeti-expeditions' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'SUPER_ADMIN' OR name = 'super admin' LIMIT 1),
        'superadmin@serengeti.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Kavit',
        'SuperAdmin',
        'MALE',
        '+255 754 111 000',
        'ACTIVE',
        NOW(),
        NOW()
    ),
-- 5b. Kilimanjaro Trekking & Safaris (Professional)
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'kilimanjaro-safaris' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'ADMIN' OR name = 'admin' LIMIT 1),
        'admin@kilimanjaro.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Kelvin',
        'Tarimo',
        'MALE',
        '+255 754 333 001',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'kilimanjaro-safaris' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'SALES_PERSON' OR name = 'sales person' LIMIT 1),
        'sales@kilimanjaro.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Amina',
        'Mushi',
        'FEMALE',
        '+255 754 333 002',
        'ACTIVE',
        NOW(),
        NOW()
    ),
-- 5c. Zanzibar Blue Travel (Starter)
    (
        gen_random_uuid(),
        (SELECT id FROM companies WHERE slug = 'zanzibar-travel' LIMIT 1),
        (SELECT id FROM roles WHERE name = 'ADMIN' OR name = 'admin' LIMIT 1),
        'admin@zanzibar.com',
        '$2a$10$A7yZH1B9ch89.f49JyNTLuzrlDLCTkSVd9aObBy2ujbnTfDFwUHkK',
        'Fatma',
        'Said',
        'FEMALE',
        '+255 754 555 001',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO UPDATE SET updated_at = NOW();
