package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;


//CustomerDao provides the database access for all the endpoints in customer controller

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;
    //Creates a new Customer in database
    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    //find existing customer by contact number
    public CustomerEntity getCustomerByContactNumber(String contactNumber) {
        try {
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class).setParameter("contactNumber", contactNumber).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //creates new authorization from given CustomerAuthEntity object
    public CustomerAuthEntity createCustomerAuth(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    //find existing customer authorization by access token
    public CustomerAuthEntity getCustomerAuthByAccessToken(String accessToken) {
        try {
            return entityManager.createNamedQuery("customerAuthByAccessToken", CustomerAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //updates existing customer authorization
    public CustomerAuthEntity updateCustomerAuth(CustomerAuthEntity customerAuthEntity) {
        return entityManager.merge(customerAuthEntity);
    }

    //updates existing customer
    public CustomerEntity updateCustomerEntity(CustomerEntity customerEntity) {
        return entityManager.merge(customerEntity);
    }

    //returns customer entity for given UUI
    public CustomerEntity getCustomerByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("customerByUUID", CustomerEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}