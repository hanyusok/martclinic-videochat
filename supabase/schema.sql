-- 0. User Roles and Profiles
create type user_role as enum ('admin', 'doctor', 'patient');

create table profiles (
  id uuid primary key references auth.users on delete cascade,
  email text,
  role user_role not null default 'patient',
  is_profile_completed boolean default false,
  updated_at timestamp with time zone default now()
);

-- Enable RLS on profiles
alter table profiles enable row level security;

-- Profiles Policies
create policy "Public profiles are viewable by everyone" on profiles
  for select using (true);

create policy "Users can update their own profiles" on profiles
  for update to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

-- Trigger to create profile on signup
create or replace function public.handle_new_user()
returns trigger as $$
declare
  user_role_val public.user_role;
begin
  user_role_val := coalesce(new.raw_user_meta_data->>'role', 'patient')::public.user_role;

  insert into public.profiles (id, email, role, is_profile_completed)
  values (
    new.id,
    new.email,
    user_role_val,
    false
  );

  -- Automatically create a patient record for new patients so they can proceed
  if user_role_val = 'patient'::public.user_role then
    insert into public.patients (user_id)
    values (new.id);
  end if;

  return new;
end;
$$ language plpgsql security definer set search_path = public;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

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

-- 3. Appointments (비대면 진료)
create table appointments (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references patients(id),
  status text check (status in ('pending', 'paid', 'confirmed', 'in_progress', 'completed', 'cancelled', 'payment_pending', 'waiting', 'calling')) default 'pending',
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
  fax text,
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
  fax text,
  hpid text,
  distance double precision
)
language sql
as $$
  select
    id, name, address, latitude, longitude, phone, fax, hpid,
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
  fax text,
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
alter table appointments enable row level security;
alter table pharmacies enable row level security;
alter table favorite_pharmacies enable row level security;
alter table prescriptions enable row level security;

-- Patients Policy: 환자는 자신의 정보만 볼 수 있고, 관리자는 모두 관리 가능
create policy "Patients can view their own data" on patients
  for select to authenticated
  using (
    auth.uid() = user_id
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

create policy "Patients can insert their own data" on patients
  for insert to authenticated
  with check (
    auth.uid() = user_id
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

create policy "Patients can update their own data" on patients
  for update to authenticated
  using (
    auth.uid() = user_id
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  )
  with check (
    auth.uid() = user_id
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

create policy "Admins can delete patient profiles" on patients
  for delete to authenticated
  using (
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

-- Appointments Policy: 환자는 자신의 예약만 조회/생성 가능, 관리자는 모두 가능
create policy "Patients and admins can view appointments" on appointments
  for select to authenticated
  using (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

create policy "Patients and admins can insert appointments" on appointments
  for insert to authenticated
  with check (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

create policy "Patients and admins can update appointments" on appointments
  for update to authenticated
  using (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  )
  with check (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

-- Pharmacies Policy: 모든 사용자는 약국 목록 조회 가능 (Guest 대응)
create policy "Pharmacies are viewable by everyone" on pharmacies
  for select using (true);

create policy "Pharmacies are insertable by authenticated users" on pharmacies
  for insert to authenticated
  with check (true);

create policy "Pharmacies are updatable by authenticated users" on pharmacies
  for update to authenticated
  using (true)
  with check (true);

-- Favorite Pharmacies Policy: 환자는 자신의 단골 약국만 관리, 관리자는 모두 관리
create policy "Patients and admins can manage favorite pharmacies" on favorite_pharmacies
  for all to authenticated
  using (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );

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

-- 7. Payments
create table payments (
  id uuid primary key default gen_random_uuid(),
  appointment_id uuid references appointments(id),
  patient_id uuid references patients(id),
  transaction_id text,
  amount integer,
  pay_method text,
  status text,
  created_at timestamp with time zone default now()
);

alter table payments enable row level security;

-- Payments Policy: 환자는 자신의 결제 내역만 관리, 관리자는 모두 관리
create policy "Patients and admins can manage payments" on payments
  for all to authenticated
  using (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  )
  with check (
    patient_id in (select id from patients where user_id = auth.uid())
    or
    exists (select 1 from profiles where id = auth.uid() and role = 'admin')
  );
