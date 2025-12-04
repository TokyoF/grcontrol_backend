# 🚀 GRControl Backend - Deployment Guide

## 📋 Configuración de Ambientes

Este backend usa **variables de entorno** para manejar diferentes configuraciones según el ambiente (desarrollo, staging, producción).

---

## 🛠️ Desarrollo Local

### Opción 1: Sin archivo .env (valores por defecto)
```bash
./mvnw spring-boot:run
```
Usará los valores por defecto definidos en `application.properties`

### Opción 2: Con archivo .env
1. Copiar el archivo de ejemplo:
   ```bash
   cp .env.development .env
   ```

2. Editar `.env` si necesitas cambiar algo (puerto, IPs de red local, etc.)

3. Ejecutar con variables de entorno (Windows):
   ```bash
   # PowerShell
   Get-Content .env | ForEach-Object {
     if ($_ -match '^([^=]+)=(.*)$') {
       [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
     }
   }
   ./mvnw spring-boot:run
   ```

   O usando un script helper (ver más abajo)

---

## 🌐 Producción

### 1. Configurar Variables de Entorno

**IMPORTANTE**: En producción, configura estas variables en tu servidor/plataforma de despliegue:

```bash
# Database (usar tu base de datos de producción)
SPRING_DATASOURCE_URL=jdbc:postgresql://tu-db-host:5432/grcontrol_db
SPRING_DATASOURCE_USERNAME=usuario_prod
SPRING_DATASOURCE_PASSWORD=password_seguro_prod

# JWT (GENERAR UN SECRET NUEVO Y SEGURO)
JWT_SECRET=un_secret_muy_largo_y_seguro_minimo_256_bits
JWT_EXPIRATION=86400000

# CORS (SOLO tus dominios de producción)
CORS_ALLOWED_ORIGINS=https://tudominio.com,https://www.tudominio.com

# Server
SERVER_PORT=8080

# Hibernate (IMPORTANTE: NO usar create-drop en producción)
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_FORMAT_SQL=false

# Logging
LOG_LEVEL_WEB=INFO
LOG_LEVEL_SECURITY=INFO
```

### 2. Generar JWT Secret Seguro

```bash
# Linux/Mac
openssl rand -base64 64

# O en Java
java -c "System.out.println(java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString())"

# PowerShell
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

### 3. Configurar CORS para Producción

⚠️ **MUY IMPORTANTE**: En producción, `CORS_ALLOWED_ORIGINS` debe contener SOLO tus dominios reales:

```bash
# ✅ CORRECTO (dominios específicos)
CORS_ALLOWED_ORIGINS=https://grcontrol.com,https://www.grcontrol.com,https://app.grcontrol.com

# ❌ INCORRECTO (NO incluir localhost en producción)
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://grcontrol.com
```

---

## 🏗️ Despliegue en Diferentes Plataformas

### Docker
```dockerfile
# En tu Dockerfile
ENV CORS_ALLOWED_ORIGINS=https://tudominio.com
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/grcontrol_db
# ... otras variables
```

### AWS Elastic Beanstalk
Configurar en: **Configuration → Software → Environment properties**

### Heroku
```bash
heroku config:set CORS_ALLOWED_ORIGINS=https://tudominio.com
heroku config:set SPRING_DATASOURCE_URL=jdbc:postgresql://...
```

### Railway / Render / Fly.io
Agregar variables en el dashboard de la plataforma

### VPS (Linux)
```bash
# Editar /etc/environment o crear archivo .env
export CORS_ALLOWED_ORIGINS=https://tudominio.com
export SPRING_DATASOURCE_URL=jdbc:postgresql://...

# Ejecutar
java -jar grcontrol_backend.jar
```

---

## 📱 Frontend - Configuración de API URL

### Mobile App (grcontrol_mobile)

**Desarrollo**:
```bash
# .env
EXPO_PUBLIC_API_URL=http://192.168.1.100:8080
```

**Producción**:
```bash
# .env.production
EXPO_PUBLIC_API_URL=https://api.tudominio.com
```

### Web App (grcontrol_web)

**Desarrollo**:
```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**Producción**:
```bash
# .env.production
NEXT_PUBLIC_API_URL=https://api.tudominio.com
```

---

## ✅ Checklist Pre-Producción

- [ ] Generar nuevo JWT_SECRET seguro
- [ ] Configurar CORS_ALLOWED_ORIGINS con dominios reales (sin localhost)
- [ ] Cambiar SPRING_JPA_HIBERNATE_DDL_AUTO a `validate` o `none`
- [ ] Desactivar logs de SQL (`SPRING_JPA_SHOW_SQL=false`)
- [ ] Configurar base de datos de producción
- [ ] Configurar logging a nivel INFO o WARN
- [ ] Probar conexión frontend → backend con dominios reales
- [ ] Verificar que SSL/HTTPS está activo
- [ ] Backup de base de datos configurado

---

## 🔍 Troubleshooting

### Error: "CORS policy: No 'Access-Control-Allow-Origin'"
- Verificar que el frontend esté usando la URL correcta
- Verificar que `CORS_ALLOWED_ORIGINS` incluya el dominio del frontend
- Verificar que el dominio incluya el protocolo (https://) y NO termine en /

### Error: "Invalid CORS request"
- Verificar que `allowCredentials=true` esté configurado
- Verificar que no estés usando `*` en allowedOrigins cuando allowCredentials=true

### Backend no lee variables de entorno
- En Windows, reiniciar el terminal después de setear variables
- Verificar que las variables estén en el scope correcto (System vs User)
- Usar `echo $env:VARIABLE_NAME` (PowerShell) para verificar

---

## 📚 Referencias

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [CORS Configuration](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [12-Factor App Methodology](https://12factor.net/config)
