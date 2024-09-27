package com.ecom.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.Model.Cart;
import com.ecom.Model.Product;
import com.ecom.Model.UserDtls;
import com.ecom.Repository.CartRepsoitory;
import com.ecom.Repository.ProductRepository;
import com.ecom.Repository.UserRepository;
import com.ecom.Service.CartService;

@Service
public class CartServiceImpl implements CartService {
	
	
	@Autowired
	private CartRepsoitory cartRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ProductRepository productRepository;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		
		UserDtls userDtls =userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();
		
		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
		Cart cart=null;
		if(ObjectUtils.isEmpty(cartStatus))
		{
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDtls);
			cart.setQuantity(1);
			cart.setTotalPrice(1*product.getDiscountPrice());
		}else
		{
			cart=cartStatus;
			cart.setQuantity(cart.getQuantity()+1);
			cart.setTotalPrice(cart.getQuantity()*cart.getProduct().getDiscountPrice());
		}
		Cart saveCart = cartRepository.save(cart);
		return saveCart;
	}

	

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
	    
	    List<Cart> carts = cartRepository.findByUserId(userId);
	    
	    Double totalOrderPrice = 0.0;
	    List<Cart> updateCarts = new ArrayList<>();
	    
	    for(Cart c : carts) {
	        // Handle potential null value for discountPrice
	        Double discountPrice = c.getProduct().getDiscountPrice();
	        
	        if (discountPrice == null) {
	            discountPrice = 0.0; // Default value for products without a discount price
	        }
	        
	        // Calculate total price for the cart item
	        Double totalPrice = discountPrice * c.getQuantity();
	        c.setTotalPrice(totalPrice);
	        
	        // Update the total order price
	        totalOrderPrice += totalPrice;
	        c.setTotalOrderPrice(totalOrderPrice);
	        
	        updateCarts.add(c);
	    }
	    
	    return updateCarts;

	}

	@Override
	public Integer getCountCart(Integer userId) {
		
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}



	@Override
	public void updateQuantity(String sy, Integer cid) {
		
		Cart cart =cartRepository.findById(cid).get();
		
		int updateQuantity;
		if(sy.equalsIgnoreCase("de"))
		{
			updateQuantity = cart.getQuantity() - 1;
			if(updateQuantity<=0)
			{
				cartRepository.delete(cart);
				
			}else {
				cart.setQuantity(updateQuantity);
				cartRepository.save(cart);
				
			}
			
			
		}else
			{
				updateQuantity=cart.getQuantity()+1;
				cart.setQuantity(updateQuantity);
				cartRepository.save(cart);
				
				
			}
			
			
		}
		
	

	}

