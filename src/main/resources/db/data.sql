insert into dual (dummy) values ('X');
-- 카테고리 초기화
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (1, 'BAG', '백', 'バッグ', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (2, 'JEWELRY', '귀금속', '貴金属', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (3, 'WATCH', '시계', '時計', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (4, 'ACCESSORY', '악세사리', '小物・アクセサリー', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (5, 'CLOTHES', '의류', '衣類', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (6, 'HOBBY', '취미', 'お楽しみ', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (7, 'DISHES', '식기', '食器', 'NONE', CURRENT_TIMESTAMP);
INSERT INTO category(id, name, korean, japanese, status, create_date) VALUES (8, 'ART', '미술품', '絵画・美術品', 'NONE', CURRENT_TIMESTAMP);

-- CONFIG
INSERT INTO config(id, config_group, config_key, config_value) VALUES (1, 'EXECUTOR', 'STATUS', 'NONE');

INSERT INTO config(id, config_group, config_key, config_value) VALUES (2, 'CONFIG', 'TITLE', 'Yonga :: auc application');
INSERT INTO config(id, config_group, config_key, config_value) VALUES (3, 'CONFIG', 'WELCOME', 'Welcome yonga mall');
