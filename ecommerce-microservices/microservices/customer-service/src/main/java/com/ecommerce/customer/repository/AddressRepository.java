package com.ecommerce.customer.repository;

import com.ecommerce.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);

    void deleteByIdAndCustomerId(Long id, Long customerId);
}
