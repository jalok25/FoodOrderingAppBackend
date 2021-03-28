package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

//CategoryDao class provides the database access for all the endpoints in category controller

@Repository
public class CategoryDao {

	@PersistenceContext
	private EntityManager entityManager;

	//get all category
	public List<CategoryEntity> getAllCategories() {
		try {
			return entityManager.createNamedQuery("allCategories", CategoryEntity.class).getResultList();
		} catch (NoResultException nre) {
			return null;
		}
	}

	//get category by UUID
	public CategoryEntity getCategoryByUuid(String uuid) {
		try {
			return entityManager.createNamedQuery("categoryByUuid", CategoryEntity.class).setParameter("uuid", uuid).getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}
}