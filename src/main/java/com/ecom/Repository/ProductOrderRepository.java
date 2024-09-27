package com.ecom.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.Model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder,Integer>{

	List<ProductOrder> findByUserId(Integer userId);

	
}
