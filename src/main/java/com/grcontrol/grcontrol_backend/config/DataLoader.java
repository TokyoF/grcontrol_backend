package com.grcontrol.grcontrol_backend.config;

import com.grcontrol.grcontrol_backend.entity.FuelPriceHistory;
import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.Nozzle.FuelType;
import com.grcontrol.grcontrol_backend.entity.Nozzle.PumpSide;
import com.grcontrol.grcontrol_backend.entity.Pump;
import com.grcontrol.grcontrol_backend.entity.PumpReading;
import com.grcontrol.grcontrol_backend.entity.Role;
import com.grcontrol.grcontrol_backend.entity.ShiftSchedule;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.entity.WorkerAssignment;
import com.grcontrol.grcontrol_backend.repository.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final StationRepository stationRepository;
  private final IslandRepository islandRepository;
  private final PumpRepository pumpRepository;
  private final NozzleRepository nozzleRepository;
  private final ShiftScheduleRepository shiftScheduleRepository;
  private final WorkerAssignmentRepository workerAssignmentRepository;
  private final FuelPriceHistoryRepository fuelPriceHistoryRepository;
  private final ShiftSessionRepository shiftSessionRepository;
  private final PumpReadingRepository pumpReadingRepository;

  @Override
  public void run(String... args) throws Exception {
    log.info("Iniciando carga de datos iniciales...");

    createRolesIfNotExist();
    createDefaultUsers();
    createStationAcobamba(); // Crear estación Acobamba primero
    createInitialFuelPrices(); // Crear precios iniciales de combustibles
    createSampleInfrastructure();
    createSampleShiftSchedules();
    createSampleWorkerAssignment();
    initializeBaseReadings(); // ✨ Inicializar lecturas base para todas las mangueras

    log.info("Carga de datos iniciales completada.");
  }

  private void createRolesIfNotExist() {
    createRoleIfNotExist(
        "ROLE_ADMINISTRADOR",
        "Administrador del sistema con acceso completo");
    createRoleIfNotExist(
        "ROLE_GRIFERO",
        "Grifero - maneja ventas y turnos en la estación");
    createRoleIfNotExist(
        "ROLE_FACTURADOR",
        "Facturador - maneja comprobantes y facturación");
    createRoleIfNotExist(
        "ROLE_GERENTE",
        "Gerente general - visualiza reportes y gestión completa");
  }

  private void createRoleIfNotExist(String name, String description) {
    if (roleRepository.findByName(name).isEmpty()) {
      Role role = new Role();
      role.setName(name);
      role.setDescription(description);
      roleRepository.save(role);
      log.info("Rol creado: {}", name);
    } else {
      log.info("Rol ya existe: {}", name);
    }
  }

  private void createDefaultUsers() {
    // Admin
    createUserIfNotExist(
        "admin",
        "admin@grcontrol.com",
        "admin123",
        "Admin",
        "Sistema",
        "999999999",
        "ROLE_ADMINISTRADOR");

    // Facturador
    createUserIfNotExist(
        "facturador",
        "facturador@grcontrol.com",
        "facturador123",
        "Maria",
        "Lopez",
        "977777777",
        "ROLE_FACTURADOR");

    // Gerente
    createUserIfNotExist(
        "gerente",
        "gerente@grcontrol.com",
        "gerente123",
        "Carlos",
        "Rodriguez",
        "966666666",
        "ROLE_GERENTE");

    // ===== 10 GRIFEROS =====

    // Grifero 1 - Juan (trabaja L-S, descansa D)
    createUserIfNotExist(
        "jperez",
        "jperez@grcontrol.com",
        "grifero123",
        "Juan",
        "Perez",
        "988888888",
        "ROLE_GRIFERO");

    // Grifero 2 - Olivia (trabaja L-S, descansa D)
    createUserIfNotExist(
        "obaldeon",
        "2021200870@ucss.pe",
        "grifero123",
        "Olivia",
        "Baldeon",
        "969632509",
        "ROLE_GRIFERO");

    // Grifero 3 - Luis (trabaja L-S, descansa D)
    createUserIfNotExist(
        "lgarcia",
        "lgarcia@grcontrol.com",
        "grifero123",
        "Luis",
        "Garcia",
        "987654321",
        "ROLE_GRIFERO");

    // Grifero 4 - Ana (trabaja M-D, descansa L)
    createUserIfNotExist(
        "amartinez",
        "amartinez@grcontrol.com",
        "grifero123",
        "Ana",
        "Martinez",
        "976543210",
        "ROLE_GRIFERO");

    // Grifero 5 - Pedro (trabaja M-D, descansa L)
    createUserIfNotExist(
        "prosales",
        "prosales@grcontrol.com",
        "grifero123",
        "Pedro",
        "Rosales",
        "965432109",
        "ROLE_GRIFERO");

    // Grifero 6 - Sofia (trabaja M-D, descansa L)
    createUserIfNotExist(
        "scastro",
        "scastro@grcontrol.com",
        "grifero123",
        "Sofia",
        "Castro",
        "954321098",
        "ROLE_GRIFERO");

    // Grifero 7 - Miguel (trabaja X-S, descansa L-M)
    createUserIfNotExist(
        "mfernandez",
        "mfernandez@grcontrol.com",
        "grifero123",
        "Miguel",
        "Fernandez",
        "943210987",
        "ROLE_GRIFERO");

    // Grifero 8 - Carmen (trabaja X-S, descansa L-M)
    createUserIfNotExist(
        "cruiz",
        "cruiz@grcontrol.com",
        "grifero123",
        "Carmen",
        "Ruiz",
        "932109876",
        "ROLE_GRIFERO");

    // Grifero 9 - Roberto (trabaja X-S, descansa L-M)
    createUserIfNotExist(
        "rdiaz",
        "rdiaz@grcontrol.com",
        "grifero123",
        "Roberto",
        "Diaz",
        "921098765",
        "ROLE_GRIFERO");

    // Grifero 10 - Laura (COMODÍN - cubre días de descanso)
    createUserIfNotExist(
        "lvargas",
        "lvargas@grcontrol.com",
        "grifero123",
        "Laura",
        "Vargas",
        "910987654",
        "ROLE_GRIFERO");
  }

  private void createUserIfNotExist(
      String username,
      String email,
      String password,
      String firstName,
      String lastName,
      String phone,
      String roleName) {
    if (userRepository.findByUsername(username).isEmpty()) {
      User user = new User();
      user.setUsername(username);
      user.setEmail(email);
      user.setPassword(passwordEncoder.encode(password));
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setPhone(phone);
      user.setIsActive(true);

      Role role = roleRepository
          .findByName(roleName)
          .orElseThrow(() -> new RuntimeException("Rol " + roleName + " no encontrado"));

      Set<Role> roles = new HashSet<>();
      roles.add(role);
      user.setRoles(roles);

      userRepository.save(user);
      log.info(
          "Usuario {} creado - Username: {}, Password: {}",
          roleName,
          username,
          password);
    } else {
      log.info("Usuario {} ya existe", username);
    }
  }

  private void createStationAcobamba() {
    // Verificar si la estación Acobamba ya existe
    if (stationRepository.findById(1L).isPresent()) {
      log.info("Estación Acobamba ya existe");
      return;
    }

    log.info("Creando estación de servicio Acobamba...");

    Station station = new Station();
    station.setName("Grifo Acobamba");
    station.setAddress("Av. Acobamba 123, Huancayo");
    station.setPhone("064-123456");
    station.setActive(true);
    stationRepository.save(station);

    log.info(
        "Estación Acobamba creada exitosamente - ID: {}",
        station.getId());
  }

  private void createInitialFuelPrices() {
    // Buscar la estación Acobamba
    Station station = stationRepository.findById(1L).orElse(null);

    if (station == null) {
      log.warn("Estación Acobamba no encontrada, omitiendo creación de precios");
      return;
    }

    // Verificar si ya tiene precios
    if (!fuelPriceHistoryRepository.findAllCurrentPrices(1L).isEmpty()) {
      log.info("La estación Acobamba ya tiene precios configurados");
      return;
    }

    log.info("Creando precios iniciales de combustibles para Acobamba...");

    LocalDateTime now = LocalDateTime.now();

    // Crear precio para REGULAR
    createFuelPrice(
        station,
        FuelPriceHistory.FuelType.REGULAR,
        new BigDecimal("15.50"),
        now,
        "Precio inicial de Regular 90");

    // Crear precio para PREMIUM
    createFuelPrice(
        station,
        FuelPriceHistory.FuelType.PREMIUM,
        new BigDecimal("17.80"),
        now,
        "Precio inicial de Premium 95");

    // Crear precio para DIESEL
    createFuelPrice(
        station,
        FuelPriceHistory.FuelType.DIESEL,
        new BigDecimal("16.20"),
        now,
        "Precio inicial de Diesel B5");

    // Crear precio para GLP (opcional)
    createFuelPrice(
        station,
        FuelPriceHistory.FuelType.GLP,
        new BigDecimal("8.50"),
        now,
        "Precio inicial de GLP");

    log.info("Precios iniciales de combustibles creados exitosamente");
  }

  private void createFuelPrice(
      Station station,
      FuelPriceHistory.FuelType fuelType,
      BigDecimal price,
      LocalDateTime effectiveFrom,
      String notes) {
    FuelPriceHistory fuelPrice = new FuelPriceHistory();
    fuelPrice.setStation(station);
    fuelPrice.setFuelType(fuelType);
    fuelPrice.setPricePerGallon(price);
    fuelPrice.setEffectiveFrom(effectiveFrom);
    fuelPrice.setEffectiveUntil(null); // Precio actual
    fuelPrice.setNotes(notes);

    // Obtener usuario admin como quien establece el precio
    userRepository.findByUsername("admin")
        .ifPresent(fuelPrice::setChangedBy);

    fuelPriceHistoryRepository.save(fuelPrice);
    log.info("Precio creado: {} - S/. {} por galón", fuelType, price);
  }

  private void createSampleInfrastructure() {
    Station station = stationRepository.findById(1L).orElse(null);

    if (station == null) {
      log.warn("Estación Acobamba no encontrada, omitiendo creación de infraestructura");
      return;
    }

    if (!islandRepository.findByStationId(1L).isEmpty()) {
      log.info("La estación Acobamba ya tiene islas configuradas");
      return;
    }

    log.info("Creando infraestructura REALISTA para Acobamba (3 islas)...");

    // ========== ISLA 1 - COMBUSTIBLES LÍQUIDOS (2 surtidores) ==========
    Island island1 = new Island();
    island1.setStation(station);
    island1.setName("Isla 1");
    island1.setDescription("Combustibles líquidos - Regular, Premium, Diesel");
    island1.setStatus(Island.IslandStatus.ACTIVE);
    island1 = islandRepository.save(island1);
    log.info("✅ Isla 1 creada");

    // Surtidor 1 - Isla 1 (3 mangueras izquierda, 4 derecha = 7 total)
    Pump pump1_1 = new Pump();
    pump1_1.setIsland(island1);
    pump1_1.setName("Surtidor 1");
    pump1_1.setPosition(0);
    pump1_1.setActive(true);
    pump1_1 = pumpRepository.save(pump1_1);

    // Lado IZQUIERDO (3 mangueras)
    createNozzle(pump1_1, "Regular 90", FuelType.REGULAR, PumpSide.LEFT, 0, null);
    createNozzle(pump1_1, "Premium 95", FuelType.PREMIUM, PumpSide.LEFT, 1, null);
    createNozzle(pump1_1, "Diesel B5", FuelType.DIESEL, PumpSide.LEFT, 2, null);

    // Lado DERECHO (4 mangueras)
    createNozzle(pump1_1, "Regular 90", FuelType.REGULAR, PumpSide.RIGHT, 0, null);
    createNozzle(pump1_1, "Premium 95", FuelType.PREMIUM, PumpSide.RIGHT, 1, null);
    createNozzle(pump1_1, "Premium 97", FuelType.PREMIUM, PumpSide.RIGHT, 2, null);
    createNozzle(pump1_1, "Diesel B5", FuelType.DIESEL, PumpSide.RIGHT, 3, null);

    log.info("  ✓ Surtidor 1 - Isla 1: 7 mangueras (3 izq + 4 der)");

    // Surtidor 2 - Isla 1 (3 mangueras izquierda, 4 derecha = 7 total)
    Pump pump1_2 = new Pump();
    pump1_2.setIsland(island1);
    pump1_2.setName("Surtidor 2");
    pump1_2.setPosition(1);
    pump1_2.setActive(true);
    pump1_2 = pumpRepository.save(pump1_2);

    // Lado IZQUIERDO (3 mangueras)
    createNozzle(pump1_2, "Regular 90", FuelType.REGULAR, PumpSide.LEFT, 0, null);
    createNozzle(pump1_2, "Premium 95", FuelType.PREMIUM, PumpSide.LEFT, 1, null);
    createNozzle(pump1_2, "Diesel B5", FuelType.DIESEL, PumpSide.LEFT, 2, null);

    // Lado DERECHO (4 mangueras)
    createNozzle(pump1_2, "Regular 90", FuelType.REGULAR, PumpSide.RIGHT, 0, null);
    createNozzle(pump1_2, "Premium 95", FuelType.PREMIUM, PumpSide.RIGHT, 1, null);
    createNozzle(pump1_2, "Premium 97", FuelType.PREMIUM, PumpSide.RIGHT, 2, null);
    createNozzle(pump1_2, "Diesel B5", FuelType.DIESEL, PumpSide.RIGHT, 3, null);

    log.info("  ✓ Surtidor 2 - Isla 1: 7 mangueras (3 izq + 4 der)");

    // ========== ISLA 2 - COMBUSTIBLES LÍQUIDOS (2 surtidores) ==========
    Island island2 = new Island();
    island2.setStation(station);
    island2.setName("Isla 2");
    island2.setDescription("Combustibles líquidos - Regular, Premium, Diesel");
    island2.setStatus(Island.IslandStatus.ACTIVE);
    island2 = islandRepository.save(island2);
    log.info("✅ Isla 2 creada");

    // Surtidor 1 - Isla 2 (3 mangueras izquierda, 4 derecha)
    Pump pump2_1 = new Pump();
    pump2_1.setIsland(island2);
    pump2_1.setName("Surtidor 1");
    pump2_1.setPosition(0);
    pump2_1.setActive(true);
    pump2_1 = pumpRepository.save(pump2_1);

    createNozzle(pump2_1, "Regular 90", FuelType.REGULAR, PumpSide.LEFT, 0, null);
    createNozzle(pump2_1, "Premium 95", FuelType.PREMIUM, PumpSide.LEFT, 1, null);
    createNozzle(pump2_1, "Diesel B5", FuelType.DIESEL, PumpSide.LEFT, 2, null);

    createNozzle(pump2_1, "Regular 90", FuelType.REGULAR, PumpSide.RIGHT, 0, null);
    createNozzle(pump2_1, "Premium 95", FuelType.PREMIUM, PumpSide.RIGHT, 1, null);
    createNozzle(pump2_1, "Premium 97", FuelType.PREMIUM, PumpSide.RIGHT, 2, null);
    createNozzle(pump2_1, "Diesel B5", FuelType.DIESEL, PumpSide.RIGHT, 3, null);

    log.info("  ✓ Surtidor 1 - Isla 2: 7 mangueras (3 izq + 4 der)");

    // Surtidor 2 - Isla 2 (3 mangueras izquierda, 4 derecha)
    Pump pump2_2 = new Pump();
    pump2_2.setIsland(island2);
    pump2_2.setName("Surtidor 2");
    pump2_2.setPosition(1);
    pump2_2.setActive(true);
    pump2_2 = pumpRepository.save(pump2_2);

    createNozzle(pump2_2, "Regular 90", FuelType.REGULAR, PumpSide.LEFT, 0, null);
    createNozzle(pump2_2, "Premium 95", FuelType.PREMIUM, PumpSide.LEFT, 1, null);
    createNozzle(pump2_2, "Diesel B5", FuelType.DIESEL, PumpSide.LEFT, 2, null);

    createNozzle(pump2_2, "Regular 90", FuelType.REGULAR, PumpSide.RIGHT, 0, null);
    createNozzle(pump2_2, "Premium 95", FuelType.PREMIUM, PumpSide.RIGHT, 1, null);
    createNozzle(pump2_2, "Premium 97", FuelType.PREMIUM, PumpSide.RIGHT, 2, null);
    createNozzle(pump2_2, "Diesel B5", FuelType.DIESEL, PumpSide.RIGHT, 3, null);

    log.info("  ✓ Surtidor 2 - Isla 2: 7 mangueras (3 izq + 4 der)");

    // ========== ISLA 3 - GLP (2 surtidores, 1 manguera por lado) ==========
    Island island3 = new Island();
    island3.setStation(station);
    island3.setName("Isla 3");
    island3.setDescription("Gas Licuado de Petróleo (GLP)");
    island3.setStatus(Island.IslandStatus.ACTIVE);
    island3 = islandRepository.save(island3);
    log.info("✅ Isla 3 creada (GLP)");

    // Surtidor 1 - Isla 3 (1 manguera por lado = 2 total)
    Pump pump3_1 = new Pump();
    pump3_1.setIsland(island3);
    pump3_1.setName("Surtidor 1");
    pump3_1.setPosition(0);
    pump3_1.setActive(true);
    pump3_1 = pumpRepository.save(pump3_1);

    createNozzle(pump3_1, "GLP", FuelType.GLP, PumpSide.LEFT, 0, null);
    createNozzle(pump3_1, "GLP", FuelType.GLP, PumpSide.RIGHT, 0, null);

    log.info("  ✓ Surtidor 1 - Isla 3: 2 mangueras GLP (1 izq + 1 der)");

    // Surtidor 2 - Isla 3 (1 manguera por lado = 2 total)
    Pump pump3_2 = new Pump();
    pump3_2.setIsland(island3);
    pump3_2.setName("Surtidor 2");
    pump3_2.setPosition(1);
    pump3_2.setActive(true);
    pump3_2 = pumpRepository.save(pump3_2);

    createNozzle(pump3_2, "GLP", FuelType.GLP, PumpSide.LEFT, 0, null);
    createNozzle(pump3_2, "GLP", FuelType.GLP, PumpSide.RIGHT, 0, null);

    log.info("  ✓ Surtidor 2 - Isla 3: 2 mangueras GLP (1 izq + 1 der)");

    log.info("✅ INFRAESTRUCTURA COMPLETA:");
    log.info("   • Isla 1: 14 mangueras (2 surtidores líquidos)");
    log.info("   • Isla 2: 14 mangueras (2 surtidores líquidos)");
    log.info("   • Isla 3: 4 mangueras (2 surtidores GLP)");
    log.info("   • TOTAL: 32 mangueras en 6 surtidores");
  }

  private void createNozzle(
      Pump pump,
      String name,
      FuelType fuelType,
      PumpSide side,
      int position,
      BigDecimal pricePerGallon // Mantener parámetro por compatibilidad
  ) {
    Nozzle nozzle = new Nozzle();
    nozzle.setPump(pump);
    nozzle.setFuelName(name);
    nozzle.setFuelType(fuelType);
    nozzle.setSide(side);
    nozzle.setPosition(position);
    // No establecer precio aquí - se obtiene de FuelPriceHistory
    // nozzle.setPricePerGallon(pricePerGallon); // DEPRECATED

    // Asignar color según tipo de combustible
    switch (fuelType) {
      case REGULAR -> nozzle.setColor("Rojo");
      case PREMIUM -> nozzle.setColor("Verde");
      case DIESEL -> nozzle.setColor("Negro");
      case GLP -> nozzle.setColor("Azul");
    }

    nozzle.setActive(true);
    nozzleRepository.save(nozzle);
    log.info(
        "Manguera creada: {} - {} - Lado {} - Color: {}",
        name,
        fuelType,
        side,
        nozzle.getColor());
  }

  private void createSampleShiftSchedules() {
    // Buscar la estación Acobamba
    Station station = stationRepository.findById(1L).orElse(null);

    if (station == null) {
      log.warn(
          "Estación Acobamba no encontrada, omitiendo creación de horarios");
      return;
    }

    // Verificar si ya tiene horarios
    if (!shiftScheduleRepository.findByStationId(1L).isEmpty()) {
      log.info("La estación Acobamba ya tiene horarios configurados");
      return;
    }

    log.info("Creando horarios realistas para Acobamba (3 turnos de 8 horas)...");

    // Turno Mañana: 6:00 AM - 2:00 PM
    createShiftSchedule(station, "Turno Mañana", "06:00", "14:00", false);

    // Turno Tarde: 2:00 PM - 10:00 PM
    createShiftSchedule(station, "Turno Tarde", "14:00", "22:00", false);

    // Turno Noche: 10:00 PM - 6:00 AM (overnight)
    createShiftSchedule(station, "Turno Noche", "22:00", "06:00", true);

    log.info("3 turnos de 8 horas creados exitosamente");
  }

  private void createShiftSchedule(
      Station station,
      String name,
      String startTime,
      String endTime,
      boolean isOvernight) {
    ShiftSchedule schedule = new ShiftSchedule();
    schedule.setStation(station);
    schedule.setName(name);
    schedule.setDisplayLabel(name);
    schedule.setStartTime(LocalTime.parse(startTime));
    schedule.setEndTime(LocalTime.parse(endTime));
    schedule.setIsOvernight(isOvernight);
    schedule.setActive(true);
    shiftScheduleRepository.save(schedule);
    log.info("Horario creado: {} ({} - {}) {}",
        name, startTime, endTime, isOvernight ? "[NOCTURNO]" : "");
  }

  private void createSampleWorkerAssignment() {
    log.info("Creando asignaciones REALISTAS para 10 trabajadores...");

    // Verificar si ya existen asignaciones
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(DayOfWeek.MONDAY);

    if (workerAssignmentRepository.count() > 0) {
      log.info("Ya existen asignaciones en la base de datos");
      return;
    }

    // Obtener turnos
    List<ShiftSchedule> shifts = shiftScheduleRepository.findByStationId(1L);
    ShiftSchedule morningShift = shifts.stream()
        .filter(s -> s.getName().equals("Turno Mañana"))
        .findFirst().orElse(null);
    ShiftSchedule afternoonShift = shifts.stream()
        .filter(s -> s.getName().equals("Turno Tarde"))
        .findFirst().orElse(null);
    ShiftSchedule nightShift = shifts.stream()
        .filter(s -> s.getName().equals("Turno Noche"))
        .findFirst().orElse(null);

    if (morningShift == null || afternoonShift == null || nightShift == null) {
      log.warn("No se encontraron los 3 turnos, omitiendo asignaciones");
      return;
    }

    // Obtener islas
    List<Island> islands = islandRepository.findByStationId(1L);
    Island island1 = islands.stream().filter(i -> i.getName().equals("Isla 1")).findFirst().orElse(null);
    Island island2 = islands.stream().filter(i -> i.getName().equals("Isla 2")).findFirst().orElse(null);
    Island island3 = islands.stream().filter(i -> i.getName().equals("Isla 3")).findFirst().orElse(null);

    if (island1 == null || island2 == null || island3 == null) {
      log.warn("No se encontraron las 3 islas, omitiendo asignaciones");
      return;
    }

    // Obtener trabajadores
    User juan = userRepository.findByUsername("jperez").orElse(null);
    User olivia = userRepository.findByUsername("obaldeon").orElse(null);
    User luis = userRepository.findByUsername("lgarcia").orElse(null);
    User ana = userRepository.findByUsername("amartinez").orElse(null);
    User pedro = userRepository.findByUsername("prosales").orElse(null);
    User sofia = userRepository.findByUsername("scastro").orElse(null);
    User miguel = userRepository.findByUsername("mfernandez").orElse(null);
    User carmen = userRepository.findByUsername("cruiz").orElse(null);
    User roberto = userRepository.findByUsername("rdiaz").orElse(null);
    User laura = userRepository.findByUsername("lvargas").orElse(null);

    log.info("📋 CREANDO ASIGNACIONES SEMANALES:");

    // ========== TURNO MAÑANA (6-14) ==========

    // Juan - Isla 1 - Turno Mañana - L a S (descansa D)
    if (juan != null) {
      createWeeklyAssignment(juan, island1, island1, morningShift, monday, DayOfWeek.SUNDAY);
      log.info("  ✓ Juan: Isla 1, Turno Mañana, L-S (descansa D)");
    }

    // Olivia - Isla 2 - Turno Mañana - L a S (descansa D)
    if (olivia != null) {
      createWeeklyAssignment(olivia, island2, island1, morningShift, monday, DayOfWeek.SUNDAY);
      log.info("  ✓ Olivia: Isla 2, Turno Mañana, L-S (descansa D)");
    }

    // Luis - Isla 3 (GLP) - Turno Mañana - L a S (descansa D)
    if (luis != null) {
      createWeeklyAssignment(luis, island3, island1, morningShift, monday, DayOfWeek.SUNDAY);
      log.info("  ✓ Luis: Isla 3, Turno Mañana, L-S (descansa D)");
    }

    // ========== TURNO TARDE (14-22) ==========

    // Ana - Isla 1 - Turno Tarde - M a D (descansa L)
    if (ana != null) {
      createWeeklyAssignment(ana, island1, island1, afternoonShift, monday, DayOfWeek.MONDAY);
      log.info("  ✓ Ana: Isla 1, Turno Tarde, M-D (descansa L)");
    }

    // Pedro - Isla 2 - Turno Tarde - M a D (descansa L)
    if (pedro != null) {
      createWeeklyAssignment(pedro, island2, island1, afternoonShift, monday, DayOfWeek.MONDAY);
      log.info("  ✓ Pedro: Isla 2, Turno Tarde, M-D (descansa L)");
    }

    // Sofia - Isla 3 (GLP) - Turno Tarde - M a D (descansa L)
    if (sofia != null) {
      createWeeklyAssignment(sofia, island3, island1, afternoonShift, monday, DayOfWeek.MONDAY);
      log.info("  ✓ Sofia: Isla 3, Turno Tarde, M-D (descansa L)");
    }

    // ========== TURNO NOCHE (22-6) ==========

    // Miguel - Isla 1 - Turno Noche - X a S (descansa L-M)
    if (miguel != null) {
      createWeeklyAssignmentWithTwoRestDays(miguel, island1, island1, nightShift, monday,
          DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
      log.info("  ✓ Miguel: Isla 1, Turno Noche, X-S (descansa L-M)");
    }

    // Carmen - Isla 2 - Turno Noche - X a S (descansa L-M)
    if (carmen != null) {
      createWeeklyAssignmentWithTwoRestDays(carmen, island2, island1, nightShift, monday,
          DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
      log.info("  ✓ Carmen: Isla 2, Turno Noche, X-S (descansa L-M)");
    }

    // Roberto - Isla 3 (GLP) - Turno Noche - X a S (descansa L-M)
    if (roberto != null) {
      createWeeklyAssignmentWithTwoRestDays(roberto, island3, island1, nightShift, monday,
          DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
      log.info("  ✓ Roberto: Isla 3, Turno Noche, X-S (descansa L-M)");
    }

    // ========== LAURA (COMODÍN) - Cubre días de descanso ==========

    if (laura != null) {
      // Laura cubre el DOMINGO en Isla 1, Turno Mañana (día libre de Juan)
      createSingleAssignment(laura, island1, morningShift, monday, DayOfWeek.SUNDAY, false, island1, morningShift);

      // Laura cubre el LUNES en Isla 1, Turno Tarde (día libre de Ana)
      createSingleAssignment(laura, island1, afternoonShift, monday, DayOfWeek.MONDAY, false, island1, morningShift);

      // Laura cubre el LUNES en Isla 1, Turno Noche (día libre de Miguel)
      createSingleAssignment(laura, island1, nightShift, monday, DayOfWeek.MONDAY, false, island1, morningShift);

      // Laura descansa Martes a Sábado
      for (DayOfWeek day : new DayOfWeek[] { DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
          DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY }) {
        createSingleAssignment(laura, island1, null, monday, day, true, island1, morningShift);
      }

      log.info("  ✓ Laura (COMODÍN): Cubre D (Isla1-Mañana), L (Isla1-Tarde y Noche)");
    }

    log.info("✅ ASIGNACIONES COMPLETAS: 10 trabajadores, 3 turnos, 3 islas");
    log.info("   • Turno Mañana (6-14): 3 trabajadores + 1 comodín");
    log.info("   • Turno Tarde (14-22): 3 trabajadores + 1 comodín");
    log.info("   • Turno Noche (22-6): 3 trabajadores + 1 comodín");
    log.info("   • Total: 9 trabajadores regulares + 1 comodín (Laura)");
  }

  /**
   * Crear asignación semanal con 1 día de descanso
   */
  private void createWeeklyAssignment(User worker, Island islandForWork, Island islandForRest, ShiftSchedule shift,
      LocalDate monday, DayOfWeek restDay) {
    for (DayOfWeek day : DayOfWeek.values()) {
      WorkerAssignment assignment = new WorkerAssignment();
      assignment.setWorker(worker);
      assignment.setWeekStartDate(monday);
      assignment.setDayOfWeek(day);

      if (day == restDay) {
        assignment.setIsland(islandForRest); // Assign a valid island for rest days
        assignment.setShiftSchedule(shift);
        assignment.setRestDay(true);
      } else {
        assignment.setIsland(islandForWork);
        assignment.setShiftSchedule(shift);
        assignment.setRestDay(false);
      }

      assignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);
      workerAssignmentRepository.save(assignment);
    }
  }

  /**
   * Crear asignación semanal con 2 días de descanso
   */
  private void createWeeklyAssignmentWithTwoRestDays(User worker, Island islandForWork, Island islandForRest,
      ShiftSchedule shift,
      LocalDate monday, DayOfWeek restDay1, DayOfWeek restDay2) {
    for (DayOfWeek day : DayOfWeek.values()) {
      WorkerAssignment assignment = new WorkerAssignment();
      assignment.setWorker(worker);
      assignment.setWeekStartDate(monday);
      assignment.setDayOfWeek(day);

      if (day == restDay1 || day == restDay2) {
        assignment.setIsland(islandForRest); // Assign a valid island for rest days
        assignment.setShiftSchedule(shift);
        assignment.setRestDay(true);
      } else {
        assignment.setIsland(islandForWork);
        assignment.setShiftSchedule(shift);
        assignment.setRestDay(false);
      }

      assignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);
      workerAssignmentRepository.save(assignment);
    }
  }

  /**
   * Crear asignación para un solo día
   */
  private void createSingleAssignment(User worker, Island island, ShiftSchedule shift,
      LocalDate monday, DayOfWeek day, boolean isRestDay, Island defaultIslandForRest,
      ShiftSchedule defaultShiftForRest) {
    WorkerAssignment assignment = new WorkerAssignment();
    assignment.setWorker(worker);
    assignment.setWeekStartDate(monday);
    assignment.setDayOfWeek(day);

    if (isRestDay) {
      assignment.setIsland(defaultIslandForRest);
      assignment.setShiftSchedule(defaultShiftForRest); // Assign a valid shift for rest days
    } else {
      assignment.setIsland(island);
      assignment.setShiftSchedule(shift);
    }

    assignment.setRestDay(isRestDay);
    assignment.setStatus(WorkerAssignment.AssignmentStatus.ACTIVE);
    workerAssignmentRepository.save(assignment);
  }

  /**
   * ✨ Inicializar lecturas BASE para todas las mangueras
   * Valores iniciales: Soles 324950.12, Galones 4573.435, Reloj 23783
   */
  @Transactional
  private void initializeBaseReadings() {
    log.info("Inicializando lecturas base para todas las mangueras...");

    // Verificar si ya existen lecturas
    long readingCount = pumpReadingRepository.count();
    if (readingCount > 0) {
      log.info("Ya existen {} lecturas en la base de datos, omitiendo inicialización", readingCount);
      return;
    }

    Station station = stationRepository.findById(1L).orElse(null);
    if (station == null) {
      log.warn("Estación no encontrada, omitiendo inicialización de lecturas base");
      return;
    }

    // Obtener todas las mangueras con relaciones cargadas (JOIN FETCH para evitar
    // LazyInitializationException)
    List<Nozzle> nozzles = nozzleRepository.findAllByStationIdWithRelations(station.getId());
    log.info("Encontradas {} mangueras para inicializar", nozzles.size());

    // Crear sesión ficticia para las lecturas base
    User admin = userRepository.findByUsername("admin").orElse(null);
    if (admin == null) {
      log.warn("Usuario admin no encontrado, omitiendo inicialización");
      return;
    }

    ShiftSession baseSession = new ShiftSession();
    baseSession.setSessionId("BASE-INITIAL-" + System.currentTimeMillis());
    baseSession.setOperator(admin);
    baseSession.setStation(station);
    baseSession.setStationName(station.getName());
    baseSession.setStationId(station.getId().intValue());
    baseSession.setShiftTime("INIT");
    // Fecha muy antigua para que no aparezca en listados normales
    baseSession.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 0));
    baseSession.setEndTime(LocalDateTime.of(2000, 1, 1, 1, 0));
    baseSession.setStatus(ShiftSession.SessionStatus.COMPLETED);
    baseSession.setSyncStatus(ShiftSession.SyncStatus.SYNCED);
    baseSession.setTotalSales(0.0);
    baseSession = shiftSessionRepository.save(baseSession);

    int totalReadings = 0;

    for (Nozzle nozzle : nozzles) {
      log.info("Inicializando manguera: {} - {} (Lado: {}, Posición: {})",
          nozzle.getPump().getName(),
          nozzle.getFuelName(),
          nozzle.getSide(),
          nozzle.getPosition());

      // SOLES
      if (nozzle.getHasSolesCounter()) {
        createInitialReading(
            baseSession,
            nozzle,
            PumpReading.ReadingType.SOLES,
            324950.12,
            nozzle.getSolesTotalDigits(),
            nozzle.getSolesDecimals());
        totalReadings++;
      }

      // GALLONS
      if (nozzle.getHasGallonsCounter()) {
        createInitialReading(
            baseSession,
            nozzle,
            PumpReading.ReadingType.GALLONS,
            4573.435,
            nozzle.getGallonsTotalDigits(),
            nozzle.getGallonsDecimals());
        totalReadings++;
      }

      // CLOCK
      if (nozzle.getHasClockCounter()) {
        createInitialReading(
            baseSession,
            nozzle,
            PumpReading.ReadingType.CLOCK,
            23783.0,
            nozzle.getClockTotalDigits(),
            nozzle.getClockDecimals());
        totalReadings++;
      }
    }

    log.info("✅ Inicialización completada: {} lecturas base creadas para {} mangueras",
        totalReadings, nozzles.size());
    log.info("ℹ️ Sesión base creada con fecha antigua (2000-01-01) para no aparecer en listados normales");
  }

  /**
   * Crear una lectura inicial con valor base específico
   */
  private void createInitialReading(
      ShiftSession session,
      Nozzle nozzle,
      PumpReading.ReadingType type,
      double baseValue,
      int totalDigits,
      int decimals) {
    // Formatear el valor base según dígitos y decimales
    long valueAsInt = Math.round(baseValue * Math.pow(10, decimals));
    String valueStr = String.format("%0" + totalDigits + "d", valueAsInt);

    PumpReading reading = new PumpReading();
    reading.setSession(session);
    reading.setNozzle(nozzle);
    reading.setIslandName(nozzle.getPump().getIsland().getName());
    reading.setIslandId(nozzle.getPump().getIsland().getId().intValue());
    reading.setPumpName(nozzle.getPump().getName());
    reading.setPumpId(nozzle.getPump().getId().intValue());
    reading.setSide(nozzle.getSide() == Nozzle.PumpSide.LEFT ? PumpReading.PumpSide.LEFT : PumpReading.PumpSide.RIGHT);
    reading.setNozzleIndex(nozzle.getPosition());
    reading.setFuelType(PumpReading.FuelType.valueOf(nozzle.getFuelType().name()));
    reading.setFuelName(nozzle.getFuelType().toString());
    reading.setReadingType(type);
    reading.setEntryDigits(valueStr);
    reading.setExitDigits(valueStr);
    reading.setDifference(0.0);
    reading.setCompleted(true);
    reading.setReadingTimestamp(session.getStartTime());
    reading.setSyncStatus(PumpReading.SyncStatus.SYNCED);

    pumpReadingRepository.save(reading);

    log.info("  ✓ {} inicializado con valor: {}", type, baseValue);
  }
}
