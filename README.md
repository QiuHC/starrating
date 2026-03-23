# Starrating Deployment Notes

## Current Topology

- Frontend: `starrating-vue`
- Backend: `starrating-backend`
- Production database target: Supabase Postgres
- Production backend target: Railway

## Local Development

Backend uses the `local` Spring profile for H2 in-memory development data.

Run backend locally:

```bash
cd starrating-backend
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

Run frontend locally:

```bash
cd starrating-vue
cp .env.example .env.local
npm install
npm run dev
```

## Supabase Setup

1. Create a Supabase project.
2. Open the SQL editor.
3. Run [postgres-init.sql](/Users/qiuhengchen/Documents/CodeX/starrating/starrating-backend/db/postgres-init.sql).
4. The script will also seed:
   - super admin `6666666 / 88888888`
   - shop account `BYDGD100W / 88888888`
   - default roles, permissions, and user-role / role-permission mappings

The seed rules now match the RBAC model:

- `6666666` is a factory-side super admin
- `BYDGD100W` is a shop-side account
- shop username equals `shopCode`
- a shop account only operates its own shop data

## Railway Setup

Set these environment variables in Railway for the backend service:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=<db-user>
SPRING_DATASOURCE_PASSWORD=<db-password>
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
APP_JWT_SECRET=<a-long-random-secret>
APP_JWT_EXPIRATION_MS=3600000
```

If you deploy the frontend separately, set:

```bash
VITE_API_BASE_URL=https://<your-railway-domain>/api
```

## Important

- Do not run the backend in production with `SPRING_PROFILES_ACTIVE=local`.
- Production no longer auto-runs the local H2 seed script.
- Local test data still comes from `starrating-backend/src/main/resources/schema.sql`.
