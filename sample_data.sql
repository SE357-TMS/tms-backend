-- ========================================
-- SQL Script: Thêm dữ liệu mẫu cho TMS (Tour Management System)
-- ========================================
-- Lưu ý: UUID được chuyển đổi sang BINARY(16) để phù hợp với JPA UUID type
-- Sử dụng UNHEX(REPLACE(UUID(), '-', '')) để convert UUID string thành BINARY(16)

-- Xóa dữ liệu cũ trước khi thêm mới (để tránh lỗi duplicate)
DELETE FROM invoice;
DELETE FROM favorite_tour;
DELETE FROM tour_booking;
DELETE FROM trip;
DELETE FROM route_attraction;
DELETE FROM route;
DELETE FROM attraction;
DELETE FROM category;
DELETE FROM user WHERE username IN ('admin', 'staff01', 'staff02', 'customer01', 'customer02', 'customer03', 'customer04');

-- 1. Thêm Users (Admin, Staff, Customers)
INSERT INTO user (id, username, user_password, is_lock, full_name, email, phone_number, address, birthday, gender, role, created_at, updated_at, deleted_at, version) VALUES
-- Admin
(UNHEX(REPLACE(UUID(), '-', '')), 'admin', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Nguyễn Văn Admin', 'admin@tms.com', '0901234567', '123 Nguyễn Huệ, Q1, TP.HCM', '1985-01-15', 'M', 'ADMIN', NOW(), NOW(), 0, 0),

-- Staff
(UNHEX(REPLACE(UUID(), '-', '')), 'staff01', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Trần Thị Hồng', 'hong.tran@tms.com', '0912345678', '456 Lê Lợi, Q1, TP.HCM', '1990-05-20', 'F', 'STAFF', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'staff02', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Lê Văn Minh', 'minh.le@tms.com', '0923456789', '789 Trần Hưng Đạo, Q5, TP.HCM', '1992-08-10', 'M', 'STAFF', NOW(), NOW(), 0, 0),

-- Customers
(UNHEX(REPLACE(UUID(), '-', '')), 'customer01', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Phạm Thị Lan', 'lan.pham@gmail.com', '0934567890', '321 Hai Bà Trưng, Q3, TP.HCM', '1995-03-12', 'F', 'CUSTOMER', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'customer02', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Hoàng Văn Tuấn', 'tuan.hoang@gmail.com', '0945678901', '654 Võ Văn Tần, Q3, TP.HCM', '1993-07-25', 'M', 'CUSTOMER', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'customer03', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Đỗ Thị Mai', 'mai.do@gmail.com', '0956789012', '987 Nguyễn Thị Minh Khai, Q1, TP.HCM', '1997-11-30', 'F', 'CUSTOMER', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'customer04', '$2a$12$zuriztglmw7FGNOgsgnP7uGDbR4ksnA2aUMEa6oEgTTdZRKMyD7bK', 0, 'Vũ Minh Khoa', 'khoa.vu@gmail.com', '0967890123', '147 Pasteur, Q1, TP.HCM', '1994-02-18', 'M', 'CUSTOMER', NOW(), NOW(), 0, 0);

-- 2. Thêm Categories (Loại điểm tham quan)
INSERT INTO category (id, name, status, created_at, updated_at, deleted_at, version) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'Di tích lịch sử', 'ACTIVE', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Thiên nhiên', 'ACTIVE', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Biển đảo', 'ACTIVE', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Văn hóa tâm linh', 'ACTIVE', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Vui chơi giải trí', 'ACTIVE', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Ẩm thực', 'ACTIVE', NOW(), NOW(), 0, 0);

-- 3. Thêm Attractions (Điểm tham quan)
INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Dinh Độc Lập',
    'Dinh Độc Lập là công trình kiến trúc lịch sử nổi tiếng tại TP.HCM, từng là nơi làm việc của Tổng thống Việt Nam Cộng hòa.',
    'TP. Hồ Chí Minh',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Di tích lịch sử';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Vịnh Hạ Long',
    'Di sản thiên nhiên thế giới với hàng nghìn hòn đảo đá vôi tuyệt đẹp.',
    'Quảng Ninh',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Thiên nhiên';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Bãi biển Nha Trang',
    'Một trong những bãi biển đẹp nhất Việt Nam với làn nước trong xanh và bờ cát trắng mịn.',
    'Nha Trang',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Biển đảo';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Chùa Một Cột',
    'Chùa Một Cột là một di tích lịch sử văn hóa nổi tiếng tại Hà Nội.',
    'Hà Nội',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Văn hóa tâm linh';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Vinpearl Land',
    'Khu vui chơi giải trí hiện đại với nhiều trò chơi hấp dẫn.',
    'Nha Trang',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Vui chơi giải trí';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Phố cổ Hội An',
    'Phố cổ Hội An với kiến trúc cổ kính và ẩm thực đặc sắc.',
    'Hội An',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Di tích lịch sử';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Chợ Bến Thành',
    'Chợ truyền thống nổi tiếng với đa dạng món ăn đường phố và đặc sản địa phương.',
    'TP. Hồ Chí Minh',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Ẩm thực';

INSERT INTO attraction (id, name, description, location, category_id, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    'Đảo Phú Quốc',
    'Đảo ngọc Phú Quốc với bãi biển tuyệt đẹp và hệ sinh thái phong phú.',
    'Phú Quốc',
    id,
    'ACTIVE',
    NOW(),
    NOW(),
    0,
    0
FROM category WHERE name = 'Biển đảo';

-- 4. Thêm Routes (Tuyến du lịch)
INSERT INTO route (id, route_name, start_location, end_location, duration_days, image, status, created_at, updated_at, deleted_at, version) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'Tour Sài Gòn - Vũng Tàu', 'TP. Hồ Chí Minh', 'Vũng Tàu', 2, 'vungtau_tour.jpg', 'OPEN', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Tour Hà Nội - Hạ Long', 'Hà Nội', 'Quảng Ninh', 3, 'halong_tour.jpg', 'OPEN', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Tour Nha Trang - Đà Lạt', 'Nha Trang', 'Đà Lạt', 4, 'nhatrang_dalat.jpg', 'OPEN', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Tour Đà Nẵng - Hội An', 'Đà Nẵng', 'Hội An', 3, 'danang_hoian.jpg', 'OPEN', NOW(), NOW(), 0, 0),
(UNHEX(REPLACE(UUID(), '-', '')), 'Tour Phú Quốc', 'TP. Hồ Chí Minh', 'Phú Quốc', 4, 'phuquoc_tour.jpg', 'OPEN', NOW(), NOW(), 0, 0);

-- 5. Thêm Route-Attraction (Liên kết giữa tuyến và điểm tham quan)
-- Sử dụng cột day và order_in_day thay vì visit_order
INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    1,
    'Tham quan Chùa Một Cột',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Hà Nội - Hạ Long' AND a.name = 'Chùa Một Cột';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    2,
    1,
    'Khám phá Vịnh Hạ Long',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Hà Nội - Hạ Long' AND a.name = 'Vịnh Hạ Long';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    1,
    'Tắm biển Nha Trang',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Nha Trang - Đà Lạt' AND a.name = 'Bãi biển Nha Trang';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    2,
    'Vui chơi tại Vinpearl Land',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Nha Trang - Đà Lạt' AND a.name = 'Vinpearl Land';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    1,
    'Tham quan phố cổ Hội An',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Đà Nẵng - Hội An' AND a.name = 'Phố cổ Hội An';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    1,
    'Tham quan Dinh Độc Lập',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Sài Gòn - Vũng Tàu' AND a.name = 'Dinh Độc Lập';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    2,
    'Mua sắm tại Chợ Bến Thành',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Sài Gòn - Vũng Tàu' AND a.name = 'Chợ Bến Thành';

INSERT INTO route_attraction (id, route_id, attraction_id, day, order_in_day, activity_description, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    r.id,
    a.id,
    1,
    1,
    'Khám phá đảo Phú Quốc',
    NOW(),
    NOW(),
    0,
    0
FROM route r, attraction a
WHERE r.route_name = 'Tour Phú Quốc' AND a.name = 'Đảo Phú Quốc';

-- 6. Thêm Trips (Chuyến đi cụ thể)
INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2025-01-15',
    '2025-01-17',
    4500000.00,
    40,
    15,
    '07:00:00',
    'Công viên 23/9, Q1, TP.HCM',
    'SCHEDULED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Sài Gòn - Vũng Tàu';

INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2025-02-10',
    '2025-02-13',
    8500000.00,
    35,
    20,
    '06:00:00',
    'Sân bay Nội Bài, Hà Nội',
    'SCHEDULED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Hà Nội - Hạ Long';

INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2025-03-05',
    '2025-03-09',
    9500000.00,
    30,
    12,
    '08:00:00',
    'Sân bay Cam Ranh, Nha Trang',
    'SCHEDULED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Nha Trang - Đà Lạt';

INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2025-04-20',
    '2025-04-23',
    7500000.00,
    40,
    25,
    '07:30:00',
    'Sân bay Đà Nẵng',
    'SCHEDULED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Đà Nẵng - Hội An';

INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2025-05-10',
    '2025-05-14',
    12000000.00,
    25,
    18,
    '09:00:00',
    'Sân bay Tân Sơn Nhất, TP.HCM',
    'SCHEDULED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Phú Quốc';

-- Thêm trip đã hoàn thành (cho lịch sử)
INSERT INTO trip (id, route_id, departure_date, return_date, price, total_seats, booked_seats, pick_up_time, pick_up_location, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    id,
    '2024-12-01',
    '2024-12-03',
    4200000.00,
    40,
    35,
    '07:00:00',
    'Công viên 23/9, Q1, TP.HCM',
    'FINISHED',
    NOW(),
    NOW(),
    0,
    0
FROM route WHERE route_name = 'Tour Sài Gòn - Vũng Tàu';

-- 7. Thêm Tour Bookings (Đặt tour)
-- Booking cho trip Sài Gòn - Vũng Tàu (CONFIRMED)
INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    2,
    9000000.00,
    'CONFIRMED',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2025-01-15' AND u.username = 'customer01';

INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    3,
    13500000.00,
    'CONFIRMED',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2025-01-15' AND u.username = 'customer02';

-- Booking cho trip Hà Nội - Hạ Long (PENDING)
INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    4,
    34000000.00,
    'PENDING',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2025-02-10' AND u.username = 'customer03';

-- Booking cho trip Nha Trang - Đà Lạt (CONFIRMED)
INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    2,
    19000000.00,
    'CONFIRMED',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2025-03-05' AND u.username = 'customer04';

-- Booking cho trip Phú Quốc (CONFIRMED)
INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    2,
    24000000.00,
    'CONFIRMED',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2025-05-10' AND u.username = 'customer01';

-- Booking đã hoàn thành (trip finished)
INSERT INTO tour_booking (id, trip_id, user_id, seats_booked, total_price, status, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    t.id,
    u.id,
    2,
    8400000.00,
    'COMPLETED',
    NOW(),
    NOW(),
    0,
    0
FROM trip t, user u
WHERE t.departure_date = '2024-12-01' AND u.username = 'customer02';

-- 8. Thêm Invoices (Hóa đơn)
-- Invoice cho booking đã PAID
INSERT INTO invoice (id, booking_id, total_amount, payment_status, payment_method, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    tb.id,
    tb.total_price,
    'PAID',
    'Chuyển khoản ngân hàng',
    NOW(),
    NOW(),
    0,
    0
FROM tour_booking tb
JOIN user u ON tb.user_id = u.id
JOIN trip t ON tb.trip_id = t.id
WHERE u.username = 'customer01' AND t.departure_date = '2025-01-15';

INSERT INTO invoice (id, booking_id, total_amount, payment_status, payment_method, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    tb.id,
    tb.total_price,
    'PAID',
    'Ví điện tử MoMo',
    NOW(),
    NOW(),
    0,
    0
FROM tour_booking tb
JOIN user u ON tb.user_id = u.id
JOIN trip t ON tb.trip_id = t.id
WHERE u.username = 'customer02' AND t.departure_date = '2025-01-15';

-- Invoice cho booking PENDING (chưa thanh toán)
INSERT INTO invoice (id, booking_id, total_amount, payment_status, payment_method, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    tb.id,
    tb.total_price,
    'UNPAID',
    NULL,
    NOW(),
    NOW(),
    0,
    0
FROM tour_booking tb
JOIN user u ON tb.user_id = u.id
JOIN trip t ON tb.trip_id = t.id
WHERE u.username = 'customer03' AND t.departure_date = '2025-02-10';

-- Invoice cho booking COMPLETED
INSERT INTO invoice (id, booking_id, total_amount, payment_status, payment_method, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    tb.id,
    tb.total_price,
    'PAID',
    'Thẻ tín dụng',
    NOW(),
    NOW(),
    0,
    0
FROM tour_booking tb
JOIN user u ON tb.user_id = u.id
JOIN trip t ON tb.trip_id = t.id
WHERE u.username = 'customer02' AND t.departure_date = '2024-12-01';

INSERT INTO invoice (id, booking_id, total_amount, payment_status, payment_method, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    tb.id,
    tb.total_price,
    'PAID',
    'Chuyển khoản ngân hàng',
    NOW(),
    NOW(),
    0,
    0
FROM tour_booking tb
JOIN user u ON tb.user_id = u.id
JOIN trip t ON tb.trip_id = t.id
WHERE u.username = 'customer04' AND t.departure_date = '2025-03-05';

-- 9. Thêm Favorite Tours (Tour yêu thích)
-- Sử dụng route_id thay vì trip_id (vì FavoriteTour liên kết với Route, không phải Trip)
INSERT INTO favorite_tour (id, user_id, route_id, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.id,
    r.id,
    NOW(),
    NOW(),
    0,
    0
FROM user u, route r
WHERE u.username = 'customer01' AND r.route_name = 'Tour Hà Nội - Hạ Long';

INSERT INTO favorite_tour (id, user_id, route_id, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.id,
    r.id,
    NOW(),
    NOW(),
    0,
    0
FROM user u, route r
WHERE u.username = 'customer03' AND r.route_name = 'Tour Đà Nẵng - Hội An';

INSERT INTO favorite_tour (id, user_id, route_id, created_at, updated_at, deleted_at, version)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.id,
    r.id,
    NOW(),
    NOW(),
    0,
    0
FROM user u, route r
WHERE u.username = 'customer04' AND r.route_name = 'Tour Phú Quốc';

-- ========================================
-- KẾT THÚC SCRIPT
-- ========================================
-- Lưu ý: 
-- - Password mặc định cho tất cả user: "password123" (đã được mã hóa BCrypt)
-- - UUID được convert sang BINARY(16) bằng UNHEX(REPLACE(UUID(), '-', ''))
-- - Dữ liệu có tính logic: bookings phù hợp với số ghế, invoices khớp với bookings, etc.
-- - Các quan hệ khóa ngoại được đảm bảo tính nhất quán
-- 
-- Các sửa đổi so với bản gốc:
-- 1. Thêm DELETE statements ở đầu để xóa dữ liệu cũ (tránh lỗi duplicate)
-- 2. Đổi visit_order thành day + order_in_day trong route_attraction
-- 3. Đổi trip_id thành route_id trong favorite_tour
