package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private StateDao stateDao;

    //Implement Save Address
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity) throws SaveAddressException {

        // Validation for required fields
        if (addressEntity.getFlatBuilNo().equals("") || addressEntity.getLocality().equals("") ||
                        addressEntity.getCity().equals("") || addressEntity.getPincode().equals("") ||
                        addressEntity.getState() == null || addressEntity.getActive() == null
        ) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }

        // Validation for format of Pincode
        if(!addressEntity.getPincode().matches("^[1-9][0-9]{5}$")){
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        AddressEntity createdAddressEntity = addressDao.createAddress(addressEntity);

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerEntity.getId());
        customerAddressEntity.setAddress(createdAddressEntity.getId());
        customerAddressDao.createCustomerAddress(customerAddressEntity);

        return createdAddressEntity;
    }

    //Find the state for a given uuid
    public StateEntity getStateByUUID(String stateUUID) throws AddressNotFoundException {
        StateEntity stateEntity = stateDao.getStateByUUID(stateUUID);
        if (stateEntity == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this state id");
        }
        return stateEntity;
    }

    //Get all the address for a given customer
    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {
        return customerEntity.getAddresses();
    }

    //Get all the state
    public List<StateEntity> getAllStates() {
        return stateDao.getAllStates();
    }

    //Get address by UUID
    public AddressEntity getAddressByUUID(String addressUUID, CustomerEntity customerEntity) throws AddressNotFoundException, AuthorizationFailedException {
        AddressEntity addressEntity = addressDao.getAddressByUUID(addressUUID);

        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        if (!addressEntity.getCustomer().getUuid().equals(customerEntity.getUuid())) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        return addressEntity;
    }

    //Delete address
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        return addressDao.deleteAddressEntity(addressEntity);
    }
}
