package com.yonga.auc.data.category;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	public List<Category> findAll() {
		return categoryRepository.findAll();
	}
	
	public Category save(Category category) {
		return this.categoryRepository.save(category);
	}
	
	public Category clean(Category category) {
		category.setModifiedDate(new Date());
		category.setTotalProductNum(0);
		category.setExtProductNum(0);
		category.setExtProductDetailNum(0);
		category.setStatus("INIT");
		return save(category);
	}
}
