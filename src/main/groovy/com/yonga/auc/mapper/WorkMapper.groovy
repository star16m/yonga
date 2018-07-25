package com.yonga.auc.mapper

import com.yonga.auc.category.Category
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select

@Mapper
interface CategoryMapper {

	@Select('''
		SELECT w.work_product_id
		  FROM work w
		 WHERE work_category_id = #{workCategoryId}
	''')
	List<Integer> findCategory() throws DataAccessException;
	@Delete('''
		DELETE
		  FROM work
		 WHERE work_category_id = #{workCategoryId}
		   AND work_product_id  = #{workProductId}
	''')
    Long deleteByWorkCategoryIdAndWorkProductId(Integer workCategoryId, Integer workProductId);
    @Delete('''
    	DELETE
    	  FROM work
    	 WHERE work_category_id = #{workCategoryId}
    ''')
    Long deleteByWorkCategoryId(Integer workCategoryId
}