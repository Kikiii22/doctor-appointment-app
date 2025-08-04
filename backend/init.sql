CREATE TABLE departments
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100)       NOT NULL,
    role     VARCHAR(20)        NOT NULL CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN'))
);

CREATE TABLE doctors
(
    id            SERIAL PRIMARY KEY,
    user_id       INTEGER REFERENCES users (id),
    department_id INTEGER REFERENCES departments (id),
    full_name TEXT,
    email         TEXT,
    phone         TEXT
);

CREATE TABLE patients
(
    id       SERIAL PRIMARY KEY,
    user_id  INTEGER REFERENCES users (id),
    full_name TEXT,
    phone    TEXT,
    email    TEXT
);

CREATE TABLE slots
(
    id         SERIAL PRIMARY KEY,
    doctor_id  INTEGER REFERENCES doctors (id),
    start_time TIMESTAMP NOT NULL,
    booked  BOOLEAN DEFAULT FALSE,
    date DATE
);

CREATE TABLE appointments
(
    id         SERIAL PRIMARY KEY,
    slot_id    INTEGER REFERENCES slots (id),
    patient_id INTEGER REFERENCES patients (id),
    description      TEXT,
    status     VARCHAR(20) DEFAULT 'AVAILABLE'
);

INSERT INTO departments (name, description)
VALUES ('Cardiology', 'Heart and cardiovascular diseases'),
       ('Dermatology', 'Skin conditions treatment'),
       ('Neurology', 'Nervous system disorders');

INSERT INTO users (username, password, role)
VALUES ('admin', 'password', 'ADMIN'),
       ('dr_smith', 'password', 'DOCTOR'),
       ('patient_john', 'password', 'PATIENT'),
       ('dr_jones', 'password', 'DOCTOR'),
       ('patient_mary', 'password', 'PATIENT');

INSERT INTO doctors (user_id, department_id, full_name, email, phone)
VALUES (2, 1, 'Dr. John Smith', 'dr.smith@hospital.com', '+1234567890'),
       (4, 2, 'Dr. Emily Jones', 'dr.jones@hospital.com', '+1987654321');

INSERT INTO patients (user_id, full_name, phone, email)
VALUES (3, 'John Doe', '+1555123456', 'john.doe@example.com'),
       (5, 'Mary Johnson', '+1555987654', 'mary.j@example.com');

INSERT INTO slots (doctor_id, start_time, booked, date)
VALUES (1, '2023-12-01 09:00:00', FALSE, '2023-12-01'),
       (1, '2023-12-01 09:15:00', FALSE, '2023-12-01'),
       (1, '2023-12-01 09:30:00', FALSE, '2023-12-01'),
       (2, '2023-12-02 10:00:00', FALSE, '2023-12-02'),
       (2, '2023-12-02 10:15:00', FALSE, '2023-12-02');

INSERT INTO appointments (slot_id, patient_id, description, status)
VALUES (1, 1, 'Annual heart checkup', 'BOOKED'),
       (4, 2, 'Skin allergy consultation', 'BOOKED');