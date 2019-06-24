DROP TABLE dual IF EXISTS;
DROP TABLE product_image IF EXISTS;
DROP TABLE maker IF EXISTS;
DROP TABLE brand IF EXISTS;
DROP TABLE keijo IF EXISTS;
DROP TABLE product IF EXISTS;
DROP TABLE category IF EXISTS;
DROP TABLE work_log IF EXISTS;
DROP TABLE config IF EXISTS;
DROP TABLE customer if EXISTS;

-- dual
CREATE TABLE dual (
	dummy varchar(1)
);

-- 카테고리
CREATE TABLE category (
    id INTEGER IDENTITY PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    korean VARCHAR(80) NOT NULL,
    japanese VARCHAR(80) NOT NULL,
    status VARCHAR(80) NOT NULL,
    total_product_num INTEGER,
    ext_product_num INTEGER,
    kaisai_kaisu INTEGER,
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);

-- 메이커
CREATE TABLE maker (
    maker_cd INTEGER IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_no INTEGER NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    name_kr VARCHAR(255) NULL,
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);
-- 브랜드
CREATE TABLE brand (
    brand_cd INTEGER IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_no INTEGER NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    name_kr VARCHAR(255) NULL,
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);
-- 형상
CREATE TABLE keijo (
    keijo_cd INTEGER IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_no INTEGER NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    name_kr VARCHAR(255) NULL,
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);

-- 제품
CREATE TABLE product (
    uketsuke_bng VARCHAR(80) PRIMARY KEY,
    genre_cd INTEGER NOT NULL,
    kaijo_cd INTEGER DEFAULT 0 NOT NULL, -- 몰 인 경우 0
    kaisai_kaisu INTEGER NULL, -- 몰 인 경우 null
    seri_bng INTEGER NULL, -- 몰 인 경우 null
    maker_cd INTEGER NOT NULL,
    brand_type_cd INTEGER NOT NULL,
    keijo_cd INTEGER NOT NULL,
    -- start of detail
    seizo_bng VARCHAR(255) NULL,
    channel_kbn VARCHAR(255) NULL,
    shinpin_kbn INTEGER NULL,
    start_kng INTEGER NULL,
    kibo_kng INTEGER NULL,
    kekka_kbn INTEGER NULL,
    kekka VARCHAR(255) NULL,
    kekka_kng INTEGER NULL,
    shohin VARCHAR(255) NULL,
    shuppin_biko2 VARCHAR(255) NULL,
    kata VARCHAR(255) NULL,
    biko VARCHAR(255) NULL,
    hyoka VARCHAR(255) NULL,
    hyoka_gaiso VARCHAR(255) NULL,
    hyoka_naiso VARCHAR(255) NULL,
    inv_toroku_bng INTEGER NULL,
    inventory_no VARCHAR(255) NULL,
    kaisai_ymd VARCHAR(255) NULL,
    kaisai_jkn TIMESTAMP NULL,
    entry_date TIMESTAMP NULL,
    update_date TIMESTAMP NULL,
    -- end of detail
    extract_result VARCHAR(30) NOT NULL,
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);

-- 제품 이미지
CREATE TABLE product_image (
    image_url VARCHAR(255) NOT NULL PRIMARY KEY,
    thumbnail_image_url VARCHAR(255) NOT NULL,
    genre_cd INTEGER NOT NULL,
    product_uketsuke_bng    VARCHAR(80) NOT NULL,
    display_order INTEGER NOT NULL,
    create_date   TIMESTAMP,
    modified_date TIMESTAMP
);
ALTER TABLE product_image ADD CONSTRAINT fk_product_image FOREIGN KEY (product_uketsuke_bng) REFERENCES product (uketsuke_bng);


-- LOG
CREATE TABLE WORK_LOG (
    id           INTEGER IDENTITY PRIMARY KEY,
    message      VARCHAR(255),
    create_date  TIMESTAMP,
);

-- STATUS
CREATE TABLE CONFIG (
    id             INTEGER IDENTITY PRIMARY KEY,
    config_group   VARCHAR(255),
    config_key     VARCHAR(255),
    config_value   VARCHAR(800)
);

-- customer
create table customer (
    id              INTEGER IDENTITY PRIMARY KEY,
    user_id         VARCHAR(255),
    password        VARCHAR(255),
    name            VARCHAR(255),
    tel             VARCHAR(255),
    email           VARCHAR(255),
    description     VARCHAR(255),
    privilege       VARCHAR(255),
    enabled         boolean,
    display         boolean,
    last_login       TIMESTAMP
);