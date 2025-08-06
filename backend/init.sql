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

INSERT INTO users (id, username, password, email, role)
VALUES (1, 'admin', 'password', 'hospital@gmail.com', 'ADMIN'),
       (2, 'dr_smith', 'password', 'dr.smith@hospital.com', 'DOCTOR'),
       (3, 'patient_john', 'password', 'john.doe@example.com', 'PATIENT'),
       (4, 'dr_jones', 'password', 'dr.jones@hospital.com', 'DOCTOR'),
       (5, 'patient_mary', 'password', 'mary.j@example.com', 'PATIENT');

INSERT INTO hospitals (id, name, phone, user_id)
VALUES (1, 'Hospital', '+1234567890', 1);

INSERT INTO departments (id, name, description)
VALUES (1, 'Cardiology', 'Heart and cardiovascular diseases'),
       (2, 'Dermatology', 'Skin conditions treatment'),
       (3, 'Neurology', 'Nervous system disorders');

INSERT INTO doctors (id, user_id, department_id, hospital_id, full_name, phone)
VALUES (1, 2, 1, 1, 'Dr. John Smith', '+1234567890'),
       (2, 4, 2, 1, 'Dr. Emily Jones', '+1987654321');

INSERT INTO patients (id, user_id, full_name, phone)
VALUES (1, 3, 'John Doe', '+1555123456'),
       (2, 5, 'Mary Johnson', '+1555987654');

INSERT INTO appointments (id, slot_id, patient_id, description, status)
VALUES (1, 1, 1, 'Annual heart checkup', 'BOOKED'),
       (2, 4, 2, 'Skin allergy consultation', 'BOOKED');
