# GRControl Backend 🏭

Sistema de gestión para estaciones de combustible - API REST Backend

## 🚀 Quick Start

### Desarrollo Local

```bash
# Opción 1: Usar valores por defecto
./mvnw spring-boot:run

# Opción 2: Usar script con variables de entorno (recomendado)
# Windows
run-dev.bat

# Linux/Mac
chmod +x run-dev.sh
./run-dev.sh
```

El servidor estará disponible en `http://localhost:8080`

## 📋 Requisitos

- Java 21+
- PostgreSQL 15+
- Maven 3.8+

## ⚙️ Configuración

### Variables de Entorno

Este proyecto usa variables de entorno para diferentes ambientes. Ver `DEPLOYMENT.md` para guía completa.

**Desarrollo**:
```bash
cp .env.development .env
# Editar .env según necesites
```

**Producción**:
```bash
# Configurar estas variables en tu servidor/plataforma
CORS_ALLOWED_ORIGINS=https://tudominio.com
SPRING_DATASOURCE_URL=jdbc:postgresql://...
JWT_SECRET=tu_secret_seguro
```

### Configuración de CORS

⚠️ **IMPORTANTE**: En producción, actualizar `CORS_ALLOWED_ORIGINS` con tus dominios reales:

```bash
# Desarrollo (localhost permitido)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:19006

# Producción (SOLO dominios reales)
CORS_ALLOWED_ORIGINS=https://tudominio.com,https://www.tudominio.com
```

## 📁 Estructura del Proyecto

```
grcontrol_backend/
├── src/main/java/com/grcontrol/grcontrol_backend/
│   ├── config/          # Configuración (Security, CORS, JWT)
│   ├── controller/      # REST Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── repository/     # Spring Data Repositories
│   ├── service/        # Business Logic
│   └── util/           # Utilities
├── src/main/resources/
│   └── application.properties
├── .env.example        # Plantilla de variables
├── .env.development    # Variables de desarrollo
├── .env.production     # Variables de producción (template)
└── DEPLOYMENT.md       # Guía de despliegue
```

## 🔑 Endpoints Principales

### Autenticación
- `POST /api/auth/login` - Login de usuario
- `POST /api/auth/register` - Registro (según rol)

### Shifts
- `POST /api/shifts/sync-batch` - Sincronización batch desde mobile
- `GET /api/shifts` - Listar turnos
- `GET /api/shifts/{id}` - Detalle de turno

### Usuarios
- `GET /api/users` - Listar usuarios (admin)
- `POST /api/users` - Crear usuario (admin)

Ver documentación completa en `/docs` después del deploy.

## 🧪 Testing

```bash
# Run tests
./mvnw test

# Run with coverage
./mvnw clean test jacoco:report
```

## 📦 Build

```bash
# Build JAR
./mvnw clean package

# Run JAR
java -jar target/grcontrol_backend-0.0.1-SNAPSHOT.jar
```

## 🔧 Tecnologías

- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL + JPA/Hibernate
- **Security**: Spring Security + JWT
- **Build**: Maven
- **Java**: 21

## 📚 Documentación Adicional

- [DEPLOYMENT.md](DEPLOYMENT.md) - Guía completa de despliegue
- [CLAUDE.md](../CLAUDE.md) - Guía de arquitectura del proyecto

## 🤝 Contribución

Ver estructura de commits en el proyecto principal.

## 📄 Licencia

Propietario - GRControl © 2025
