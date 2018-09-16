package com.yonga.auc.config;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfigRepository extends JpaRepository<Config, Integer> {
    @Query(value="select c.* from config c where config_group = :group and config_key = :key", nativeQuery=true)
    Config findConfig(@Param("group") String group, @Param("key")String key) throws DataAccessException;

    @Query(value="select c.* from config c where config_group = :group order by key", nativeQuery = true)
    List<Config> getConfig(String group);

    @Query(value="select count(c.*) as cnt from config c where c.config_group = :group", nativeQuery = true)
    Integer countGroupConfigs(@Param("group") String group);

}
