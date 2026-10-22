# Deployment Guide — RESIK VPS

## Setup Sekali Saja (Di VPS)

SSH ke VPS Anda, lalu jalankan:

```bash
# 1. Clone repo
git clone https://github.com/unkronnn/resik.git /opt/resik
cd /opt/resik

# 2. Beri permission
chmod +x deploy.sh

# 3. Jalankan deploy (otomatis install Docker, build, dan start)
./deploy.sh
```

Selesai! Backend API akan jalan di `http://43.157.243.87:8080/api`

## Auto-Deploy dari GitHub (Setup Secrets)

Agar setiap push ke `main` auto-deploy ke VPS:

1. Generate SSH key di VPS:
```bash
ssh-keygen -t ed25519 -f ~/.ssh/resik_deploy -N ""
cat ~/.ssh/resik_deploy.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/resik_deploy   # Copy private key
```

2. Add secrets di GitHub repo `unkronnn/resik`:
   - Settings > Secrets and variables > Actions > New repository secret
   - `VPS_HOST` = `43.157.243.87`
   - `VPS_USER` = `ubuntu`
   - `VPS_SSH_KEY` = (paste private key dari step 1)

3. Setiap push ke `main` → GitHub Actions auto-deploy ke VPS

## Manual Update di VPS

```bash
cd /opt/resik
git pull origin main
docker compose down
docker compose build --no-cache
docker compose up -d
```

## Cek Status

```bash
# Lihat logs
docker compose logs -f api

# Cek API
curl http://localhost:8080/api/sampah

# Restart
docker compose restart api
```

## Database

PostgreSQL jalan di container `resik_db`:
- Database: `resik`
- User: `resik`
- Password: `resik_secret_2026`
- Port: `5432`
