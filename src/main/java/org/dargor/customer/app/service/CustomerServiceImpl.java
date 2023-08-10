package org.dargor.customer.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dargor.customer.app.client.ProductClient;
import org.dargor.customer.app.dto.*;
import org.dargor.customer.app.exception.CustomException;
import org.dargor.customer.app.exception.ErrorDefinition;
import org.dargor.customer.core.repository.CustomerRepository;
import org.dargor.customer.core.util.mapper.CustomerMapper;
import org.dargor.customer.core.util.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final CustomerMapper customerMapper = CustomerMapper.INSTANCE;
    private static final ProductMapper productMapper = ProductMapper.INSTANCE;
    private final CustomerRepository customerRepository;
    private final ProductClient productClient;

    @Override
    public WishListResponseDto createCustomer(CustomerCreationRequestDto request) {
        try {
            var customer = customerMapper.customerCreationRequestToCustomer(request);
            log.info(String.format("Customer %s", customer));
            var savedCustomer = customerRepository.save(customer);
            log.info(String.format("Saved Customer %s", customer));
            var customerDto = customerMapper.customerToCustomerDto(savedCustomer);
            log.info(String.format("Customer response %s", customerDto));
            var wishListRequest = productMapper.toWishListRequestDto(customer.getId(), request.getProducts());
            log.info(String.format("WishListDTO response %s", wishListRequest));
            var products = productClient.createProducts(wishListRequest);
            var wishListResponse = productMapper.toWishListResponseDto(products, customerDto);
            log.info(String.format("Customer created successfully [request: %s] [response: %s]", request, wishListResponse));
            return wishListResponse;
        } catch (Exception e) {
            log.error(String.format("Error found creating customer [request: %s] [error: %s]", request.toString(), e.getMessage()));
            throw e;
        }
    }

    @Override
    public CustomerDto getCustomer(UUID customerId) {
        try {
            if (ObjectUtils.isEmpty(customerId))
                throw new CustomException(ErrorDefinition.INVALID_INPUT_DATA.getMessage(), null);

            var customer = customerRepository.getById(customerId);
            var response = customerMapper.customerToCustomerDto(customer);
            log.info(String.format("Customer fetched successfully [customerId: %s] [response: %s]", customerId, response.toString()));
            return response;
        } catch (Exception e) {
            log.error(String.format("Error found fetching customer [customerId: %s] [error: %s]", customerId, e.getMessage()));
            throw e;
        }
    }

    @Override
    public CustomerDto updateCustomer(CustomerUpdateRequestDto request) {
        try {
            var customer = customerMapper.customerUpdateRequestToCustomer(request);
            var updatedCustomer = customerRepository.save(customer);
            var response = customerMapper.customerToCustomerDto(updatedCustomer);
            log.info(String.format("Customer updated successfully [request: %s] [response: %s]", request.toString(), response.toString()));
            return response;
        } catch (Exception e) {
            log.error(String.format("Error found updating customer [request: %s] [error: %s]", request.toString(), e.getMessage()));
            throw e;
        }
    }

    public WishListResponseDto getWishList(UUID customerId) {
        try {
            if (ObjectUtils.isEmpty(customerId))
                throw new CustomException(ErrorDefinition.INVALID_INPUT_DATA.getMessage(), null);
            var wishList = productClient.getWishList(customerId);
            log.info(String.format("Products fetched successfully [products %s]", wishList.toString()));
            var customer = customerRepository.getById(customerId);
            log.info(String.format("Customer fetched successfully [entity %s]", customer));
            var customerDto = customerMapper.customerToCustomerDto(customer);
            var response = productMapper.toWishListResponseDto(wishList, customerDto);
            log.info(String.format("Request performed successfully [request: %s] [response: %s]", customerId, response.toString()));
            return response;
        } catch (Exception e) {
            log.error(String.format("Error found adding products to cart for customer [id: %s] [error: %s]", customerId, e.getMessage()));
            throw e;
        }
    }

}
