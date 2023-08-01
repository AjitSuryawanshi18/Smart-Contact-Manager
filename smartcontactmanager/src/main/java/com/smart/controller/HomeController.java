package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

//import jakarta.servlet.http.HttpSession;
//import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	// home handler
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	//about handler
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	//sign up handler
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//Login handler
//	@RequestMapping("/login")
//	public String login(Model model) {
//		
//		model.addAttribute("title","Login - Smart Contact Manager");
//		return "login";
//	}
	
	
//	handler for registering user
	@RequestMapping(value= "/do_Register", method = RequestMethod.POST)// if we used PostMapping then method is not required to write
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreement", defaultValue = "false")boolean agreement,Model model,HttpSession session) {
		
		try {
			
			if(!agreement) {
				throw new Exception("you not checked terms and condition");
			}
			
			if (result1.hasErrors()) {
				System.out.println("Error : "+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			// WHEN SETTING ANY ROLE KEEP ONE THING IN MIND WRITE "ROLE_ANYROLE" BCZ ROLE IS CONSIDERED AS PREFIX.
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
//			System.out.println("agreement "+agreement);
//			System.out.println("User"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			
			
			session.setAttribute("message",new Message("Successfully Registered !!  LOgin NOw", "alert-success"));
			return "signup";
		} catch (Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("Something Went Wrong !! check red place or you not accepted terms and conditions", "alert-danger"));
			return "signup";
		}
		
		
	}
	
//	handler for customized LOgin PAge
	@GetMapping("/custom_login")
	public String customLogin(Model model) {
		
		model.addAttribute("title","Login - Smart Contact Manager");

		return "CustomizedLoginPage";
	}
	
	// handler for login failure
	@GetMapping("/login_fail")
	public String login_fail(Model model) {
		
		model.addAttribute("title","Login Failed - Smart Contact Manager");

		return "login_fail";
	}
}
