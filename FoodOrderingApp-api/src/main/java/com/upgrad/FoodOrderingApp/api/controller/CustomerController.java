package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    //Controller for Signup
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(
            @RequestBody(required = false) final SignupCustomerRequest signupCustomerRequest)
            throws SignUpRestrictedException
    {
        //check for blank responses
        if (signupCustomerRequest.getFirstName().equals("") || signupCustomerRequest.getEmailAddress().equals("") ||
                        signupCustomerRequest.getContactNumber().equals("") || signupCustomerRequest.getPassword().equals("")
        ) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        //create a new CustomerEntity and fill the details
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassoword(signupCustomerRequest.getPassword());
        //persist in database
        final CustomerEntity createdCustomerEntity = customerService.saveCustomer(customerEntity);
        //Set the response
        SignupCustomerResponse customerResponse = new SignupCustomerResponse()
                .id(createdCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
    }

    //Controller for Sign in
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException
    {
        byte[] decode;
        String contactNumber;
        String customerPassword;
        //decode the password from the encoded text field
        try {
            decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");
            contactNumber = decodedArray[0];
            customerPassword = decodedArray[1];
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        //authenticate the Customer
        CustomerAuthEntity createdCustomerAuthEntity = customerService.authenticate(contactNumber, customerPassword);
        //set the response
        LoginResponse loginResponse = new LoginResponse().id(createdCustomerAuthEntity.getCustomer()
                        .getUuid()).message("LOGGED IN SUCCESSFULLY");
        loginResponse.setId(createdCustomerAuthEntity.getCustomer().getUuid());
        loginResponse.setFirstName(createdCustomerAuthEntity.getCustomer().getFirstName());
        loginResponse.setLastName(createdCustomerAuthEntity.getCustomer().getLastName());
        loginResponse.setContactNumber(createdCustomerAuthEntity.getCustomer().getContactNumber());
        loginResponse.setEmailAddress(createdCustomerAuthEntity.getCustomer().getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", createdCustomerAuthEntity.getAccessToken());
        List<String> header = new ArrayList<>();
        header.add("access-token");
        headers.setAccessControlExposeHeaders(header);
        //send the response
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    //Controller for Logout
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException
    {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerAuthEntity customerAuthEntity = customerService.logout(accessToken);
        LogoutResponse logoutResponse = new LogoutResponse().id(customerAuthEntity.getCustomer()
                .getUuid()).message("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    //Controller for updating the Customer info
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> update(
            @RequestBody(required = false) final UpdateCustomerRequest updateCustomerRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UpdateCustomerException
    {
        //check for first name
        if (updateCustomerRequest.getFirstName().equals("")) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        //get the customer by accessToken
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        customerEntity.setFirstName(updateCustomerRequest.getFirstName());
        if (!updateCustomerRequest.getLastName().equals("")) {
            customerEntity.setLastName(updateCustomerRequest.getLastName());
        }
        //update the database
        CustomerEntity updatedCustomerEntity = customerService.updateCustomer(customerEntity);
        //set the response
        UpdateCustomerResponse customerResponse = new UpdateCustomerResponse()
                .id(updatedCustomerEntity.getUuid()).status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        customerResponse.setFirstName(updatedCustomerEntity.getFirstName());
        customerResponse.setLastName(updatedCustomerEntity.getLastName());
        return new ResponseEntity<UpdateCustomerResponse>(customerResponse, HttpStatus.OK);
    }

    //Controller for Password Update
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> changePassword(
            @RequestBody(required = false) final UpdatePasswordRequest updatePasswordRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UpdateCustomerException
    {
        //Check for password field if empty
        if (updatePasswordRequest.getOldPassword().equals("") || updatePasswordRequest.getNewPassword().equals("")) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        //get the customer by access token
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        //call service for password update
        CustomerEntity updatedCustomerEntity = customerService.updateCustomerPassword(
                updatePasswordRequest.getOldPassword(),
                updatePasswordRequest.getNewPassword(),
                customerEntity
        );
        //set response
        UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse()
                .id(updatedCustomerEntity.getUuid())
                .status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
    }
}
