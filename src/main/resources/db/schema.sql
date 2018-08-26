DROP TABLE dual IF EXISTS;
DROP TABLE product_image IF EXISTS;
DROP TABLE product IF EXISTS;
DROP TABLE category IF EXISTS;
DROP TABLE work_log IF EXISTS;

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
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);

-- 제품
CREATE TABLE product (
    id INTEGER IDENTITY PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    category_no INTEGER NOT NULL,
    product_no INTEGER NOT NULL,
    desc_uketsuke_no VARCHAR(80),
    desc_keijo VARCHAR(255),
    desc_open_count VARCHAR(255),
    desc_open_date VARCHAR(255),
    desc_seri_bng VARCHAR(255),
    desc_maker VARCHAR(255),
    desc_type VARCHAR(255),
    desc_item_name VARCHAR(255),
    desc_rating VARCHAR(255),
    desc_outer VARCHAR(255),
    desc_interior VARCHAR(255),
    desc_start VARCHAR(255),
    desc_result VARCHAR(255),
    desc_model_no VARCHAR(255),
    desc_reference_price VARCHAR(255),
    desc_sales_point VARCHAR(255),
    desc_sales_point2 VARCHAR(255),
    desc_accessories VARCHAR(255),
    desc_note VARCHAR(255),
    desc_correction VARCHAR(255),
    create_date TIMESTAMP,
    modified_date TIMESTAMP
);

-- 제품 이미지
CREATE TABLE product_image (
    id            INTEGER IDENTITY PRIMARY KEY,
    name          VARCHAR(255),
    product_id    INTEGER NOT NULL,
    create_date   TIMESTAMP,
    modified_date TIMESTAMP
);
ALTER TABLE product_image ADD CONSTRAINT fk_product_image FOREIGN KEY (product_id) REFERENCES product (id);


-- LOG
CREATE TABLE WORK_LOG (
    id           INTEGER IDENTITY PRIMARY KEY,
    message      VARCHAR(255),
    create_date  TIMESTAMP,
);