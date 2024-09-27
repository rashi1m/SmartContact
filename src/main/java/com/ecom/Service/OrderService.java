package com.ecom.Service;

import java.util.List;

import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;


public interface OrderService {

	public void saveOrder(Integer userId,OrderRequest orderRequest);
	
	public List<ProductOrder> getOrdersByUser(Integer userId);
	
	public Boolean updateOrderStatus(Integer id,String status);
	
	public List<ProductOrder> getAllOrders();
}
