package com.ecom.controller;

import java.security.Principal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.Model.Cart;
import com.ecom.Model.Category;
import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;
import com.ecom.Model.UserDtls;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.OrderService;
import com.ecom.Service.UserService;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CartService cartService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrderService orderService;
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
	@ModelAttribute
	public void getUserDetails(Model m, Principal p)
	{
		if(p != null)
		{
			String email= p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
			
			
		}
		
		List<Category> allActiveCategory= categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}
	
	
	 @GetMapping("/addCart") 
	 public String addToCart(@RequestParam Integer
	  pid,@RequestParam Integer uid,HttpSession session) 
	 { 
		 Cart saveCart =cartService.saveCart(pid, uid);
	 if(ObjectUtils.isEmpty(saveCart)) {
	  session.setAttribute("errorMsg","Product Add to cart failed");
	  } else {
	  session.setAttribute("succMsg","Product Added to cart"); 
	  } 
	 return "redirect:/product/"+pid; 
	 
	  
	 }
	 
	 @GetMapping("/cartQuantityUpdate")
	 public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid) {
		 
		 
		  cartService.updateQuantity(sy,cid);
		 return "redirect:/user/cart";
	 }
	 
@GetMapping("/cart")	 
public String loadCartPage(Principal p, Model m)
{
	
	UserDtls user = getLoggedInUserDetails(p);
	List<Cart> carts = cartService.getCartsByUser(user.getId());
	m.addAttribute("carts", carts);
	if(carts.size() >0)
	{
		Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
		m.addAttribute("totalOrderPrice",totalOrderPrice);
	}
	
	
	return "/user/cart";
}

private UserDtls getLoggedInUserDetails(Principal p) {
	
	String email=p.getName();
	UserDtls userDtls = userService.getUserByEmail(email);
	return userDtls;
}

@GetMapping("/orders")
public String orderPage(Model m,Principal p) {
	
	UserDtls user = getLoggedInUserDetails(p);
	List<Cart> carts = cartService.getCartsByUser(user.getId());
	m.addAttribute("carts", carts);
	if(carts.size() >0)
	{
		Double orderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
		Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice()+250 +100;
		m.addAttribute("orderPrice",orderPrice);
		m.addAttribute("totalOrderPrice",totalOrderPrice);
	}
	
	return "/user/order";
}


@PostMapping("/save-order")
public String orderPage(@ModelAttribute OrderRequest request,Principal p) {
	UserDtls  user= getLoggedInUserDetails(p);
	orderService.saveOrder(user.getId(), request);
	return "redirect:/user/success";
	
}

@GetMapping("/success")
public String loadSuccess()
{
	return "/user/success";
}
 

@GetMapping("/user-orders")
public String myOrder(Model m,Principal p)
{
	UserDtls  loginUser= getLoggedInUserDetails(p);
	List<ProductOrder> orders =orderService.getOrdersByUser(loginUser.getId());
	m.addAttribute("orders", orders);
	return "/user/my_orders";
}

@GetMapping("/update-status")
public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session)
{
	OrderStatus[] values= OrderStatus.values();
	String status = null;
	for(OrderStatus orderSt:values)
	{
		if(orderSt.getId().equals(st))
		{
			status=orderSt.getName();
		}
	}
	Boolean updateOrder = orderService.updateOrderStatus(id, status);
	if(updateOrder)
	{
		session.setAttribute("succMsg", "Status updated");
	}
	else
	{
		session.setAttribute("errorMsg","Status not updated");
	}
	return "redirect:/user/user-orders";
}
	 
}
