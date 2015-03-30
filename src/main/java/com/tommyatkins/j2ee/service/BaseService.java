package com.tommyatkins.j2ee.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;

import com.tommyatkins.j2ee.entity.Page;
import com.tommyatkins.j2ee.exception.BusinessException;

public interface BaseService<T> {
	Serializable save(T t);

	void update(T t);

	void delete(Serializable id);

	/**
	 * Only used in simple primary key situation
	 * 
	 * @param entityClass
	 * @param id
	 */
	void merge(T t);

	T get(Serializable id) throws BusinessException;

	List<T> getAll();

	List<?> findByHql(String hql, Object... objects);

	List<?> findBySql(String sql, Object... objects);

	int executeBySql(String sql, Object... objects);

	/**
	 * Just support simple POJO query , which fields are simple type like
	 * String, int, boolean and so on. So far, this method can not deal with the
	 * POJO which want to query that field is collection and other beans.
	 * 
	 * @param queryPOJO
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	List<T> intelligentQuery(T queryPOJO) throws IllegalArgumentException, IllegalAccessException;

	List<?> findByFields(List<?> params, String... fields);

	Page findByPageAndHql(Page page, String hql, List<?> params);

	Page findByPageAndSql(Page page, String sql, List<?> params);

	List<T> loadAll();

	Page findByConditions(Page page, List<String> conditions, List<?> params);

	List<T> findByConditionsWithOutPage(List<String> conditions, List<?> params);

	Integer countByHql(String hql, List<?> params);

	Integer countByConditions(List<String> conditions, List<?> params);

	
	void executeByHql(String hql, List<?> object);

	
	Session getCurrentSession();

}
