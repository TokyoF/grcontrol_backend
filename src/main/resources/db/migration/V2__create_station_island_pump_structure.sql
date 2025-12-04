-- =========================================================================
-- Migration V2: Station, Island, Pump Structure & Scheduling System
-- =========================================================================
-- This migration creates the normalized structure for managing multiple
-- service stations with configurable islands, pumps, nozzles, and worker
-- scheduling system.
-- =========================================================================

-- =========================================================================
-- PART 1: STATIONS TABLE
-- =========================================================================
CREATE TABLE stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address TEXT,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stations_active ON stations(is_active);

-- =========================================================================
-- PART 2: STATION ADMINISTRATORS (Many-to-Many with Users)
-- =========================================================================
CREATE TABLE station_administrators (
    station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (station_id, user_id)
);

CREATE INDEX idx_station_admins_station ON station_administrators(station_id);
CREATE INDEX idx_station_admins_user ON station_administrators(user_id);

-- =========================================================================
-- PART 3: ISLANDS TABLE
-- =========================================================================
CREATE TABLE islands (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, OFFLINE, MAINTENANCE
    position INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_station_island_name UNIQUE (station_id, name)
);

CREATE INDEX idx_islands_station ON islands(station_id);
CREATE INDEX idx_islands_status ON islands(status);

