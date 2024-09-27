package com.ecom.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.ecom.Model.Cart;

public interface CartRepsoitory extends JpaRepository<Cart,Integer>{

	public Cart findByProductIdAndUserId(Integer userId,Integer productId);
	
	public Integer countByUserId(Integer userId);

	public List<Cart> findByUserId(Integer userId);
	
}
