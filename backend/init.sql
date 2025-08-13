CREATE TABLE departments
(
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    description TEXT
);

CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100)       NOT NULL,
    email    TEXT               NOT NULL,
    role     VARCHAR(20)        NOT NULL CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN'))
);

CREATE TABLE hospitals
(
    id      SERIAL PRIMARY KEY,
    name    TEXT NOT NULL,
    phone   TEXT,
    user_id INTEGER REFERENCES users (id)
);

CREATE TABLE doctors
(
    id            SERIAL PRIMARY KEY,
    user_id       INTEGER REFERENCES users (id),
    department_id INTEGER REFERENCES departments (id),
    hospital_id   INTEGER REFERENCES hospitals (id),
    full_name     TEXT,
    phone         TEXT
);

CREATE TABLE patients
(
    id        SERIAL PRIMARY KEY,
    user_id   INTEGER REFERENCES users (id),
    full_name TEXT,
    phone     TEXT
);

CREATE TABLE slots
(
    id         SERIAL PRIMARY KEY,
    doctor_id  INTEGER REFERENCES doctors (id),
    start_time TIMESTAMP NOT NULL,
    booked     BOOLEAN DEFAULT FALSE,
    date       DATE
);

CREATE TABLE schedule
(
    id          SERIAL PRIMARY KEY,
    doctor_id   INTEGER REFERENCES doctors (id),
    day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN
                                            ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY',
                                             'SUNDAY')),
    is_working  BOOLEAN DEFAULT FALSE,
    start_time  TIME,
    end_time    TIME,
    break_start TIME,
    break_end   TIME
);

CREATE TABLE appointments
(
    id          SERIAL PRIMARY KEY,
    slot_id     INTEGER REFERENCES slots (id),
    patient_id  INTEGER REFERENCES patients (id),
    description TEXT,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'BOOKED', 'FINISHED'))
);
