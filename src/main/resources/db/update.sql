--drop table if exists config;
--
--CREATE TABLE CONFIG (
--    id             INTEGER IDENTITY PRIMARY KEY,
--    config_group   VARCHAR(255),
--    config_key     VARCHAR(255),
--    config_value   VARCHAR(800)
--);
--
--
---- CONFIG
--INSERT INTO config(id, config_group, config_key, config_value) VALUES (1, 'EXECUTOR', 'STATUS', 'NONE');
--
--INSERT INTO config(id, config_group, config_key, config_value) VALUES (2, 'CONFIG', 'TITLE', 'Yonga :: auc application');
--INSERT INTO config(id, config_group, config_key, config_value) VALUES (3, 'CONFIG', 'WELCOME', 'Welcome yonga mall');
--INSERT INTO config(id, config_group, config_key, config_value) VALUES (4, 'CONFIG', 'ADMIN_EMAIL', 'yaglobal@naver.com');
----INSERT INTO config(id, config_group, config_key, config_value) VALUES (5, 'AUCTION', 'INFO', '{}');
--
--
---- alter table category add kaisai_kaisu integer;
--update category set kaisai_kaisu = 0;

update category set kaisai_kaisu = 522
where ;