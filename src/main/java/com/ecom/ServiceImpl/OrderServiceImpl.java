package com.ecom.ServiceImpl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.Model.Cart;
import com.ecom.Model.OrderAddress;
import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;
import com.ecom.Repository.CartRepsoitory;
import com.ecom.Repository.ProductOrderRepository;
import com.ecom.Service.OrderService;
import com.ecom.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepository;
	
	@Autowired
	private CartRepsoitory cartRepository;
	
	 
	
	@Override
	public void saveOrder(Integer userId,OrderRequest orderRequest) {
	List<Cart> carts = 	cartRepository.findByUserId(userId);
		for(Cart cart : carts)
		{
			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			order.setStatus(OrderStatus.In_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			
			order.setOrderAddress(address);
			orderRepository.save(order);
			
		}
		
	}




	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
	List<ProductOrder> orders=	orderRepository.findByUserId(userId);
		return null;
	}




	@Override
	public Boolean updateOrderStatus(Integer id, String status) {
		
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if(findById.isPresent())
		{
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			orderRepository.save(productOrder);
			return true;
		}
		return false;
	}




	@Override
	public List<ProductOrder> getAllOrders() {
		
		return orderRepository.findAll();
	}

}
