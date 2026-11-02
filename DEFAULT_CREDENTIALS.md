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

## 5. Jinsi Seeding na Usalama wa Mazingira (Hosting Security) Inavyofanya Kazi

### A. Udhibiti wa Data Seeding kupitia Environment Variables
Kwa usalama wa mfumo unapouhost (Production / Hosting), `DataInitializer.java` imelindwa na sasa inatii variable zifuatazo:

| Variable | Chaguo-msingi (Default) | Maelezo na Matumizi |
| :--- | :--- | :--- |
| `APP_SEED_ENABLED` | `false` | **Swichi Kuu**: Ikiwa `false`, mfumo **HAUTATENGENEZA** data yoyote wala akaunti chaguomsingi wakati server inawaka. Huu ndio msingi salama wa production. |
| `APP_SEED_SAMPLE_DATA` | `false` | Ikiwa `true`, inaseed makampuni ya mfano (Serengeti, Kilimanjaro, Zanzibar), wateja bandia, na safari. Kwenye production, iache `false`. |
| `APP_SEED_SUPER_ADMIN_EMAIL` | `superadmin@platform.com` | Barua pepe utakayotumia kuanzisha Platform Super Admin wa kwanza. |
| `APP_SEED_SUPER_ADMIN_PASSWORD` | *(tupu)* | **Password maalum salama** ya Super Admin. Usipotaja hii, mfumo utakumbusha na kutumia default. |
| `APP_SEED_DEFAULT_PASSWORD` | `Password123!` | Password ya akaunti za mfano za majaribio (kwa ajili ya developer mazingira ya ndani tu). |

> [!WARNING]
> **Tahadhari Muhimu ya Hosting / Production:**
> Unapohost mfumo kwenye server ya uzalishaji (Production):
> 1. Weka `APP_SEED_ENABLED=true`, `APP_SEED_SAMPLE_DATA=false`, na weka `APP_SEED_SUPER_ADMIN_PASSWORD=PasswordYakoImara!` wakati wa kuanzisha mfumo kwa mara ya kwanza.
> 2. Mara tu Super Admin akishatengenezwa na server ikawaka, badilisha mara moja kuwa `APP_SEED_ENABLED=false`. Hii inazuia mtu yeyote asijue au kutumia akaunti za majaribio!

### B. Mazingira ya Ndani ya Developer (Local Development)
Kwenye faili lako la `saas/.env` au mizizi ya mradi (`.env`):
```env
APP_SEED_ENABLED=true
APP_SEED_SAMPLE_DATA=true
APP_SEED_DEFAULT_PASSWORD=Password123!
```
Hii inamwezesha developer kupata mara moja akaunti na data zote za majaribio zilizotajwa kwenye jedwali hapo juu.

### C. Manual SQL Seeding (`seed_default_credentials.sql`)
Ikiwa unataka ku-seed database moja kwa moja kupitia terminal au SQL client (DBeaver/pgAdmin/psql):

```bash
psql -U postgres -h localhost -d saas_db -f saas/src/main/resources/db/seed_default_credentials.sql
```

---

## 6. Docker & Docker Compose Setup

Mradi sasa una `Dockerfile` kamili za multi-stage pamoja na `docker-compose.yml`:
- **Postgres (pgvector/pgvector:pg16)**: Database yenye uwezo wa AI na pgvector.
- **Backend (Spring Boot + Java 25)**: Inaendesha API kwenye port `8080` ikiwa na user salama asiye na haki za root (`springboot`).
- **Frontend (Vite + React + Nginx)**: Inahudumia React SPA kwenye port `5173` na ina Nginx proxy.

Kuwasha mfumo mzima kupitia Docker:
```bash
# 1. Nakili .env.example kwenda .env
cp .env.example .env

# 2. Washa vyombo vyote
docker compose up -d --build
```

---

## 7. Mfumo wa Kuhifadhi Mafaili (File Storage: Local vs Cloudflare R2)

Mradi una huduma ya kisasa ya kuhifadhi mafaili (`FileStorageService`) inayobadilika kiotomatiki kulingana na mazingira:

### A. Local Development (Mazingira ya Ndani)
Kwenye `.env`:
```env
STORAGE_TYPE=local
UPLOAD_DIR=uploads
```
Mafaili yote (k.m. PDF za Invoices, viambatisho vya email) yanahifadhiwa kwenye folder la ndani la `uploads/`. Hakuna haja ya mtandao wala akaunti ya nje.

### B. Production Hosting (Cloudflare R2 Object Storage)
Ili kuzuia mafaili yasipotee wakati containers za Docker zinapozimwa, zikirestart au kuhama server, unatumia **Cloudflare R2** (ambayo inaendana 100% na standard ya S3 bila gharama za egress bandwidth):

Kwenye production `.env`:
```env
STORAGE_TYPE=r2
R2_ACCOUNT_ID=account_id_yako_kutoka_cloudflare
R2_BUCKET_NAME=jina_la_bucket_lako
R2_ACCESS_KEY_ID=access_key_kutoka_cloudflare_r2_token
R2_SECRET_ACCESS_KEY=secret_key_kutoka_cloudflare_r2_token
# (Hiari) URL ya Public CDN/Domain ukiiunganisha kwenye bucket
R2_PUBLIC_URL=https://media.kampuniyako.com
```

> [!TIP]
> **Jinsi ya Kupata Taarifa za Cloudflare R2:**
> 1. Ingia kwenye Dashboard ya Cloudflare: nenda **R2 Object Storage**.
> 2. Bonyeza **Create Bucket** na weka jina (k.m. `saas-production-files`).
> 3. Kwenye ukurasa wa R2, bonyeza **Manage R2 API Tokens** -> **Create API Token**.
> 4. Chagua ruhusa ya **Object Read & Write**, kisha nakili **Access Key ID**, **Secret Access Key**, na **Account ID**.

---

## 8. Jinsi ya Kujaribu Kuingia (Frontend & API)

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