-- =========================================================================
-- PART 4: PUMPS TABLE
-- =========================================================================
CREATE TABLE pumps (
    id BIGSERIAL PRIMARY KEY,
    island_id BIGINT NOT NULL REFERENCES islands(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    position INTEGER,
    brand VARCHAR(50),
    model VARCHAR(50),
    serial_number VARCHAR(100),
    installation_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pumps_island ON pumps(island_id);
CREATE INDEX idx_pumps_active ON pumps(is_active);

-- =========================================================================
-- PART 5: NOZZLES TABLE
-- =========================================================================
CREATE TABLE nozzles (
    id BIGSERIAL PRIMARY KEY,
    pump_id BIGINT NOT NULL REFERENCES pumps(id) ON DELETE CASCADE,
    side VARCHAR(10) NOT NULL, -- LEFT, RIGHT
    position INTEGER NOT NULL,
    fuel_type VARCHAR(20) NOT NULL, -- REGULAR, PREMIUM, DIESEL, GLP
    price_per_gallon NUMERIC(10, 3),
    color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_nozzles_pump ON nozzles(pump_id);
CREATE INDEX idx_nozzles_active ON nozzles(is_active);
CREATE INDEX idx_nozzles_fuel_type ON nozzles(fuel_type);

-- =========================================================================
-- PART 6: SHIFT SCHEDULES TABLE
-- =========================================================================
CREATE TABLE shift_schedules (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    display_label VARCHAR(20), -- e.g., "6-14", "14-22", "22-6"
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_overnight BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_station_schedule_name UNIQUE (station_id, name)
);

CREATE INDEX idx_shift_schedules_station ON shift_schedules(station_id);
CREATE INDEX idx_shift_schedules_active ON shift_schedules(is_active);

-- =========================================================================
-- PART 7: WORKER ASSIGNMENTS TABLE
-- =========================================================================
CREATE TABLE worker_assignments (
    id BIGSERIAL PRIMARY KEY,
    worker_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    island_id BIGINT NOT NULL REFERENCES islands(id) ON DELETE CASCADE,
    shift_schedule_id BIGINT NOT NULL REFERENCES shift_schedules(id) ON DELETE CASCADE,
    week_start_date DATE NOT NULL, -- Monday of the week
    day_of_week VARCHAR(10) NOT NULL, -- MONDAY, TUESDAY, etc.
    is_rest_day BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, CANCELLED, REPLACED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_worker_assignments_worker ON worker_assignments(worker_id);
CREATE INDEX idx_worker_assignments_island ON worker_assignments(island_id);
CREATE INDEX idx_worker_assignments_schedule ON worker_assignments(shift_schedule_id);
CREATE INDEX idx_worker_week ON worker_assignments(worker_id, week_start_date);
CREATE INDEX idx_island_schedule ON worker_assignments(island_id, shift_schedule_id, day_of_week);
CREATE INDEX idx_assignments_status ON worker_assignments(status);

-- =========================================================================
-- PART 8: PUMP CONFIGURATION HISTORY TABLE
-- =========================================================================
CREATE TABLE pump_configuration_history (
    id BIGSERIAL PRIMARY KEY,
    nozzle_id BIGINT NOT NULL REFERENCES nozzles(id) ON DELETE CASCADE,
    change_type VARCHAR(30) NOT NULL, -- PRICE_CHANGE, ACTIVATION, DEACTIVATION, FUEL_TYPE_CHANGE
    previous_value VARCHAR(255),
    new_value VARCHAR(255),
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    affected_session_id BIGINT REFERENCES shift_sessions(id) ON DELETE SET NULL,
    reason TEXT,
    notes TEXT
);

CREATE INDEX idx_config_history_nozzle ON pump_configuration_history(nozzle_id);
CREATE INDEX idx_config_history_timestamp ON pump_configuration_history(change_timestamp);
CREATE INDEX idx_config_history_changed_by ON pump_configuration_history(changed_by_id);
CREATE INDEX idx_config_history_session ON pump_configuration_history(affected_session_id);

-- =========================================================================
-- PART 9: UPDATE SHIFT_SESSIONS TABLE
-- =========================================================================
-- Add new columns for normalized relationships
ALTER TABLE shift_sessions
    ADD COLUMN station_id BIGINT REFERENCES stations(id) ON DELETE SET NULL,
    ADD COLUMN shift_schedule_id BIGINT REFERENCES shift_schedules(id) ON DELETE SET NULL,
    ADD COLUMN assignment_id BIGINT REFERENCES worker_assignments(id) ON DELETE SET NULL,
    ADD COLUMN configuration_snapshot TEXT; -- JSON snapshot of pump config at shift start

-- Add indexes for performance
CREATE INDEX idx_shift_sessions_station ON shift_sessions(station_id);
CREATE INDEX idx_shift_sessions_schedule ON shift_sessions(shift_schedule_id);
CREATE INDEX idx_shift_sessions_assignment ON shift_sessions(assignment_id);

-- Add comments for deprecated fields (keep for backward compatibility)
COMMENT ON COLUMN shift_sessions.station_id_old IS 'DEPRECATED: Use station_id FK instead';
COMMENT ON COLUMN shift_sessions.station_name IS 'DEPRECATED: Use station relation instead';
COMMENT ON COLUMN shift_sessions.shift_time IS 'DEPRECATED: Use shift_schedule_id FK instead';

-- =========================================================================
-- PART 10: UPDATE PUMP_READINGS TABLE
-- =========================================================================
-- Add new columns for normalized relationships
ALTER TABLE pump_readings
    ADD COLUMN nozzle_id BIGINT REFERENCES nozzles(id) ON DELETE CASCADE,
    ADD COLUMN base_reading_id BIGINT REFERENCES pump_readings(id) ON DELETE SET NULL;

-- Add indexes for performance
CREATE INDEX idx_pump_readings_nozzle ON pump_readings(nozzle_id);
CREATE INDEX idx_pump_readings_base ON pump_readings(base_reading_id);
CREATE INDEX idx_session_nozzle ON pump_readings(session_id, nozzle_id, reading_type);

-- Add comments for deprecated fields
COMMENT ON COLUMN pump_readings.island_id IS 'DEPRECATED: Use nozzle_id FK to get island via pump';
COMMENT ON COLUMN pump_readings.pump_id IS 'DEPRECATED: Use nozzle_id FK to get pump';
COMMENT ON COLUMN pump_readings.nozzle_index IS 'DEPRECATED: Use nozzle_id FK instead of array index';

-- =========================================================================
-- PART 11: INSERT INITIAL DATA - 4 SERVICE STATIONS
-- =========================================================================
INSERT INTO stations (name, address, is_active) VALUES
    ('Acobamba', 'Av. Principal Acobamba, Huancavelica', TRUE),
    ('Tarma', 'Jr. Lima 123, Tarma, Junín', TRUE),
    ('Paccha', 'Carretera Central Km 15, Paccha, Junín', TRUE),
    ('Santa Ana', 'Av. Santa Ana 456, Huancayo, Junín', TRUE);

-- =========================================================================
-- PART 12: INSERT DEFAULT SHIFT SCHEDULES (Same for all stations)
-- =========================================================================
-- Get station IDs for insertions
DO $$
DECLARE
    station_rec RECORD;
BEGIN
    FOR station_rec IN SELECT id FROM stations LOOP
        INSERT INTO shift_schedules (station_id, name, display_label, start_time, end_time, is_overnight, is_active)
        VALUES
            (station_rec.id, 'Mañana', '6-14', '06:00:00', '14:00:00', FALSE, TRUE),
            (station_rec.id, 'Tarde', '14-22', '14:00:00', '22:00:00', FALSE, TRUE),
            (station_rec.id, 'Noche', '22-6', '22:00:00', '06:00:00', TRUE, TRUE);
    END LOOP;
END $$;

-- =========================================================================
-- MIGRATION COMPLETE
-- =========================================================================
-- Next steps:
-- 1. Populate islands for each station
-- 2. Configure pumps and nozzles for each island
-- 3. Assign administrators to stations
-- 4. Create worker assignments for weekly schedules
-- =========================================================================
