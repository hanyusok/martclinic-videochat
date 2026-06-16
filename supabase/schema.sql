-- 1. Patients
create table patients (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users not null,
  name text,
  phone text,
  resident_number text, -- 주민번호 (암호화 권장)
  relationship text default '본인',
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

-- 4. Master Pharmacies (외부 API 및 CSV에서 가져온 약국 목록)
create table pharmacies (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  address text,
  latitude double precision,
  longitude double precision,
  phone text,
  hpid text unique, -- 기관 식별 코드 (Upsert용)
  location geography(POINT, 4326), -- PostGIS 공간 데이터
  created_at timestamp with time zone default now(),
  constraint pharmacies_name_address_key unique (name, address)
);

-- 공간 인덱스 생성 (검색 성능 최적화)
create index pharmacies_location_idx on pharmacies using gist(location);

-- 주변 약국 검색용 RPC 함수
create or replace function get_nearby_pharmacies(
  user_lat double precision,
  user_lon double precision,
  radius_meters double precision default 5000
)
returns table (
  id uuid,
  name text,
  address text,
  latitude double precision,
  longitude double precision,
  phone text,
  hpid text,
  distance double precision
)
language sql
as $$
  select
    id, name, address, latitude, longitude, phone, hpid,
    st_distance(location, st_point(user_lon, user_lat)::geography) as distance
  from pharmacies
  where st_dwithin(location, st_point(user_lon, user_lat)::geography, radius_meters)
  order by distance;
$$;

-- 5. Favorite Pharmacies
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

-- 6. Prescriptions
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
alter table pharmacies enable row level security;
alter table favorite_pharmacies enable row level security;
alter table prescriptions enable row level security;

-- Patients Policy: 환자는 자신의 정보만 볼 수 있음
create policy "Patients can view their own data" on patients
  for select to authenticated
  using (auth.uid() = user_id);

create policy "Patients can insert their own data" on patients
  for insert to authenticated
  with check (auth.uid() = user_id);

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

-- Pharmacies Policy: 모든 인증된 사용자는 약국 목록 조회/동기화 가능
create policy "Pharmacies are viewable by authenticated users" on pharmacies
  for select to authenticated
  using (true);

create policy "Pharmacies are insertable by authenticated users" on pharmacies
  for insert to authenticated
  with check (true);

create policy "Pharmacies are updatable by authenticated users" on pharmacies
  for update to authenticated
  using (true)
  with check (true);

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
