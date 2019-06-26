package com.yonga.auc.data.category.detail;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface MakerRepository extends JpaRepository<Maker, Integer> {
	@Query(value="select m.* from maker m where category_no = :categoryId order by maker_cd asc", nativeQuery=true)
    List<Maker> findMaker(@Param("categoryId") Integer categoryId) throws DataAccessException;
	@Transactional
	@Modifying
	@Query(value = "delete from maker where category_no = :categoryNo", nativeQuery = true)
	void deleteByCategoryNo(@Param("categoryNo") Integer categoryNo) throws DataAccessException;
}
