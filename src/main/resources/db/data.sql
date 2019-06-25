-- 카테고리 초기화
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (1, 'Bag', '백', 'バッグ', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (2, 'Watch', '시계', '時計', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (3, 'Precious metal', '귀금속', '貴金属', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (4, 'Clothing', '의류', '衣類', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (5, 'Accessories', '악세사리', '小物・アクセサリー', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (6, 'Tableware', '식기', '食器', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (7, 'Variety', '취미', 'お楽しみ', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (8, 'Painting･Art', '미술품', '絵画・美術品', 'NONE', 0, CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, kaisai_kaisu, create_date) VALUES (9, 'Coin', '코인', 'コイン・古銭', 'NONE', 0, CURRENT_TIMESTAMP);


-- CONFIG
INSERT INTO config(id, config_group, config_key, config_value) VALUES (1, 'EXECUTOR', 'STATUS', 'NONE');

INSERT INTO config(id, config_group, config_key, config_value) VALUES (2, 'CONFIG', 'TITLE', 'Yonga :: auc application');
INSERT INTO config(id, config_group, config_key, config_value) VALUES (3, 'CONFIG', 'WELCOME', 'Welcome yonga mall');
INSERT INTO config(id, config_group, config_key, config_value) VALUES (4, 'CONFIG', 'ADMIN_EMAIL', 'yaglobal@naver.com');
INSERT INTO config(id, config_group, config_key, config_value) VALUES (5, 'AUCTION', 'INFO', '{}');


INSERT INTO CUSTOMER(USER_ID, PASSWORD, NAME, TEL, EMAIL, DESCRIPTION, PRIVILEGE, ENABLED, DISPLAY) VALUES (
'admin',
'$2a$10$kCEWR8eamrvWB9yXX/WVT.C1rmhS2rRlugWr1MzhM4RR5KhSGM0xe',
'관리자',
'010-0000-0000',
'yaglobal@naver.com',
'관리자 입니다.',
'ROLE_USER,ROLE_ADMIN',
true,
true
)
;
INSERT INTO CUSTOMER(USER_ID, PASSWORD, NAME, TEL, EMAIL, DESCRIPTION, PRIVILEGE, ENABLED, DISPLAY) VALUES (
'star16m',
'$2a$10$kCEWR8eamrvWB9yXX/WVT.C1rmhS2rRlugWr1MzhM4RR5KhSGM0xe',
'관리자',
'010-0000-0000',
'star16m@gmail.com',
'관리자 입니다.',
'ROLE_USER,ROLE_ADMIN',
true,
false
)
;
INSERT INTO CUSTOMER(USER_ID, PASSWORD, NAME, TEL, EMAIL, DESCRIPTION, PRIVILEGE, ENABLED, DISPLAY) VALUES (
'user',
'$2a$10$lYeqaESfY9mbTQZqYy4qYuPni/7Y7dTEhoWMAGbd6smHg0Mi3JCGm',
'일반사용자',
'010-0000-0000',
'yaglobal@naver.com',
'일반 사용자 입니다.',
'ROLE_USER',
true,
true
)
;