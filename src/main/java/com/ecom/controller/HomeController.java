package com.ecom.controller;

import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Model.Category;
import com.ecom.Model.Product;
import com.ecom.Model.UserDtls;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.ProductService;
import com.ecom.Service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CartService cartService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private CommonUtil commonUtil;
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m) {
		if(p != null) {
			String email =p.getName();
			UserDtls userDtls =userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			 
		}
		
	List<Category> allActiveCategory =	categoryService.getAllActiveCategory();
	m.addAttribute("categorys", allActiveCategory);
	}
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/products")
	public String product(Model m,@RequestParam (value="category" ,defaultValue="") String category) {
		//System.out.println("category="+category);
		List<Category> categories =categoryService.getAllActiveCategory();
		List<Product> products =productService.getAllActiveProducts(category);
		m.addAttribute("categories",categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue",category );
		return "product";
	}
	
	@GetMapping("/product/{id}")
	public String products(@PathVariable int id,Model m) {
		Product productById =productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_products";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user,@RequestParam("img") MultipartFile file,HttpSession session) throws IOException{
		String imageName=file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveUser(user);
		if(!ObjectUtils.isEmpty(saveUser))
		{
			if(!file.isEmpty())
			{
				
				 File saveFile = new ClassPathResource("static/img/profile/").getFile();
		    		Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator +"profile"+File.separator 
		    				+file.getOriginalFilename());
		             
		             //System.out.println(path);
		             Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg","Saved successfully");
		}else {
			session.setAttribute("errorMsg","Something went wrong on server");
		}
		
		return "redirect:/register";
	}
	
	
	//forgot password code
	
	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		
		
		return "forgot_password";
	}
	
	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		
		UserDtls userByEmail = userService.getUserByEmail(email);
		if(ObjectUtils.isEmpty(userByEmail))
		{
			session.setAttribute("errorMsg","invalid email");
		}else
		{
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email,resetToken);
			//Generate url
			
			String url =CommonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
			
			Boolean sendMail = commonUtil.sendMail(url,email);
			if(sendMail)
			{
				session.setAttribute("succMsg","Please send your email.. Password reset link sent");
			}else
			{
				session.setAttribute("errorMsg","Something went wrong on server !! Email not send");
			}
		}
		return "redirect:/forgot-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token,HttpSession session,Model m) {
		
		UserDtls userByToken =userService.getUserByToken(token);
		
		if(userByToken==null)
		{
			m.addAttribute("msg","Your link is invalid or expired");
			return "message";
		}
		m.addAttribute("token",token);
		return "reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password
			,HttpSession session,Model m) {
		
		UserDtls userByToken =userService.getUserByToken(token);
		
		if(userByToken==null)
		{
			m.addAttribute("msg","Your link is invalid or expired");
			return "message";
		}else
		{
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			//session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("msg","Password change successfully");
			return "message";
		}
		
	}
	
	
	
}
