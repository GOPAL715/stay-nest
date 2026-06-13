-- liquibase formatted sql

-- changeset staynest:V012-seed-super-admin
-- Default SUPER_ADMIN: admin@staynest.com / Admin@123!
-- BCrypt hash of "Admin@123!" with strength 12
-- NOTE: DataInitializer.java corrects this hash at runtime using the real PasswordEncoder.
INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    role, status, email_verified,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'admin@staynest.com',
    '$2a$12$TKuDp4JBqSJmzqBpq0IkYeL.z7hb3VfOHCXq4R9z0/gCKtv6.NDUS',
    'Super',
    'Admin',
    'SUPER_ADMIN',
    'ACTIVE',
    TRUE,
    now(),
    now()
) ON CONFLICT (email) DO NOTHING;

-- changeset staynest:V012-seed-platform-config
INSERT INTO platform_config (id, config_key, config_value, description, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'service_fee_percent',      '10',  'Platform service fee percentage charged on each booking',          now(), now()),
    (gen_random_uuid(), 'payout_delay_days',         '7',   'Number of days after check-out before host payout is processed',  now(), now()),
    (gen_random_uuid(), 'tax_rate_default',           '18',  'Default GST tax rate percentage applied to bookings',             now(), now()),
    (gen_random_uuid(), 'max_booking_duration_days',  '30',  'Maximum number of nights allowed per booking',                   now(), now()),
    (gen_random_uuid(), 'min_booking_advance_hours',  '24',  'Minimum hours in advance a booking must be made',                now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- changeset staynest:V012-seed-amenities
INSERT INTO amenities (id, name, icon, category, created_at, updated_at) VALUES
    (gen_random_uuid(), 'WiFi',             'wifi',            'Connectivity',  now(), now()),
    (gen_random_uuid(), 'Swimming Pool',    'pool',            'Recreation',    now(), now()),
    (gen_random_uuid(), 'Kitchen',          'kitchen',         'Essentials',    now(), now()),
    (gen_random_uuid(), 'Free Parking',     'parking',         'Essentials',    now(), now()),
    (gen_random_uuid(), 'Air Conditioning', 'ac',              'Climate',       now(), now()),
    (gen_random_uuid(), 'Heating',          'heating',         'Climate',       now(), now()),
    (gen_random_uuid(), 'Washer',           'washer',          'Essentials',    now(), now()),
    (gen_random_uuid(), 'Dryer',            'dryer',           'Essentials',    now(), now()),
    (gen_random_uuid(), 'TV',               'tv',              'Entertainment', now(), now()),
    (gen_random_uuid(), 'Iron',             'iron',            'Essentials',    now(), now()),
    (gen_random_uuid(), 'Hair Dryer',       'hair-dryer',      'Essentials',    now(), now()),
    (gen_random_uuid(), 'Workspace',        'workspace',       'Work',          now(), now()),
    (gen_random_uuid(), 'Gym',              'gym',             'Recreation',    now(), now()),
    (gen_random_uuid(), 'Hot Tub',          'hot-tub',         'Recreation',    now(), now()),
    (gen_random_uuid(), 'BBQ Grill',        'bbq',             'Recreation',    now(), now()),
    (gen_random_uuid(), 'Fireplace',        'fireplace',       'Climate',       now(), now()),
    (gen_random_uuid(), 'EV Charger',       'ev-charger',      'Transport',     now(), now()),
    (gen_random_uuid(), 'Smoke Alarm',      'smoke-alarm',     'Safety',        now(), now()),
    (gen_random_uuid(), 'First Aid Kit',    'first-aid',       'Safety',        now(), now()),
    (gen_random_uuid(), 'Fire Extinguisher','fire-extinguisher','Safety',       now(), now())
ON CONFLICT (name) DO NOTHING;
