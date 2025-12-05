-- ==========================================
-- Setup Olivia Obaldeon para pruebas Mobile
-- ==========================================

-- 1. Verificar datos de Olivia
SELECT u.id, u.username, u.first_name, u.last_name, u.email
FROM users u
WHERE u.username = 'olivia.obaldeon';

-- 2. Verificar roles de Olivia (debe tener ROLE_GRIFERO)
SELECT u.username, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'olivia.obaldeon';

-- 3. Verificar estaciones disponibles
SELECT id, name, address FROM stations WHERE active = true;

-- 4. Verificar islas disponibles (usaremos la primera)
SELECT i.id, i.name, i.status, s.name as station_name
FROM islands i
JOIN stations s ON i.station_id = s.id
WHERE i.active = true;

-- 5. Verificar turnos disponibles
SELECT id, station_id, name, display_label, start_time, end_time, is_overnight
FROM shift_schedules
WHERE active = true
ORDER BY start_time;

-- 6. Obtener fecha de inicio de semana (Lunes)
-- En PostgreSQL:
-- SELECT date_trunc('week', CURRENT_DATE)::date as week_start;

-- 7. CREAR ASIGNACIÓN PARA OLIVIA - HOY (Miércoles 4 de Diciembre 2025)
-- Ajustar según tus IDs reales

-- Primero, eliminar asignaciones conflictivas de Olivia para esta semana
DELETE FROM worker_assignments
WHERE worker_id = (SELECT id FROM users WHERE username = 'olivia.obaldeon')
  AND week_start_date >= date_trunc('week', CURRENT_DATE)::date
  AND week_start_date < date_trunc('week', CURRENT_DATE)::date + INTERVAL '7 days';

-- Insertar asignación para HOY (Miércoles)
-- Asumiendo:
-- - Olivia ID: se obtiene dinámicamente
-- - Turno: 6-14 (turno mañana)
-- - Isla: primera isla disponible
-- - Semana actual

INSERT INTO worker_assignments (
    worker_id,
    week_start_date,
    day_of_week,
    shift_schedule_id,
    island_id,
    is_rest_day,
    status,
    created_at,
    updated_at
)
SELECT
    u.id as worker_id,
    date_trunc('week', CURRENT_DATE)::date as week_start_date,
    'WEDNESDAY' as day_of_week,
    ss.id as shift_schedule_id,
    i.id as island_id,
    false as is_rest_day,
    'ACTIVE' as status,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM users u
CROSS JOIN shift_schedules ss
CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon'
  AND ss.name = 'TURNO_MANANA'  -- Turno 6-14
  AND ss.active = true
  AND i.active = true
LIMIT 1;

-- 8. Verificar asignación creada
SELECT 
    wa.id,
    u.username,
    u.first_name,
    u.last_name,
    wa.week_start_date,
    wa.day_of_week,
    ss.display_label as shift,
    ss.start_time,
    ss.end_time,
    i.name as island,
    wa.status
FROM worker_assignments wa
JOIN users u ON wa.worker_id = u.id
JOIN shift_schedules ss ON wa.shift_schedule_id = ss.id
JOIN islands i ON wa.island_id = i.id
WHERE u.username = 'olivia.obaldeon'
  AND wa.week_start_date = date_trunc('week', CURRENT_DATE)::date
  AND wa.day_of_week = 'WEDNESDAY';

-- 9. OPCIONAL: Asignar toda la semana (Lunes a Domingo)
-- Lunes - Turno Mañana
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'MONDAY', ss.id, i.id, false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u CROSS JOIN shift_schedules ss CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon' AND ss.name = 'TURNO_MANANA' AND ss.active = true AND i.active = true LIMIT 1;

-- Martes - Turno Mañana
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'TUESDAY', ss.id, i.id, false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u CROSS JOIN shift_schedules ss CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon' AND ss.name = 'TURNO_MANANA' AND ss.active = true AND i.active = true LIMIT 1;

-- Jueves - Turno Tarde
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'THURSDAY', ss.id, i.id, false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u CROSS JOIN shift_schedules ss CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon' AND ss.name = 'TURNO_TARDE' AND ss.active = true AND i.active = true LIMIT 1;

-- Viernes - Turno Tarde
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'FRIDAY', ss.id, i.id, false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u CROSS JOIN shift_schedules ss CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon' AND ss.name = 'TURNO_TARDE' AND ss.active = true AND i.active = true LIMIT 1;

-- Sábado - Turno Mañana
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'SATURDAY', ss.id, i.id, false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u CROSS JOIN shift_schedules ss CROSS JOIN islands i
WHERE u.username = 'olivia.obaldeon' AND ss.name = 'TURNO_MANANA' AND ss.active = true AND i.active = true LIMIT 1;

-- Domingo - DÍA DE DESCANSO
INSERT INTO worker_assignments (worker_id, week_start_date, day_of_week, shift_schedule_id, island_id, is_rest_day, status, created_at, updated_at)
SELECT u.id, date_trunc('week', CURRENT_DATE)::date, 'SUNDAY', NULL, NULL, true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.username = 'olivia.obaldeon'
LIMIT 1;

-- 10. Ver todas las asignaciones de Olivia para esta semana
SELECT 
    wa.id,
    wa.day_of_week,
    CASE 
        WHEN wa.is_rest_day THEN '🛌 DESCANSO'
        ELSE ss.display_label
    END as shift,
    CASE 
        WHEN wa.is_rest_day THEN '-'
        ELSE CONCAT(ss.start_time, ' - ', ss.end_time)
    END as horario,
    COALESCE(i.name, '-') as island,
    wa.status
FROM worker_assignments wa
LEFT JOIN shift_schedules ss ON wa.shift_schedule_id = ss.id
LEFT JOIN islands i ON wa.island_id = i.id
JOIN users u ON wa.worker_id = u.id
WHERE u.username = 'olivia.obaldeon'
  AND wa.week_start_date = date_trunc('week', CURRENT_DATE)::date
ORDER BY 
    CASE wa.day_of_week
        WHEN 'MONDAY' THEN 1
        WHEN 'TUESDAY' THEN 2
        WHEN 'WEDNESDAY' THEN 3
        WHEN 'THURSDAY' THEN 4
        WHEN 'FRIDAY' THEN 5
        WHEN 'SATURDAY' THEN 6
        WHEN 'SUNDAY' THEN 7
    END;

-- ==========================================
-- NOTAS:
-- ==========================================
-- 1. Ejecutar este script en pgAdmin o psql
-- 2. Verificar que los nombres de turnos coincidan:
--    - TURNO_MANANA (6:00-14:00)
--    - TURNO_TARDE (14:00-22:00)
--    - TURNO_NOCHE (22:00-6:00)
-- 3. El script asume que ya existe:
--    - Usuario: olivia.obaldeon
--    - Rol: ROLE_GRIFERO
--    - Estación activa
--    - Al menos una isla activa
--    - Turnos configurados
-- 4. HOY debe ser miércoles 4 de diciembre de 2025
-- 5. Para probar en mobile:
--    - Username: olivia.obaldeon
--    - Password: password123 (o la que configuraste)
