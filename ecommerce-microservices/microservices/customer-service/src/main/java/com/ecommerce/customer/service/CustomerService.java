package com.ecommerce.customer.service;

import com.ecommerce.customer.dto.*;
import com.ecommerce.customer.entity.Address;
import com.ecommerce.customer.entity.Customer;
import com.ecommerce.customer.entity.CustomerRole;
import com.ecommerce.customer.exception.CustomerNotFoundException;
import com.ecommerce.customer.exception.DuplicateEmailException;
import com.ecommerce.customer.repository.AddressRepository;
import com.ecommerce.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository  addressRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtService          jwtService;

    // ─── Registration ─────────────────────────────────────────────────────────

    public CustomerResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        Customer customer = Customer.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .role(CustomerRole.ROLE_USER)
            .build();

        customer = customerRepository.save(customer);
        log.info("New customer registered: id={} email={}", customer.getId(), customer.getEmail());
        return mapToResponse(customer);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new CustomerNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new CustomerNotFoundException("Invalid email or password");
        }

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Account is deactivated");
        }

        String token = jwtService.generateToken(customer);
        log.info("Customer logged in: id={}", customer.getId());

        return LoginResponse.builder()
            .token(token)
            .customerId(customer.getId())
            .email(customer.getEmail())
            .role(customer.getRole().name())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .build();
    }

    // ─── Profile ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long customerId) {
        Customer customer = findById(customerId);
        return mapToResponse(customer);
    }

    public CustomerResponse updateProfile(Long customerId, RegisterRequest request) {
        Customer customer = findById(customerId);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        // Don't allow email change here for security; require separate flow
        return mapToResponse(customerRepository.save(customer));
    }

    // ─── Addresses ────────────────────────────────────────────────────────────

    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        Customer customer = findById(customerId);

        // If this is marked default, clear previous defaults
        if (request.isDefault()) {
            addressRepository.findByCustomerId(customerId)
                .forEach(a -> a.setDefault(false));
        }

        Address address = Address.builder()
            .customer(customer)
            .street(request.getStreet())
            .city(request.getCity())
            .state(request.getState())
            .zipCode(request.getZipCode())
            .country(request.getCountry())
            .isDefault(request.isDefault())
            .build();

        return mapAddressToResponse(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long customerId) {
        return addressRepository.findByCustomerId(customerId)
            .stream()
            .map(this::mapAddressToResponse)
            .collect(Collectors.toList());
    }

    public void deleteAddress(Long customerId, Long addressId) {
        addressRepository.deleteByIdAndCustomerId(addressId, customerId);
    }

    // ─── Admin: Get All Customers ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Customer findById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));
    }

    private CustomerResponse mapToResponse(Customer c) {
        return CustomerResponse.builder()
            .id(c.getId())
            .firstName(c.getFirstName())
            .lastName(c.getLastName())
            .email(c.getEmail())
            .phone(c.getPhone())
            .role(c.getRole().name())
            .active(c.isActive())
            .createdAt(c.getCreatedAt())
            .build();
    }

    private AddressResponse mapAddressToResponse(Address a) {
        return AddressResponse.builder()
            .id(a.getId())
            .street(a.getStreet())
            .city(a.getCity())
            .state(a.getState())
            .zipCode(a.getZipCode())
            .country(a.getCountry())
            .isDefault(a.isDefault())
            .build();
    }
}
