package com.yonga.auc.data.log;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LogRepository extends JpaRepository<Log, Integer> {

	List<Log> findTop30ByOrderByCreateDateDesc();
	
	List<Log> findByOrderByCreateDateDesc();

	@Transactional
	@Modifying
	@Query(value = "delete from work_log w where w.create_date < CURRENT_TIMESTAMP - INTERVAL '3' DAY", nativeQuery=true)
	int deleteOldLog();
	
}
