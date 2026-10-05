# Default Credentials & Test Accounts Guide

Huu ni mwongozo wa **default credentials na test accounts** zilizowekwa kwenye mradi ili developer yeyote atakaye-clone aweze kuingia mara moja na kutest mifumo yote (Super Admin, Company Admin, Reservation Manager, Sales Person, Guide) pamoja na Subscription Plans na Makampuni mbalimbali.

---

## 1. Universal Password

> [!IMPORTANT]
> Password ya akaunti zote za majaribio ni: **`Password123!`**

---

## 2. Default Subscription Plans

| Mpango (Plan Name) | Bei (Price) | Max Users | Safaris / Mwezi | Storage | AI Assistant | Priority Support | Custom Branding |
| :--- | :--- | :--- | :--- | :--- | :---: | :---: | :---: |
| **Starter Plan** | $49.00 / mwezi | 3 watumiaji | 15 safaris | 5 GB | ❌ | ❌ | ❌ |
| **Professional Plan** | $149.00 / mwezi | 15 watumiaji | 60 safaris | 50 GB | ✅ | ✅ | ❌ |
| **Enterprise Plan** | $399.00 / mwezi | 100 watumiaji | Unlimited | 500 GB | ✅ | ✅ | ✅ |

---

## 3. Default Companies

Makampuni 3 yameandaliwa yenye subscription tiers tofauti:

| Jina la Kampuni (Company) | Slug | Plan Iliyounganishwa | Barua Pepe (Email) | Simu | Nchi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Serengeti Expeditions Ltd** | `serengeti-expeditions` | **Enterprise Plan** | `contact@serengetiexpeditions.com` | +255 754 100 200 | Tanzania |
| **Kilimanjaro Trekking & Safaris** | `kilimanjaro-safaris` | **Professional Plan** | `info@kilimanjarosafaris.com` | +255 754 300 400 | Tanzania |
| **Zanzibar Blue Travel** | `zanzibar-travel` | **Starter Plan** | `hello@zanzibartravel.com` | +255 754 500 600 | Tanzania |

---

## 4. Default Login Accounts Matrix

Unaweza kutumia akaunti zifuatazo moja kwa moja kwenye skrini ya kuingia (`/login`):

| Role / Nafasi | Barua Pepe (Email) | Password | Kampuni (Company) | Plan | Dashboard & Uwezo |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Platform Super Admin** | `superadmin@platform.com` | `Password123!` | *Platform Level* | Platform Control | **SuperAdminDashboard** (Usimamizi wa tenants, makampuni, mipango yote ya subscription, watumiaji wote na takwimu za jukwaa zima) |
| **Company Super Admin** | `superadmin@serengeti.com` | `Password123!` | Serengeti Expeditions Ltd | Enterprise | **SuperAdminDashboard / Executive** (Usimamizi wa kampuni na vitengo vyote) |
| **Company Admin** | `admin@serengeti.com` | `Password123!` | Serengeti Expeditions Ltd | Enterprise | **AdminDashboard** (Usimamizi wa wafanyakazi, revenue, ripoti za kampuni na resources zote) |
| **Reservation Manager** | `rm@serengeti.com` | `Password123!` | Serengeti Expeditions Ltd | Enterprise | **ReservationDashboard** (Usimamizi wa lodge bookings, room allocations, upatikanaji wa vyumba na mawasiliano ya hoteli) |
| **Sales Person** | `sales@serengeti.com` | `Password123!` | Serengeti Expeditions Ltd | Enterprise | **SalesDashboard** (Usimamizi wa wateja/leads, quotation, itineraries, na safari pipelines) |
| **Safari Tour Guide** | `guide@serengeti.com` | `Password123!` | Serengeti Expeditions Ltd | Enterprise | **GuideDashboard** (Ratiba za safari za kila siku, manifest ya wageni, safari circuits na usalama) |
| **Company Admin (Pro)** | `admin@kilimanjaro.com` | `Password123!` | Kilimanjaro Trekking & Safaris | Professional | **AdminDashboard** (Kujaribu usimamizi wa kampuni yenye Professional plan) |
| **Sales Person (Pro)** | `sales@kilimanjaro.com` | `Password123!` | Kilimanjaro Trekking & Safaris | Professional | **SalesDashboard** (Kutest mauzo chini ya kampuni ya pili) |
| **Company Admin (Starter)** | `admin@zanzibar.com` | `Password123!` | Zanzibar Blue Travel | Starter | **AdminDashboard** (Kujaribu kampuni yenye vikwazo vya Starter plan) |

---

## 5. Jinsi Seeding Inavyofanya Kazi (Automated & Manual)

### A. Automatic Boot Seeding (`DataInitializer.java`)
Mradi una class ya `com.saas.backend.config.DataInitializer` inayotekelezwa kiotomatiki wakati backend ya Spring Boot inapoanza (`CommandLineRunner`). 
- Inakagua kama roles, plans, makampuni au watumiaji tayari wapo.
- Ikiwa database ni mpya, inajaza taarifa zote mara moja bila kuingiliana na data iliyopo (Idempotent).

### B. Manual SQL Seeding (`seed_default_credentials.sql`)
Ikiwa unataka ku-seed database moja kwa moja kupitia terminal au SQL client (DBeaver/pgAdmin/psql):

```bash
psql -U postgres -h localhost -d saas_db -f saas/src/main/resources/db/seed_default_credentials.sql
```

---

## 6. Jinsi ya Kujaribu Kuingia (Frontend & API)

### Kuingia kupitia Frontend:
1. Washa frontend (`cd frontend && npm run dev`).
2. Fungua ukurasa wa Login (`http://localhost:5173/login`).
3. Weka email yoyote hapo juu (mfano: `admin@serengeti.com` au `superadmin@platform.com`).
4. Weka password: `Password123!`.
5. Mfumo utamwelekeza moja kwa moja kwenye Dashboard inayolingana na nafasi (role) yake.

### Kuingia kupitia REST API:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "superadmin@platform.com",
    "password": "Password123!"
  }'
```
