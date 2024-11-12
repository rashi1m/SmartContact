package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Model.Category;
import com.ecom.Model.Product;
import com.ecom.Model.ProductOrder;
import com.ecom.Model.UserDtls;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.OrderService;
import com.ecom.Service.ProductService;
import com.ecom.Service.UserService;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;
    
    @Autowired
	private UserService userService;
    
    @Autowired
    private CartService cartService;
	
    @Autowired
    private OrderService orderService;
    
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
    @GetMapping("/")
    public String index() {
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m) {
    	List<Category> categories =categoryService.getAllCategory();
    	m.addAttribute("categories",categories);
        return "admin/add_product";
    }

    @GetMapping("/category")
    public String category(Model m) {
        m.addAttribute("categorys", categoryService.getAllCategory());
        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(
        @ModelAttribute Category category,
        @RequestParam("file") MultipartFile file,
        HttpSession session
    ) throws IOException {
        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        Boolean existcategory = categoryService.existCategory(category.getName());
        if (existcategory) {
            session.setAttribute("errorMsg", "Category name already exists");
            
        } else {
            Category savedCategory = categoryService.saveCategory(category);
            if (ObjectUtils.isEmpty(savedCategory)) {
                session.setAttribute("errorMsg", "Not saved!! Internal server error");
            } else {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_images"+File.separator+file.getOriginalFilename());
                
               System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                session.setAttribute("succMsg", "Successfully saved");
            }
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("succMsg", "Category deleted successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on server");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model m) {
        m.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        Category category1 = categoryService.getCategoryById(category.getId());
        String imageName = !file.isEmpty() ?category1.getImageName():file.getOriginalFilename();
        if (!ObjectUtils.isEmpty(category1)) {
            category1.setName(category.getName());
            category1.setIsActive(category.getIsActive());
            category1.setImageName(imageName);
        }
        Category updateCategory = categoryService.saveCategory(category1);
        if (!ObjectUtils.isEmpty(updateCategory)) {
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator +"category_images"+File.separator+file.getOriginalFilename());
                
                System.out.println(path);
                 Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            session.setAttribute("succMsg", "Category update success");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on server");
        }
        return "redirect:/admin/loadEditCategory/" + category.getId();
    }
    
    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,
    		HttpSession session) throws IOException{
        String imageName = image.isEmpty()?"default.jpg" : image.getOriginalFilename();
            product.setImage(imageName);
            product.setDiscount(0);
            product.setDiscountPrice(product.getPrice());
    	
    	Product saveProduct = productService.saveProduct(product);
    	if(!ObjectUtils.isEmpty(saveProduct)) {
    		
    		 File saveFile = new ClassPathResource("static/img").getFile();
    		Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator +"product_images"+File.separator 
    				+image.getOriginalFilename());
             
             
             Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    		session.setAttribute("succMsg","Product saved successfully");
    	}else {
    		session.setAttribute("errorMsg","Something went wrong");
    	}
    	
    	return "redirect:/admin/loadAddProduct";
    }
    
    @GetMapping("/products")
    public String loadViewProduct(Model m) {
    	m.addAttribute("products",productService.getAllProducts());
    	return "admin/products";
    }
    
    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id,HttpSession session) {
         Boolean deleteProduct = productService.deleteProduct(id);
         if(deleteProduct) {
        	 session.setAttribute("succMsg","Product delete successfully");
         }else {
        	 session.setAttribute("errorMsg","Something went wrong on server");
         }
    	return "redirect:/admin/products";
    }
    
    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id,Model m) {
    	m.addAttribute("product",productService.getProductById(id));
    	m.addAttribute("categories",categoryService.getAllCategory());
    	return "admin/edit_product";
    }
    
    @PostMapping("/updateProduct")
    public String editProduct(@ModelAttribute Product product,Model m,
    		HttpSession session,@RequestParam("file") MultipartFile image) throws IOException {
    	if(product.getDiscount()<0 || product.getDiscount()>100)
    	{
    		session.setAttribute("errorMsg","Invalid discount");
    	}else {
    	Product updateproduct = productService.updateProduct(product, image);
    	if(!ObjectUtils.isEmpty(updateproduct)) {
    		
    		
    		session.setAttribute("succMsg","Product updated successfully");
    		
    	}else {
    		
    		session.setAttribute("errorMsg","something went wrong on server");
    	}	
    	}
    	return "redirect:/admin/editProduct/" + product.getId();
    }
    
    @GetMapping("/users")
    public String getAllUsers(Model m)
    {
    	List<UserDtls> users = userService.getUsers("ROLE_USER");
    	m.addAttribute("users", users);
    	return "/admin/users";
    	
    }
    @GetMapping("/updateSts")
    public String updateUserAccountStatus(@RequestParam Boolean status,@RequestParam Integer id,
    		HttpSession session) {
    	
    	Boolean f= userService.updateAccountStatus(id,status);
    	if(f) {
    		session.setAttribute("succMsg","Account status updated");
    	}else {
    		session.setAttribute("errorMsg","Something went wrong on server");
    	}
    	return "redirect:/admin/users";
    }
    
@GetMapping("/orders")
public String getAllOrders(Model m)
    {
   List<ProductOrder> allOrders = orderService.getAllOrders();
   m.addAttribute("orders", allOrders);
    	return "/admin/orders";
    	
    }
    
@PostMapping("/update-order-status")
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
    	return "redirect:/admin/orders";
    }
    
}
