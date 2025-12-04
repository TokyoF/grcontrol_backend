package com.grcontrol.grcontrol_backend.config;

import com.grcontrol.grcontrol_backend.entity.Island;
import com.grcontrol.grcontrol_backend.entity.Nozzle;
import com.grcontrol.grcontrol_backend.entity.Nozzle.FuelType;
import com.grcontrol.grcontrol_backend.entity.Nozzle.PumpSide;
import com.grcontrol.grcontrol_backend.entity.Pump;
import com.grcontrol.grcontrol_backend.entity.Role;
import com.grcontrol.grcontrol_backend.entity.ShiftSchedule;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.entity.WorkerAssignment;
import com.grcontrol.grcontrol_backend.repository.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos iniciales...");

        createRolesIfNotExist();
        createDefaultUsers();
        createSampleInfrastructure();
        createSampleShiftSchedules();
        createSampleWorkerAssignment();

        log.info("Carga de datos iniciales completada.");
    }

    private void createRolesIfNotExist() {
        createRoleIfNotExist(
            "ROLE_ADMINISTRADOR",
            "Administrador del sistema con acceso completo"
        );
        createRoleIfNotExist(
            "ROLE_GRIFERO",
            "Grifero - maneja ventas y turnos en la estación"
        );
        createRoleIfNotExist(
            "ROLE_FACTURADOR",
            "Facturador - maneja comprobantes y facturación"
        );
        createRoleIfNotExist(
            "ROLE_GERENTE",
            "Gerente general - visualiza reportes y gestión completa"
        );
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
        createUserIfNotExist(
            "admin",
            "admin@grcontrol.com",
            "admin123",
            "Admin",
            "Sistema",
            "999999999",
            "ROLE_ADMINISTRADOR"
        );

        createUserIfNotExist(
            "grifero",
            "grifero@grcontrol.com",
            "grifero123",
            "Juan",
            "Perez",
            "988888888",
            "ROLE_GRIFERO"
        );

        createUserIfNotExist(
            "facturador",
            "facturador@grcontrol.com",
            "facturador123",
            "Maria",
            "Lopez",
            "977777777",
            "ROLE_FACTURADOR"
        );

        createUserIfNotExist(
            "gerente",
            "gerente@grcontrol.com",
            "gerente123",
            "Carlos",
            "Rodriguez",
            "966666666",
            "ROLE_GERENTE"
        );

        createUserIfNotExist(
            "obaldeon",
            "2021200870@ucss.pe",
            "obaldeon123",
            "Olivia",
            "Baldeon",
            "969632509",
            "ROLE_GRIFERO"
        );
    }

    private void createUserIfNotExist(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        String roleName
    ) {
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
                .orElseThrow(() ->
                    new RuntimeException("Rol " + roleName + " no encontrado")
                );

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);
            log.info(
                "Usuario {} creado - Username: {}, Password: {}",
                roleName,
                username,
                password
            );
        } else {
            log.info("Usuario {} ya existe", username);
        }
    }

    private void createSampleInfrastructure() {
        // Buscar la estación Acobamba (debe existir desde la migración)
        Station station = stationRepository.findById(1L).orElse(null);

        if (station == null) {
            log.warn(
                "Estación Acobamba no encontrada, omitiendo creación de infraestructura de ejemplo"
            );
            return;
        }

        // Verificar si ya tiene islas
        if (!islandRepository.findByStationId(1L).isEmpty()) {
            log.info("La estación Acobamba ya tiene islas configuradas");
            return;
        }

        log.info("Creando infraestructura de ejemplo para Acobamba...");

        // Crear Isla 1
        Island island1 = new Island();
        island1.setStation(station);
        island1.setName("Isla 1");
        island1.setStatus(Island.IslandStatus.ACTIVE);
        island1 = islandRepository.save(island1);
        log.info("Isla creada: {}", island1.getName());

        // Crear Surtidor 1 en Isla 1
        Pump pump1 = new Pump();
        pump1.setIsland(island1);
        pump1.setName("Surtidor 1");
        pump1.setActive(true);
        pump1 = pumpRepository.save(pump1);
        log.info("Surtidor creado: {}", pump1.getName());

        // Crear mangueras para Surtidor 1
        createNozzle(
            pump1,
            "Manguera 1",
            FuelType.REGULAR,
            PumpSide.LEFT,
            0,
            new BigDecimal("15.50")
        );
        createNozzle(
            pump1,
            "Manguera 2",
            FuelType.PREMIUM,
            PumpSide.LEFT,
            1,
            new BigDecimal("17.80")
        );
        createNozzle(
            pump1,
            "Manguera 3",
            FuelType.DIESEL,
            PumpSide.LEFT,
            2,
            new BigDecimal("16.20")
        );
        createNozzle(
            pump1,
            "Manguera 4",
            FuelType.REGULAR,
            PumpSide.RIGHT,
            0,
            new BigDecimal("15.50")
        );
        createNozzle(
            pump1,
            "Manguera 5",
            FuelType.PREMIUM,
            PumpSide.RIGHT,
            1,
            new BigDecimal("17.80")
        );
        createNozzle(
            pump1,
            "Manguera 6",
            FuelType.DIESEL,
            PumpSide.RIGHT,
            2,
            new BigDecimal("16.20")
        );

        // Crear Isla 2
        Island island2 = new Island();
        island2.setStation(station);
        island2.setName("Isla 2");
        island2.setStatus(Island.IslandStatus.ACTIVE);
        island2 = islandRepository.save(island2);
        log.info("Isla creada: {}", island2.getName());

        // Crear Surtidor 2 en Isla 2
        Pump pump2 = new Pump();
        pump2.setIsland(island2);
        pump2.setName("Surtidor 2");
        pump2.setActive(true);
        pump2 = pumpRepository.save(pump2);
        log.info("Surtidor creado: {}", pump2.getName());

        // Crear mangueras para Surtidor 2
        createNozzle(
            pump2,
            "Manguera 1",
            FuelType.REGULAR,
            PumpSide.LEFT,
            0,
            new BigDecimal("15.50")
        );
        createNozzle(
            pump2,
            "Manguera 2",
            FuelType.PREMIUM,
            PumpSide.LEFT,
            1,
            new BigDecimal("17.80")
        );
        createNozzle(
            pump2,
            "Manguera 3",
            FuelType.DIESEL,
            PumpSide.LEFT,
            2,
            new BigDecimal("16.20")
        );
        createNozzle(
            pump2,
            "Manguera 4",
            FuelType.REGULAR,
            PumpSide.RIGHT,
            0,
            new BigDecimal("15.50")
        );
        createNozzle(
            pump2,
            "Manguera 5",
            FuelType.PREMIUM,
            PumpSide.RIGHT,
            1,
            new BigDecimal("17.80")
        );
        createNozzle(
            pump2,
            "Manguera 6",
            FuelType.DIESEL,
            PumpSide.RIGHT,
            2,
            new BigDecimal("16.20")
        );

        log.info("Infraestructura de ejemplo creada exitosamente");
    }

    private void createNozzle(
        Pump pump,
        String name,
        FuelType fuelType,
        PumpSide side,
        int position,
        BigDecimal pricePerGallon
    ) {
        Nozzle nozzle = new Nozzle();
        nozzle.setPump(pump);
        nozzle.setFuelName(name);
        nozzle.setFuelType(fuelType);
        nozzle.setSide(side);
        nozzle.setPosition(position);
        nozzle.setPricePerGallon(pricePerGallon);
        nozzle.setActive(true);
        nozzleRepository.save(nozzle);
        log.info(
            "Manguera creada: {} - {} - Lado {} - Precio: S/. {}",
            name,
            fuelType,
            side,
            pricePerGallon
        );
    }

    private void createSampleShiftSchedules() {
        // Buscar la estación Acobamba
        Station station = stationRepository.findById(1L).orElse(null);

        if (station == null) {
            log.warn(
                "Estación Acobamba no encontrada, omitiendo creación de horarios"
            );
            return;
        }

        // Verificar si ya tiene horarios
        if (!shiftScheduleRepository.findByStationId(1L).isEmpty()) {
            log.info("La estación Acobamba ya tiene horarios configurados");
            return;
        }

        log.info("Creando horarios de ejemplo para Acobamba...");

        createShiftSchedule(station, "Turno Día", "07:00", "19:00");
        createShiftSchedule(station, "Turno Noche", "19:00", "07:00");

        log.info("Horarios de ejemplo creados exitosamente");
    }

    private void createShiftSchedule(
        Station station,
        String displayLabel,
        String startTime,
        String endTime
    ) {
        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setStation(station);
        schedule.setDisplayLabel(displayLabel);
        schedule.setStartTime(LocalTime.parse(startTime));
        schedule.setEndTime(LocalTime.parse(endTime));
        shiftScheduleRepository.save(schedule);
        log.info(
            "Horario creado: {} ({} - {})",
            displayLabel,
            startTime,
            endTime
        );
    }

    private void createSampleWorkerAssignment() {
        // Buscar usuario operario
        User operario = userRepository.findByUsername("operario").orElse(null);

        if (operario == null) {
            log.warn(
                "Usuario operario no encontrado, omitiendo asignación de ejemplo"
            );
            return;
        }

        // Buscar Isla 1
        Island island1 = islandRepository.findById(1L).orElse(null);

        if (island1 == null) {
            log.warn("Isla 1 no encontrada, omitiendo asignación de ejemplo");
            return;
        }

        // Buscar horario "Turno Día"
        ShiftSchedule dayShift = shiftScheduleRepository
            .findByStationId(1L)
            .stream()
            .filter(s -> s.getDisplayLabel().equals("Turno Día"))
            .findFirst()
            .orElse(null);

        if (dayShift == null) {
            log.warn(
                "Horario Turno Día no encontrado, omitiendo asignación de ejemplo"
            );
            return;
        }

        // Verificar si ya existe asignación para esta semana
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        if (
            !workerAssignmentRepository
                .findByWorkerIdAndWeekStartDate(operario.getId(), monday)
                .isEmpty()
        ) {
            log.info("El operario ya tiene asignaciones para esta semana");
            return;
        }

        log.info("Creando asignación de ejemplo para operario en Isla 1...");

        // Crear asignaciones para toda la semana (Lunes a Sábado trabajo, Domingo descanso)
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkerAssignment assignment = new WorkerAssignment();
            assignment.setWorker(operario);
            assignment.setIsland(island1);
            assignment.setShiftSchedule(dayShift);
            assignment.setDayOfWeek(day);
            assignment.setWeekStartDate(monday);
            assignment.setIsRestDay(day == DayOfWeek.SUNDAY);
            workerAssignmentRepository.save(assignment);

            log.info(
                "Asignación creada: {} - {} - {}",
                day,
                day == DayOfWeek.SUNDAY ? "DESCANSO" : "Turno Día",
                island1.getName()
            );
        }

        log.info("Asignación semanal de ejemplo creada exitosamente");
    }
}
