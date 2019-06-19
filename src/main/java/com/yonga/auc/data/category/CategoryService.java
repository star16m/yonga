package com.yonga.auc.data.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

	public List<Category> findNotCompleteCategory() {
		List<Category> categoryList = findAll();
		return categoryList.stream().filter(c -> "EXTRACT_INIT".equals(c.getStatus()) || "PROGRESS".equals(c.getStatus())).collect(Collectors.toList());
	}
	
	public Category save(Category category) {
		return this.categoryRepository.save(category);
	}
	
	public Category clean(Category category) {
		category.setModifiedDate(new Date());
		category.setTotalProductNum(0);
		category.setExtProductNum(0);
		category.setStatus("INIT");
		return save(category);
	}
}
