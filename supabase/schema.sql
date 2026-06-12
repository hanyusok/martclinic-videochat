-- 1. Patients
create table patients (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users not null unique,
  name text,
  phone text unique,
  resident_last7 text, -- 암호화 권장
  clinic_patient_number text,
  created_at timestamp with time zone default now()
);

-- 2. Doctor Schedules
create table schedules (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid, -- 나중에 확장
  date date not null,
  start_time time not null,
  end_time time not null,
  is_available boolean default true,
  booked_by uuid references patients(id),
  created_at timestamp with time zone default now()
);

-- 3. Appointments (비대면 진료)
create table appointments (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references patients(id),
  schedule_id uuid references schedules(id),
  status text check (status in ('pending', 'paid', 'confirmed', 'in_progress', 'completed', 'cancelled')) default 'pending',
  symptoms text,
  symptom_images text[], -- storage url
  meet_link text,
  payment_amount integer,
  payment_id text,
  created_at timestamp with time zone default now()
);

-- 4. Favorite Pharmacies
create table favorite_pharmacies (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references patients(id),
  pharmacy_name text,
  address text,
  latitude double precision,
  longitude double precision,
  phone text,
  is_default boolean default false,
  created_at timestamp with time zone default now()
);

-- 5. Prescriptions
create table prescriptions (
  id uuid primary key default gen_random_uuid(),
  appointment_id uuid references appointments(id),
  doctor_notes text,
  pdf_url text, -- Supabase Storage
  sent_pharmacy_id uuid references favorite_pharmacies(id),
  sent_at timestamp with time zone,
  created_at timestamp with time zone default now()
);

-- RLS (Row Level Security) 설정
alter table patients enable row level security;
alter table schedules enable row level security;
alter table appointments enable row level security;
alter table favorite_pharmacies enable row level security;
alter table prescriptions enable row level security;

-- Patients Policy: 환자는 자신의 정보만 볼 수 있음
create policy "Patients can view their own data" on patients
  for select to authenticated
  using (auth.uid() = user_id);

create policy "Patients can update their own data" on patients
  for update to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

-- Schedules Policy: 모든 인증된 사용자는 스케줄 조회 가능
create policy "Schedules are viewable by authenticated users" on schedules
  for select to authenticated
  using (true);

-- Appointments Policy: 환자는 자신의 예약만 조회/생성 가능
create policy "Patients can view their own appointments" on appointments
  for select to authenticated
  using (patient_id in (select id from patients where user_id = auth.uid()));

create policy "Patients can insert their own appointments" on appointments
  for insert to authenticated
  with check (patient_id in (select id from patients where user_id = auth.uid()));

-- Favorite Pharmacies Policy
create policy "Patients can manage their favorite pharmacies" on favorite_pharmacies
  for all to authenticated
  using (patient_id in (select id from patients where user_id = auth.uid()));

-- Prescriptions Policy
create policy "Patients can view their own prescriptions" on prescriptions
  for select to authenticated
  using (appointment_id in (
    select id from appointments where patient_id in (
      select id from patients where user_id = auth.uid()
    )
  ));

create policy "Patients can update their own prescriptions" on prescriptions
  for update to authenticated
  using (appointment_id in (
    select id from appointments where patient_id in (
      select id from patients where user_id = auth.uid()
    )
  ))
  with check (appointment_id in (
    select id from appointments where patient_id in (
      select id from patients where user_id = auth.uid()
    )
  ));

