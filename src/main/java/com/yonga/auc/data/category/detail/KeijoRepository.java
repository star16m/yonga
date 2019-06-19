package com.yonga.auc.data.category.detail;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface KeijoRepository extends JpaRepository<Keijo, Integer> {
    @Query(value="select k.* from keijo k where category_no = :categoryId order by keijo_cd asc", nativeQuery=true)
    List<Keijo> findKeijo(@Param("categoryId") Integer categoryId) throws DataAccessException;
    @Transactional
    void deleteByCategoryNo(Integer categoryNo) throws DataAccessException;
}