package com.jtspringproject.JtSpringProject.controller;

import java.sql.*;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.jtspringproject.JtSpringProject.models.Category;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.categoryService;
import com.jtspringproject.JtSpringProject.services.productService;
import com.jtspringproject.JtSpringProject.services.userService;
import com.mysql.cj.protocol.Resultset;

import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.asm.Advice.OffsetMapping.ForOrigin.Renderer.ForReturnTypeName;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private userService userService;
	@Autowired
	private categoryService categoryService;
	
	@Autowired
	private productService productService;
	
	int adminlogcheck = 0;
	String usernameforclass = "";
	@RequestMapping(value = {"/","/logout"})
	public String returnIndex() {
		adminlogcheck =0;
		usernameforclass = "";
		return "userLogin";
	}
	
	
	
	@GetMapping("/index")
	public String index(Model model) {
		if(usernameforclass.equalsIgnoreCase(""))
			return "userLogin";
		else {
			model.addAttribute("username", usernameforclass);
			return "index";
		}
			
	}
	
	
	@GetMapping("/")
	public String adminlogin() {
		
		return "adminlogin";
	}
	@GetMapping("/adminhome")
	public String adminHome(Model model) {
		if(adminlogcheck!=1)
			return "adminHome";
		else
			return "redirect:/admin";
	}
	@GetMapping("/loginvalidate")
	public String adminlog(Model model) {
		
		return "adminlogin";
	}
	@RequestMapping(value = "loginvalidate", method = RequestMethod.POST)
	public ModelAndView adminlogin( @RequestParam("username") String username, @RequestParam("password") String pass) {
		
		User user=this.userService.checkLogin(username, pass);
		
		if(user.getRole().equals("ROLE_ADMIN")) {
			ModelAndView mv = new ModelAndView("adminHome");
			mv.addObject("admin", user);
			return mv;
		}
		else {
			ModelAndView mv = new ModelAndView("adminlogin");
			mv.addObject("msg", "Please enter correct username and password");
			return mv;
		}
	}
	@GetMapping("categories")
	public ModelAndView getcategory() {
		ModelAndView mView = new ModelAndView("categories");
		List<Category> categories = this.categoryService.getCategories();
		mView.addObject("categories",categories);
		return mView;
	}
	@RequestMapping(value = "sendcategory",method = RequestMethod.POST)
	public ModelAndView addCategory(@RequestParam("categoryname") String category_name)
	{
		System.out.println(category_name);
		
		Category category =  this.categoryService.addCategory(category_name);
		if(category.getName().equals(category_name)) {
			ModelAndView mView = new ModelAndView("admin/categories");
			mView.addObject("msg", "Category added successfully");
			return mView;
		}else {
			ModelAndView mView = new ModelAndView("admin/categories");
			mView.addObject("msg", "failed to add category");
			return mView;
		}
	}
	
	@GetMapping("/admin/categories/delete")
	public ModelAndView removeCategoryDb(@RequestParam("id") int id)
	{	
			this.categoryService.deleteCategory(id);
			ModelAndView mView = new ModelAndView("admin/categories");
			return mView;
	}
	
	@GetMapping("/admin/categories/update")
	public ModelAndView updateCategoryDb(@RequestParam("categoryid") int id, @RequestParam("categoryname") String categoryname)
	{
		Category category = this.categoryService.updateCategory(id, categoryname);
		ModelAndView mView = new ModelAndView("admin/categories");
		return mView;
	}

	
//	 --------------------------Remaining --------------------
	@GetMapping("products")
	public ModelAndView getproduct() {
		
		ModelAndView mView = new ModelAndView("products");
		
		List<Product> products = this.productService.getProducts();
		
		if(products.isEmpty()) {
			mView.addObject("msg","No products are available");
		}else {
			mView.addObject("products",products);	
		}
		
		return mView;
	}
	@GetMapping("products/add")
	public String addproduct() {
		return "productsAdd";
	}

	@RequestMapping(value = "products/add",method=RequestMethod.POST)
	public ModelAndView addProduct(@ModelAttribute Product product) {
		System.out.println(product.getDescription());
		
		ModelAndView mView = new ModelAndView("products");
		
		List<Product> products = this.productService.getProducts();
		mView.addObject("products",products);
		
		return mView;
	}
	
	
	@GetMapping("products/update/{id}")
	public ModelAndView updateproduct(@PathVariable("id") int id) {
		
		ModelAndView mView = new ModelAndView("productsUpdate");
		
		Product product = this.productService.getProduc(id);
		mView.addObject("product", product);
		return mView;
	}
	
	@RequestMapping(value = "products/update/{id}",method=RequestMethod.POST)
	public ModelAndView updateProduct(@ModelAttribute Product product) 
	{
		ModelAndView mView = new ModelAndView("products");
		
		List<Product> products = this.productService.getProducts();
		
		if(products.isEmpty()) {
			mView.addObject("msg","No products are available");
		}else {
			mView.addObject("products",products);	
		}
		
		return mView;
	}
	
	@GetMapping("products/delete")
	public String removeProductDb(@RequestParam("id") int id)
	{
		this.productService.deleteProduct(id);
		return "redirect:/admin/products";
	}
	
	@PostMapping("products")
	public String postproduct() {
		return "redirect:/admin/categories";
	}
	
	@GetMapping("customers")
	public ModelAndView getCustomerDetail() {
		ModelAndView mView = new ModelAndView("displayCustomers");
		List<User> users = this.userService.getUsers();
		mView.addObject("customers", users);
		return mView;
	}
	
	
	@GetMapping("profileDisplay")
	public String profileDisplay(Model model) {
		String displayusername,displaypassword,displayemail,displayaddress;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommjava","root","");
			Statement stmt = con.createStatement();
			ResultSet rst = stmt.executeQuery("select * from users where username = '"+usernameforclass+"';");
			
			if(rst.next())
			{
			int userid = rst.getInt(1);
			displayusername = rst.getString(2);
			displayemail = rst.getString(3);
			displaypassword = rst.getString(4);
			displayaddress = rst.getString(5);
			model.addAttribute("userid",userid);
			model.addAttribute("username",displayusername);
			model.addAttribute("email",displayemail);
			model.addAttribute("password",displaypassword);
			model.addAttribute("address",displayaddress);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception:"+e);
		}
		System.out.println("Hello");
		return "updateProfile";
	}
	
	@RequestMapping(value = "updateuser",method=RequestMethod.POST)
	public String updateUserProfile(@RequestParam("userid") int userid,@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("address") String address) 
	
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecommjava","root","");
			
			PreparedStatement pst = con.prepareStatement("update users set username= ?,email = ?,password= ?, address= ? where uid = ?;");
			pst.setString(1, username);
			pst.setString(2, email);
			pst.setString(3, password);
			pst.setString(4, address);
			pst.setInt(5, userid);
			int i = pst.executeUpdate();	
			usernameforclass = username;
		}
		catch(Exception e)
		{
			System.out.println("Exception:"+e);
		}
		return "redirect:/index";
	}

}
