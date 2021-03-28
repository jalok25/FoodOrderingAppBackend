package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

//AddressDao class provides the database access for all the endpoints in address controller

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Creates address entity from given address
    public AddressEntity createAddress(AddressEntity addressEntity) {
        entityManager.persist(addressEntity);
        return addressEntity;
    }

    //fetch address by id
    public AddressEntity getAddressByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("addressByUUID", AddressEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Updated given address entity
    public AddressEntity updateAddressEntity(AddressEntity addressEntity) {
        return entityManager.merge(addressEntity);
    }

    //Deletes given address entity
    public AddressEntity deleteAddressEntity(AddressEntity addressEntity) {
        entityManager.remove(addressEntity);
        return addressEntity;
    }
}