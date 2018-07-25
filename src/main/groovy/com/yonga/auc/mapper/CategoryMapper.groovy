package com.yonga.auc.mapper

import com.yonga.auc.category.Category
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select

@Mapper
interface CategoryMapper {

    @Insert('''
        INSERT INTO category
            (name, korean, japanese, status, total_product_num, ext_product_num)
        VALUES
            (#{name}, #{korean}, #{japanese}, #{status}, #{total_product_num}, #{ext_product_num})
    ''')
    @Options(useGeneratedKeys = true)
    void insert(Category category);

	@Select('''
		SELECT
            name, korean, japanese, status, total_product_num, ext_product_num
        FROM category
	''')
	List<Categpry> findAll();

    @Select('''
        SELECT
            name, korean, japanese, status, total_product_num, ext_product_num
        FROM category
        WHERE id = #{id}
    ''')
    Category findOne(int id);

	@Select('''
		SELECT c.*
		  FROM category c
		 WHERE ext_product_num is not null
		   and ext_product_num > 0
		   and total_product_num is not null
		   and total_product_num > 0
	''')
	List<Category> findCategory() throws DataAccessException;
}