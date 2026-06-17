BEGIN;
TRUNCATE public.prescriptions CASCADE;
TRUNCATE public.appointments CASCADE;
TRUNCATE public.schedules CASCADE;
TRUNCATE public.favorite_pharmacies CASCADE;
TRUNCATE public.patients CASCADE;
TRUNCATE public.profiles CASCADE;
DELETE FROM auth.users WHERE email LIKE '%@martclinic.com' OR email LIKE '%@patient.com';

-- Insert Doctor User
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('05219952-aa7d-4aea-9429-2d2c9c14c6d3', '00000000-0000-0000-0000-000000000000', 'doctor@martclinic.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "김의사", "role": "doctor"}', now(), now());

-- Insert Doctor Profile
INSERT INTO public.profiles (id, email, role)
VALUES ('05219952-aa7d-4aea-9429-2d2c9c14c6d3', 'doctor@martclinic.com', 'doctor');

-- Insert 19 Patient Users
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('04d90700-5ed0-45db-ab77-bde1661ceb6b', '00000000-0000-0000-0000-000000000000', 'patient1@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "김철수"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('d16386c8-936d-4143-a5bd-0788435a4a02', '00000000-0000-0000-0000-000000000000', 'patient2@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "이영희"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('4646066e-9a6b-43d6-8472-7d4561bfee34', '00000000-0000-0000-0000-000000000000', 'patient3@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "박민수"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('8f7e9e18-fcdd-4fae-a838-f653d1741f80', '00000000-0000-0000-0000-000000000000', 'patient4@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "정지원"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('3e59cbcc-1c97-477f-bc7c-c284900fdc06', '00000000-0000-0000-0000-000000000000', 'patient5@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "최유진"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('54dc8a03-fd25-46cc-aef2-a73e7dadcb17', '00000000-0000-0000-0000-000000000000', 'patient6@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "강동우"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('cc63462a-f2cb-44e5-9202-d18d6cc458df', '00000000-0000-0000-0000-000000000000', 'patient7@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "조현아"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('9d28df5a-8c20-4fe4-b492-ad7630516941', '00000000-0000-0000-0000-000000000000', 'patient8@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "윤서준"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('c5a73426-0282-4258-8534-06e54664dc58', '00000000-0000-0000-0000-000000000000', 'patient9@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "임채원"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('1cb39e4a-4b6a-4bd9-99bf-57d41baa9b33', '00000000-0000-0000-0000-000000000000', 'patient10@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "한태양"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('1c691e1d-ddc2-4f72-b1d6-fa4bd60777ca', '00000000-0000-0000-0000-000000000000', 'patient11@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "신도현"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('b474ebb7-d82c-479e-a5f5-e4a9e3a7618f', '00000000-0000-0000-0000-000000000000', 'patient12@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "송하윤"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('6b6c512d-a27e-441d-84da-b44ca74f08c9', '00000000-0000-0000-0000-000000000000', 'patient13@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "서우진"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('0ced4a03-f28b-4afc-aa2d-552c54282a54', '00000000-0000-0000-0000-000000000000', 'patient14@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "오민아"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('d7e50cf0-6d8f-4696-ac5d-0322f110c1fa', '00000000-0000-0000-0000-000000000000', 'patient15@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "황하준"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('6a97c0d3-b8b4-469c-8ed5-e69af8fa9732', '00000000-0000-0000-0000-000000000000', 'patient16@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "전예은"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('ff219862-4e57-466f-a6e5-7617e3cb6be7', '00000000-0000-0000-0000-000000000000', 'patient17@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "안성민"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('d34a55e8-6da3-403f-b08a-55cd005f12de', '00000000-0000-0000-0000-000000000000', 'patient18@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "양지아"}', now(), now());
INSERT INTO auth.users (id, instance_id, email, encrypted_password, email_confirmed_at, role, aud, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
VALUES ('9a99911f-0924-458f-8637-e219f456679e', '00000000-0000-0000-0000-000000000000', 'patient19@patient.com', '$2a$10$wKkH9Q4mCdfu8B57d7Q.4Oa7YwA17Zk.6m.XzV5q4gYd3wK6m6d8e', now(), 'authenticated', 'authenticated', '{"provider": "email", "providers": ["email"]}', '{"name": "배도윤"}', now(), now());

-- Insert 19 Profiles for Patients
INSERT INTO public.profiles (id, email, role) VALUES ('04d90700-5ed0-45db-ab77-bde1661ceb6b', 'patient1@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('d16386c8-936d-4143-a5bd-0788435a4a02', 'patient2@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('4646066e-9a6b-43d6-8472-7d4561bfee34', 'patient3@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('8f7e9e18-fcdd-4fae-a838-f653d1741f80', 'patient4@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('3e59cbcc-1c97-477f-bc7c-c284900fdc06', 'patient5@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('54dc8a03-fd25-46cc-aef2-a73e7dadcb17', 'patient6@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('cc63462a-f2cb-44e5-9202-d18d6cc458df', 'patient7@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('9d28df5a-8c20-4fe4-b492-ad7630516941', 'patient8@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('c5a73426-0282-4258-8534-06e54664dc58', 'patient9@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('1cb39e4a-4b6a-4bd9-99bf-57d41baa9b33', 'patient10@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('1c691e1d-ddc2-4f72-b1d6-fa4bd60777ca', 'patient11@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('b474ebb7-d82c-479e-a5f5-e4a9e3a7618f', 'patient12@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('6b6c512d-a27e-441d-84da-b44ca74f08c9', 'patient13@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('0ced4a03-f28b-4afc-aa2d-552c54282a54', 'patient14@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('d7e50cf0-6d8f-4696-ac5d-0322f110c1fa', 'patient15@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('6a97c0d3-b8b4-469c-8ed5-e69af8fa9732', 'patient16@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('ff219862-4e57-466f-a6e5-7617e3cb6be7', 'patient17@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('d34a55e8-6da3-403f-b08a-55cd005f12de', 'patient18@patient.com', 'patient');
INSERT INTO public.profiles (id, email, role) VALUES ('9a99911f-0924-458f-8637-e219f456679e', 'patient19@patient.com', 'patient');

-- Insert 19 Patient Profiles
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('d05c7760-1487-432b-a19a-1a6f814e0c5d', '04d90700-5ed0-45db-ab77-bde1661ceb6b', '김철수', '010-9548-6331', '2006401', 'HN-10001', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('80977ad6-f8df-4405-9dc0-db82aba271b6', 'd16386c8-936d-4143-a5bd-0788435a4a02', '이영희', '010-8712-9646', '7115388', 'HN-10002', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('e59501d5-72e2-464e-922e-d719f28bc126', '4646066e-9a6b-43d6-8472-7d4561bfee34', '박민수', '010-8788-5262', '3388414', 'HN-10003', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('4f6d4b58-249c-4c4c-9120-a9c67feb678a', '8f7e9e18-fcdd-4fae-a838-f653d1741f80', '정지원', '010-2828-6768', '3838093', 'HN-10004', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('142f2244-d062-4a04-8c63-6a8917492314', '3e59cbcc-1c97-477f-bc7c-c284900fdc06', '최유진', '010-1294-9130', '1810987', 'HN-10005', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('6d7c72c1-f370-46f4-8ca9-9c15fcad4423', '54dc8a03-fd25-46cc-aef2-a73e7dadcb17', '강동우', '010-9741-9239', '7249476', 'HN-10006', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('05528392-bd46-46ac-97b0-6b2e0f7b0083', 'cc63462a-f2cb-44e5-9202-d18d6cc458df', '조현아', '010-4858-4358', '8312431', 'HN-10007', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('83f21afa-a6f9-40be-97a5-9109e6d7e8ec', '9d28df5a-8c20-4fe4-b492-ad7630516941', '윤서준', '010-4230-7687', '2570138', 'HN-10008', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('c2f1458b-0928-40f0-8243-e35589d5216f', 'c5a73426-0282-4258-8534-06e54664dc58', '임채원', '010-6935-3677', '8474741', 'HN-10009', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', '1cb39e4a-4b6a-4bd9-99bf-57d41baa9b33', '한태양', '010-2945-3529', '4574893', 'HN-10010', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('772831a9-081b-4962-b260-01f739043174', '1c691e1d-ddc2-4f72-b1d6-fa4bd60777ca', '신도현', '010-1310-4724', '7017779', 'HN-10011', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('c63887f2-d788-4365-85ad-eda8aa9b3179', 'b474ebb7-d82c-479e-a5f5-e4a9e3a7618f', '송하윤', '010-4534-8092', '4471566', 'HN-10012', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('f90b2d87-5e20-4f7b-aa81-81c7535a84a5', '6b6c512d-a27e-441d-84da-b44ca74f08c9', '서우진', '010-6838-1995', '6646139', 'HN-10013', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('742e251b-f388-4c2d-adac-2c6dab71dd0a', '0ced4a03-f28b-4afc-aa2d-552c54282a54', '오민아', '010-2945-2256', '2889033', 'HN-10014', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('24f7c8b9-3b78-4477-a2d1-bd5bc107962e', 'd7e50cf0-6d8f-4696-ac5d-0322f110c1fa', '황하준', '010-4436-9430', '5970444', 'HN-10015', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('29203683-8180-48af-a3f8-839ffdb319fb', '6a97c0d3-b8b4-469c-8ed5-e69af8fa9732', '전예은', '010-6269-4259', '5272336', 'HN-10016', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('9aab15d3-741b-422e-a522-bd89b36980b4', 'ff219862-4e57-466f-a6e5-7617e3cb6be7', '안성민', '010-8537-4232', '3916127', 'HN-10017', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('e8e2df69-7e13-4ad8-923e-98cbfde9c7d4', 'd34a55e8-6da3-403f-b08a-55cd005f12de', '양지아', '010-6852-5955', '2727911', 'HN-10018', now());
INSERT INTO public.patients (id, user_id, name, phone, resident_last7, clinic_patient_number, created_at)
VALUES ('7d104836-00e9-4076-a3c3-260ebe3a9f48', '9a99911f-0924-458f-8637-e219f456679e', '배도윤', '010-9904-3866', '1179665', 'HN-10019', now());

-- Insert 12 Favorite Pharmacies
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('f06ef7cd-666f-4abc-b81c-aa31594b8a57', 'd05c7760-1487-432b-a19a-1a6f814e0c5d', '사랑약국', '서울특별시 중구 태평로1가 31 사랑약국', 37.5683418736389, 126.97863729341768, '02-119-1526', true, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('8908f2ba-4b7a-4434-a428-79dbe1054b01', '80977ad6-f8df-4405-9dc0-db82aba271b6', '행복한약국', '서울특별시 중구 태평로1가 31 행복한약국', 37.53191954055939, 126.97342756967613, '02-314-8118', true, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('a7c83277-68fb-4245-80b5-d2b691fd2225', 'e59501d5-72e2-464e-922e-d719f28bc126', '튼튼약국', '서울특별시 중구 태평로1가 31 튼튼약국', 37.61032375741937, 126.98263592735111, '02-516-7684', true, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('e4b14a50-7b0c-4770-ad03-1a766703d17c', '4f6d4b58-249c-4c4c-9120-a9c67feb678a', '정든약국', '서울특별시 중구 태평로1가 31 정든약국', 37.53934780735288, 127.02450058555023, '02-884-1970', true, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('5cc8525c-fa35-4207-b759-f4759e119df8', '142f2244-d062-4a04-8c63-6a8917492314', '푸른약국', '서울특별시 중구 태평로1가 31 푸른약국', 37.52069295242295, 126.97239003012302, '02-747-9055', true, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('5a560b89-6245-40d8-b140-9a36c955fe0a', '6d7c72c1-f370-46f4-8ca9-9c15fcad4423', '종로약국', '서울특별시 중구 태평로1가 31 종로약국', 37.59766232361711, 127.02673816587546, '02-543-6537', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('219ed643-a48b-4e4d-91c5-e22eb03e3d8a', '05528392-bd46-46ac-97b0-6b2e0f7b0083', '온누리약국', '서울특별시 중구 태평로1가 31 온누리약국', 37.578042985838735, 126.95032804939578, '02-458-2584', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('0cd09c6a-6e29-463b-a7f8-98811150d5d1', '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', '대학약국', '서울특별시 중구 태평로1가 31 대학약국', 37.591324504694256, 126.95011196956803, '02-244-9637', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('cc84f6e5-fa57-44e5-a47a-b59ab0eae694', 'c2f1458b-0928-40f0-8243-e35589d5216f', '메디칼약국', '서울특별시 중구 태평로1가 31 메디칼약국', 37.53400145953492, 127.00481279198321, '02-164-9261', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('e3e86e47-237c-46a4-a1bb-d74a8a5862a5', 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', '비타민약국', '서울특별시 중구 태평로1가 31 비타민약국', 37.52215490520536, 126.92869722249249, '02-628-3696', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('5808a49c-4dca-4942-b38c-3315e801ec02', '772831a9-081b-4962-b260-01f739043174', '중앙약국', '서울특별시 중구 태평로1가 31 중앙약국', 37.58179357858641, 127.02521183747594, '02-417-6846', false, now());
INSERT INTO public.favorite_pharmacies (id, patient_id, pharmacy_name, address, latitude, longitude, phone, is_default, created_at)
VALUES ('6ba87c07-741c-4005-abea-9e9cc6fabab5', 'c63887f2-d788-4365-85ad-eda8aa9b3179', '건강약국', '서울특별시 중구 태평로1가 31 건강약국', 37.604546821055116, 126.97253656732727, '02-838-6620', false, now());

-- Insert 50 Schedules and 50 Appointments
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('58e93b7b-d895-4ed6-b13e-526c96e6aa03', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '09:00:00', '09:30:00', false, '742e251b-f388-4c2d-adac-2c6dab71dd0a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('99f90961-227a-48ed-9ac0-6ed825fc4867', '742e251b-f388-4c2d-adac-2c6dab71dd0a', '58e93b7b-d895-4ed6-b13e-526c96e6aa03', 'cancelled', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-356-8737', 7500, 'imp_2190834049', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('89c980ff-e570-4424-89ff-934a191d0dd5', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '09:30:00', '10:00:00', false, '80977ad6-f8df-4405-9dc0-db82aba271b6', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('ad7232b6-2215-4adc-8bed-31d6d6a5eb2c', '80977ad6-f8df-4405-9dc0-db82aba271b6', '89c980ff-e570-4424-89ff-934a191d0dd5', 'pending', '눈이 충혈되고 가려우며 눈곱이 많이 낍니다.', 'https://meet.google.com/abc-260-1359', 5000, 'imp_4306135310', now() - interval '8 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('88d7db86-b463-42b8-aaf9-04f948af73b4', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '10:00:00', '10:30:00', false, 'c2f1458b-0928-40f0-8243-e35589d5216f', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('73f7387f-f499-44ca-bf3d-d4452ad2fb2a', 'c2f1458b-0928-40f0-8243-e35589d5216f', '88d7db86-b463-42b8-aaf9-04f948af73b4', 'confirmed', '소화가 안 되고 속이 쓰리며 명치 통증이 있습니다.', 'https://meet.google.com/abc-868-9369', 7500, 'imp_7055635583', now() - interval '0 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('a29b519b-8427-4780-8fa4-c57e39437937', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '10:30:00', '11:00:00', false, '29203683-8180-48af-a3f8-839ffdb319fb', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('cd46a700-c086-4ea9-9987-153556fddfcb', '29203683-8180-48af-a3f8-839ffdb319fb', 'a29b519b-8427-4780-8fa4-c57e39437937', 'pending', '어제 저녁부터 목이 붓고 침 삼키기가 힘들고 기침도 납니다.', 'https://meet.google.com/abc-245-6497', 5000, 'imp_7500805833', now() - interval '2 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('04835050-34af-486a-b6fb-65181c44d825', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '11:00:00', '11:30:00', false, 'f90b2d87-5e20-4f7b-aa81-81c7535a84a5', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('6e35b9d2-4da5-44c3-9dff-d5c2b386dfca', 'f90b2d87-5e20-4f7b-aa81-81c7535a84a5', '04835050-34af-486a-b6fb-65181c44d825', 'paid', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-246-5824', 5000, 'imp_7884888693', now() - interval '5 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('abeda496-7db7-43e9-bbdb-7a2a5ff6a8fe', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-12', '11:30:00', '12:00:00', false, '80977ad6-f8df-4405-9dc0-db82aba271b6', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('862ee97b-8569-48fd-81a0-b09bae2ec6af', '80977ad6-f8df-4405-9dc0-db82aba271b6', 'abeda496-7db7-43e9-bbdb-7a2a5ff6a8fe', 'confirmed', '피부 건조증이 심해지고 아토피 증상이 올라왔습니다.', 'https://meet.google.com/abc-975-6330', 15000, 'imp_4220810648', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('5a292456-020d-4cda-89c4-89c18b30163a', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '13:00:00', '13:30:00', false, '6d7c72c1-f370-46f4-8ca9-9c15fcad4423', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('f72c0d67-4e57-451e-aaa5-162be158a2b2', '6d7c72c1-f370-46f4-8ca9-9c15fcad4423', '5a292456-020d-4cda-89c4-89c18b30163a', 'confirmed', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-458-4553', 10000, 'imp_1555482374', now() - interval '9 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('e050a6b7-c231-46d1-bacb-b51717dde4e0', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '13:30:00', '14:00:00', false, '7d104836-00e9-4076-a3c3-260ebe3a9f48', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('16026b7c-9df3-44ca-be99-92aee36b34bc', '7d104836-00e9-4076-a3c3-260ebe3a9f48', 'e050a6b7-c231-46d1-bacb-b51717dde4e0', 'confirmed', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-848-2192', 7500, 'imp_8719152583', now() - interval '0 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('1b66ffed-eb4f-4673-8d38-8a9a58036112', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '14:00:00', '14:30:00', false, 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('aa9d4fdf-7ed6-4e9b-8b76-96efd455221a', 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', '1b66ffed-eb4f-4673-8d38-8a9a58036112', 'paid', '얼굴과 목 부위에 가려움증을 동반한 붉은 반점이 생겼습니다.', 'https://meet.google.com/abc-425-4385', 5000, 'imp_6221892932', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('8029125e-93df-4cbf-9c31-4e004c8d934a', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '14:30:00', '15:00:00', false, '742e251b-f388-4c2d-adac-2c6dab71dd0a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('8494dead-11a3-4985-9cfb-78d659e25c34', '742e251b-f388-4c2d-adac-2c6dab71dd0a', '8029125e-93df-4cbf-9c31-4e004c8d934a', 'completed', '어제 저녁부터 목이 붓고 침 삼키기가 힘들고 기침도 납니다.', 'https://meet.google.com/abc-231-5942', 15000, 'imp_3823062713', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('071aa1cc-df75-4fe7-90ba-95b808b9ccda', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '15:00:00', '15:30:00', false, 'e8e2df69-7e13-4ad8-923e-98cbfde9c7d4', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('81e9346a-bd40-43f8-b1d7-5b39e134942a', 'e8e2df69-7e13-4ad8-923e-98cbfde9c7d4', '071aa1cc-df75-4fe7-90ba-95b808b9ccda', 'cancelled', '눈이 충혈되고 가려우며 눈곱이 많이 낍니다.', 'https://meet.google.com/abc-143-6174', 15000, 'imp_1210908905', now() - interval '0 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('ae723543-611e-4037-80c3-892540b01ad1', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-13', '15:30:00', '16:00:00', false, 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('8108cec2-c470-440f-8cfd-fb6c21822d82', 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', 'ae723543-611e-4037-80c3-892540b01ad1', 'in_progress', '어제 저녁부터 목이 붓고 침 삼키기가 힘들고 기침도 납니다.', 'https://meet.google.com/abc-982-5736', 7500, 'imp_7305943561', now() - interval '0 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('b68757cf-0d9e-4cff-8daa-c404720214f6', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '16:00:00', '16:30:00', false, '742e251b-f388-4c2d-adac-2c6dab71dd0a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('ebe5de21-1210-4348-9e54-eccb1ab7116f', '742e251b-f388-4c2d-adac-2c6dab71dd0a', 'b68757cf-0d9e-4cff-8daa-c404720214f6', 'completed', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-365-5041', 5000, 'imp_4429029484', now() - interval '4 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('237801f0-d8dc-4821-85a8-9d7283d6aa2e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '16:30:00', '17:00:00', false, 'e59501d5-72e2-464e-922e-d719f28bc126', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('1808e29a-476c-472b-9c3a-175e8b06d0fc', 'e59501d5-72e2-464e-922e-d719f28bc126', '237801f0-d8dc-4821-85a8-9d7283d6aa2e', 'in_progress', '피부 건조증이 심해지고 아토피 증상이 올라왔습니다.', 'https://meet.google.com/abc-797-7275', 10000, 'imp_5050436358', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('451924ca-20be-41c5-a586-df51dcc93bd3', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '17:00:00', '17:30:00', false, 'c63887f2-d788-4365-85ad-eda8aa9b3179', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('0e603b67-9fa4-420f-888e-9dd676fa81d8', 'c63887f2-d788-4365-85ad-eda8aa9b3179', '451924ca-20be-41c5-a586-df51dcc93bd3', 'in_progress', '소화가 안 되고 속이 쓰리며 명치 통증이 있습니다.', 'https://meet.google.com/abc-920-8846', 10000, 'imp_1586604187', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('ea289ee1-a51f-4065-a7b2-fecb60a658fe', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '17:30:00', '18:00:00', false, '7d104836-00e9-4076-a3c3-260ebe3a9f48', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('044177a5-4209-45e6-b205-b3ed314c8e5a', '7d104836-00e9-4076-a3c3-260ebe3a9f48', 'ea289ee1-a51f-4065-a7b2-fecb60a658fe', 'cancelled', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-399-6180', 15000, 'imp_5745695470', now() - interval '5 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('eb30cff3-34b6-439f-bb58-34880dd21156', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '09:00:00', '09:30:00', false, '142f2244-d062-4a04-8c63-6a8917492314', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('3f87a6cf-43d3-44e8-a608-bac2913a5ecc', '142f2244-d062-4a04-8c63-6a8917492314', 'eb30cff3-34b6-439f-bb58-34880dd21156', 'in_progress', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-345-2518', 7500, 'imp_9577738477', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('e6472dfb-4b59-4d9d-8cd6-4c0b0d89fd30', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-14', '09:30:00', '10:00:00', false, '05528392-bd46-46ac-97b0-6b2e0f7b0083', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('e886acba-dbf3-433a-a92b-23a52b321caf', '05528392-bd46-46ac-97b0-6b2e0f7b0083', 'e6472dfb-4b59-4d9d-8cd6-4c0b0d89fd30', 'completed', '피부 건조증이 심해지고 아토피 증상이 올라왔습니다.', 'https://meet.google.com/abc-706-1233', 12000, 'imp_5859079418', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('ee8eaef1-d707-4b9e-af5f-508b43a689ee', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '10:00:00', '10:30:00', false, '4f6d4b58-249c-4c4c-9120-a9c67feb678a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('6053df81-fd53-445c-9c76-e479aa79754d', '4f6d4b58-249c-4c4c-9120-a9c67feb678a', 'ee8eaef1-d707-4b9e-af5f-508b43a689ee', 'confirmed', '귀 안쪽이 욱신거리고 아프며 약간의 이명이 들립니다.', 'https://meet.google.com/abc-216-8889', 15000, 'imp_1658544643', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('68d783d4-c872-4ab0-8ee3-866c4470021e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '10:30:00', '11:00:00', false, 'c2f1458b-0928-40f0-8243-e35589d5216f', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('7972503c-8326-4a44-9c30-cd89a71a1e3d', 'c2f1458b-0928-40f0-8243-e35589d5216f', '68d783d4-c872-4ab0-8ee3-866c4470021e', 'pending', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-150-3615', 7500, 'imp_8790818139', now() - interval '11 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('bc4e15e9-0439-44bd-8400-f420c66b44a9', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '11:00:00', '11:30:00', false, '742e251b-f388-4c2d-adac-2c6dab71dd0a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('2820d00a-b3f8-4396-8fa2-5aca73dfd6ca', '742e251b-f388-4c2d-adac-2c6dab71dd0a', 'bc4e15e9-0439-44bd-8400-f420c66b44a9', 'paid', '어제 저녁부터 목이 붓고 침 삼키기가 힘들고 기침도 납니다.', 'https://meet.google.com/abc-585-9863', 15000, 'imp_4030884659', now() - interval '7 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('89cc62d8-0b60-4f76-85d5-d7e3c9361de9', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '11:30:00', '12:00:00', false, '29203683-8180-48af-a3f8-839ffdb319fb', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('1f81d63f-42e6-4b88-b2c9-47178479ef72', '29203683-8180-48af-a3f8-839ffdb319fb', '89cc62d8-0b60-4f76-85d5-d7e3c9361de9', 'pending', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-967-6267', 15000, 'imp_3459702648', now() - interval '9 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('0253f50f-1016-4635-bb09-9a4cf82da23e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '13:00:00', '13:30:00', false, '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('1260f85f-3a53-4ef7-92f9-18a36ab8228c', '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', '0253f50f-1016-4635-bb09-9a4cf82da23e', 'cancelled', '귀 안쪽이 욱신거리고 아프며 약간의 이명이 들립니다.', 'https://meet.google.com/abc-840-2515', 7500, 'imp_2190746647', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('d60ee8ed-e95b-4cdf-8d2f-26505f35bc94', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-15', '13:30:00', '14:00:00', false, '772831a9-081b-4962-b260-01f739043174', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('5c145dd8-64f8-4108-89d3-3b86ba89e6a1', '772831a9-081b-4962-b260-01f739043174', 'd60ee8ed-e95b-4cdf-8d2f-26505f35bc94', 'confirmed', '피부 건조증이 심해지고 아토피 증상이 올라왔습니다.', 'https://meet.google.com/abc-902-7838', 10000, 'imp_8070944975', now() - interval '8 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('180a6e96-cf29-4032-a641-f18b793d53bc', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '14:00:00', '14:30:00', false, '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('b9def367-56e5-4fb3-be91-aea168f032ab', '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', '180a6e96-cf29-4032-a641-f18b793d53bc', 'in_progress', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-972-6624', 15000, 'imp_2249081415', now() - interval '4 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('97259a08-9a75-4b93-aa66-f11093398285', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '14:30:00', '15:00:00', false, 'c63887f2-d788-4365-85ad-eda8aa9b3179', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('16228967-f9aa-4b00-811a-436629d97c9d', 'c63887f2-d788-4365-85ad-eda8aa9b3179', '97259a08-9a75-4b93-aa66-f11093398285', 'confirmed', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-466-2537', 10000, 'imp_4314505343', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('09b09d84-98b5-40e7-8c0f-0ef2636ab512', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '15:00:00', '15:30:00', false, '772831a9-081b-4962-b260-01f739043174', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('c0c32a01-b36b-4218-b831-5322e72c31ae', '772831a9-081b-4962-b260-01f739043174', '09b09d84-98b5-40e7-8c0f-0ef2636ab512', 'confirmed', '얼굴과 목 부위에 가려움증을 동반한 붉은 반점이 생겼습니다.', 'https://meet.google.com/abc-553-2444', 12000, 'imp_7401027708', now() - interval '4 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('d39dcbc9-3741-4ecb-80e1-bc3de3b602ab', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '15:30:00', '16:00:00', false, '742e251b-f388-4c2d-adac-2c6dab71dd0a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('fe40930e-2631-4470-82a4-32215625fc59', '742e251b-f388-4c2d-adac-2c6dab71dd0a', 'd39dcbc9-3741-4ecb-80e1-bc3de3b602ab', 'completed', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-681-1143', 15000, 'imp_7950362246', now() - interval '8 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('2e2ed3c2-0b8e-4998-9d83-52663e5cbb0b', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '16:00:00', '16:30:00', false, '7d104836-00e9-4076-a3c3-260ebe3a9f48', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('c39a3da9-d2ce-4322-b82e-c90468263d60', '7d104836-00e9-4076-a3c3-260ebe3a9f48', '2e2ed3c2-0b8e-4998-9d83-52663e5cbb0b', 'completed', '콧물이 많이 나고 재채기가 끊이지 않습니다. 비염인 것 같습니다.', 'https://meet.google.com/abc-334-8100', 12000, 'imp_9696830486', now() - interval '7 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('1abb8a46-64d8-4206-8f01-acbaaa720286', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-16', '16:30:00', '17:00:00', false, 'c63887f2-d788-4365-85ad-eda8aa9b3179', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('aa0e3abf-98b0-481b-b1f8-ecb200e1ebb2', 'c63887f2-d788-4365-85ad-eda8aa9b3179', '1abb8a46-64d8-4206-8f01-acbaaa720286', 'in_progress', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-389-4806', 15000, 'imp_3729008196', now() - interval '8 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('880b9ff0-ef89-4bfd-a4d4-1bee23852d0f', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '17:00:00', '17:30:00', false, 'f90b2d87-5e20-4f7b-aa81-81c7535a84a5', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('19a7c61b-506c-4583-94e9-e8af5c215444', 'f90b2d87-5e20-4f7b-aa81-81c7535a84a5', '880b9ff0-ef89-4bfd-a4d4-1bee23852d0f', 'paid', '귀 안쪽이 욱신거리고 아프며 약간의 이명이 들립니다.', 'https://meet.google.com/abc-295-5720', 10000, 'imp_1386021265', now() - interval '11 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('bb51d234-177f-41c3-900c-31d0067eeba3', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '17:30:00', '18:00:00', false, '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('b7242efc-162f-447a-b7d7-89440be94e20', '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', 'bb51d234-177f-41c3-900c-31d0067eeba3', 'pending', '귀 안쪽이 욱신거리고 아프며 약간의 이명이 들립니다.', 'https://meet.google.com/abc-580-6555', 7500, 'imp_5011132939', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('105beaac-a954-48a8-9f35-3b6561a02ebf', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '09:00:00', '09:30:00', false, 'c2f1458b-0928-40f0-8243-e35589d5216f', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('96359847-d2ae-483c-8fe8-458d666dee83', 'c2f1458b-0928-40f0-8243-e35589d5216f', '105beaac-a954-48a8-9f35-3b6561a02ebf', 'paid', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-889-2736', 15000, 'imp_2926594125', now() - interval '5 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('4f027603-765a-404d-b17d-dd1d31e3d404', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '09:30:00', '10:00:00', false, '142f2244-d062-4a04-8c63-6a8917492314', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('82a81613-2cd0-4d69-a5d4-8b84367fb421', '142f2244-d062-4a04-8c63-6a8917492314', '4f027603-765a-404d-b17d-dd1d31e3d404', 'cancelled', '콧물이 많이 나고 재채기가 끊이지 않습니다. 비염인 것 같습니다.', 'https://meet.google.com/abc-420-7655', 5000, 'imp_9488558863', now() - interval '9 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('ef746026-6814-4d85-9600-91027539f84e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '10:00:00', '10:30:00', false, '4f6d4b58-249c-4c4c-9120-a9c67feb678a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('34ab3916-f815-4332-bf42-5b41a5f9246f', '4f6d4b58-249c-4c4c-9120-a9c67feb678a', 'ef746026-6814-4d85-9600-91027539f84e', 'cancelled', '눈이 충혈되고 가려우며 눈곱이 많이 낍니다.', 'https://meet.google.com/abc-143-1815', 12000, 'imp_5289240367', now() - interval '9 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('6cfeb83f-0d5b-4169-bcb5-fad37fd271ce', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-17', '10:30:00', '11:00:00', false, '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('6e54b385-e983-4619-a310-12bee42ebe92', '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', '6cfeb83f-0d5b-4169-bcb5-fad37fd271ce', 'paid', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-348-8791', 15000, 'imp_9019848749', now() - interval '5 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('14b19b7a-9a98-4ac6-8d17-44b793c3d4c2', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '11:00:00', '11:30:00', false, '142f2244-d062-4a04-8c63-6a8917492314', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('008f37d5-6024-4582-bde9-610ec9ece55a', '142f2244-d062-4a04-8c63-6a8917492314', '14b19b7a-9a98-4ac6-8d17-44b793c3d4c2', 'pending', '눈이 충혈되고 가려우며 눈곱이 많이 낍니다.', 'https://meet.google.com/abc-288-2442', 5000, 'imp_7951196785', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('451ad6e6-903c-402a-abc7-b145c3bacf8e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '11:30:00', '12:00:00', false, 'c63887f2-d788-4365-85ad-eda8aa9b3179', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('8458a195-fd01-4f6e-af75-47e1e3b56486', 'c63887f2-d788-4365-85ad-eda8aa9b3179', '451ad6e6-903c-402a-abc7-b145c3bacf8e', 'cancelled', '콧물이 많이 나고 재채기가 끊이지 않습니다. 비염인 것 같습니다.', 'https://meet.google.com/abc-616-2487', 10000, 'imp_6333956919', now() - interval '2 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('053821ba-8b45-4996-80f3-6e33de882391', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '13:00:00', '13:30:00', false, '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('bd5161eb-91ba-4712-994c-5108c90a9c79', '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', '053821ba-8b45-4996-80f3-6e33de882391', 'completed', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-406-8956', 5000, 'imp_6159712369', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('b47de731-a2f2-494e-bf88-37bd896213d4', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '13:30:00', '14:00:00', false, 'e59501d5-72e2-464e-922e-d719f28bc126', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('a5124b63-85a9-4107-96ce-61fb907ff9b9', 'e59501d5-72e2-464e-922e-d719f28bc126', 'b47de731-a2f2-494e-bf88-37bd896213d4', 'completed', '소화가 안 되고 속이 쓰리며 명치 통증이 있습니다.', 'https://meet.google.com/abc-202-5835', 15000, 'imp_3306592609', now() - interval '3 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('c65d9a7f-6014-4514-b447-f66626d7a76d', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '14:00:00', '14:30:00', false, '80977ad6-f8df-4405-9dc0-db82aba271b6', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('00ac5391-ccf6-4afa-b63b-9e457e4becd6', '80977ad6-f8df-4405-9dc0-db82aba271b6', 'c65d9a7f-6014-4514-b447-f66626d7a76d', 'confirmed', '콧물이 많이 나고 재채기가 끊이지 않습니다. 비염인 것 같습니다.', 'https://meet.google.com/abc-754-1218', 12000, 'imp_1181270932', now() - interval '10 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('6d73574d-7209-4cf9-bbe3-76b092c2ee3e', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-18', '14:30:00', '15:00:00', false, 'e8e2df69-7e13-4ad8-923e-98cbfde9c7d4', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('1f20b0fd-6eb7-4606-98ff-a29272ffabf7', 'e8e2df69-7e13-4ad8-923e-98cbfde9c7d4', '6d73574d-7209-4cf9-bbe3-76b092c2ee3e', 'completed', '콧물이 많이 나고 재채기가 끊이지 않습니다. 비염인 것 같습니다.', 'https://meet.google.com/abc-231-3200', 7500, 'imp_4731666272', now() - interval '11 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('d7653992-6534-4bd8-8b63-49fde77f3a34', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '15:00:00', '15:30:00', false, '4f6d4b58-249c-4c4c-9120-a9c67feb678a', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('6b1fc16f-b2ee-49bd-8ab6-3f1a9b61e608', '4f6d4b58-249c-4c4c-9120-a9c67feb678a', 'd7653992-6534-4bd8-8b63-49fde77f3a34', 'pending', '피부 건조증이 심해지고 아토피 증상이 올라왔습니다.', 'https://meet.google.com/abc-328-9291', 5000, 'imp_4368163071', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('5fbd98e4-c2f6-44ef-8fc1-5f15e0b61db5', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '15:30:00', '16:00:00', false, '29203683-8180-48af-a3f8-839ffdb319fb', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('f9c5135a-039c-4da6-879e-ec32ed7f4a85', '29203683-8180-48af-a3f8-839ffdb319fb', '5fbd98e4-c2f6-44ef-8fc1-5f15e0b61db5', 'pending', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-276-1618', 15000, 'imp_2717699464', now() - interval '2 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('0f423a4d-8a30-4a99-ab1e-59c0e3ef7088', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '16:00:00', '16:30:00', false, '29203683-8180-48af-a3f8-839ffdb319fb', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('a4d609d2-472a-4fe9-9e30-79f870cea0d3', '29203683-8180-48af-a3f8-839ffdb319fb', '0f423a4d-8a30-4a99-ab1e-59c0e3ef7088', 'in_progress', '눈이 충혈되고 가려우며 눈곱이 많이 낍니다.', 'https://meet.google.com/abc-252-3789', 10000, 'imp_8246994984', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('ef8dc5dc-7e47-4275-9371-53cb74e54786', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '16:30:00', '17:00:00', false, 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('58d0e8ad-193a-44a6-9626-8e652fcde373', 'cf14cd3e-874c-4ace-9eb2-cb99ed8a254b', 'ef8dc5dc-7e47-4275-9371-53cb74e54786', 'confirmed', '가벼운 감기 증상과 함께 가래가 끓습니다.', 'https://meet.google.com/abc-912-8572', 7500, 'imp_2518690029', now() - interval '1 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('c9c2d217-ff70-4390-80f8-fbf5b9030461', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '17:00:00', '17:30:00', false, '29203683-8180-48af-a3f8-839ffdb319fb', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('7f72dba2-1a6d-43ee-841f-0c9ce9f0876e', '29203683-8180-48af-a3f8-839ffdb319fb', 'c9c2d217-ff70-4390-80f8-fbf5b9030461', 'cancelled', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-235-5627', 15000, 'imp_7299817934', now() - interval '7 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('bb6e0d85-7d35-4ef2-8c45-d089e05e824d', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-19', '17:30:00', '18:00:00', false, '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('6148facc-747f-4b3c-998d-cb2230d573e2', '24f7c8b9-3b78-4477-a2d1-bd5bc107962e', 'bb6e0d85-7d35-4ef2-8c45-d089e05e824d', 'confirmed', '갑작스러운 두통과 소화불량 증세가 있습니다.', 'https://meet.google.com/abc-747-3676', 10000, 'imp_4813607323', now() - interval '8 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('66e624d4-d957-4951-a6df-89a339193b9f', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-20', '09:00:00', '09:30:00', false, '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('a5c9b1c9-c0cc-4812-bcde-da907b152d2c', '83f21afa-a6f9-40be-97a5-9109e6d7e8ec', '66e624d4-d957-4951-a6df-89a339193b9f', 'completed', '어제 저녁부터 목이 붓고 침 삼키기가 힘들고 기침도 납니다.', 'https://meet.google.com/abc-171-8566', 7500, 'imp_1271098994', now() - interval '2 hour');
INSERT INTO public.schedules (id, doctor_id, date, start_time, end_time, is_available, booked_by, created_at)
VALUES ('d3dfd3f9-69e2-47d6-8791-c8a0ccde3a1a', '05219952-aa7d-4aea-9429-2d2c9c14c6d3', '2026-06-20', '09:30:00', '10:00:00', false, 'c2f1458b-0928-40f0-8243-e35589d5216f', now());
INSERT INTO public.appointments (id, patient_id, schedule_id, status, symptoms, meet_link, payment_amount, payment_id, created_at)
VALUES ('ffa2a9b8-61c3-4fef-abef-5d3de0744310', 'c2f1458b-0928-40f0-8243-e35589d5216f', 'd3dfd3f9-69e2-47d6-8791-c8a0ccde3a1a', 'completed', '어지러움과 발열이 심하고 오한이 듭니다.', 'https://meet.google.com/abc-144-6583', 5000, 'imp_1260671626', now() - interval '9 hour');
COMMIT;