package com.yonga.auc.data.log;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogService {

	@Autowired
	private LogRepository logRepository;
	public Log addLog(String message) {
		Log log = new Log();
		log.setMessage(message);
		log.setCreateDate(new Date());
		return save(log);
	}
	public Log save(Log log) {
		return this.logRepository.save(log);
	}
	
	public List<Log> getLatestLogList() {
		return this.logRepository.findTop30ByOrderByCreateDateDesc();
	}
	
	public List<Log> getAllLogList() {
		return this.logRepository.findByOrderByCreateDateDesc();
	}
	
	@Scheduled(initialDelay = 3 * 1000, fixedDelay = 24 * 60 * 60 * 1000)
	public void cleanLog() {
		int deletedLog = this.logRepository.deleteOldLog();
		log.info("deleted old log [{}]", deletedLog);
	}
	
}
