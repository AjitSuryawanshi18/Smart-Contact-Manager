package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotPassController {
//	random otp generation 
	Random random = new Random();

//	user repository
	@Autowired
	private UserRepository userRepository;
	
//	autowired email services
	@Autowired
	private EmailService emailService;
	
//	bcrypt password encoder
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

//	open email id for handler
	@RequestMapping("/forgot-pass")
	public String openEmailForm() {

		return "forgot_email_form";
	}

//	handler for sending otp
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email_forgotpass") String email_forgotpass, HttpSession session) {
		System.out.println("email for forgot password " + email_forgotpass);

//		generating otp of 6 digit
//		random object is at top for proper working
//		int otp = random.nextInt(1000000);
		int otp = 100000 + random.nextInt(100000);

//		write code for send otp to email 

		String subject = "OTP FROM SCM...!!";
		String message = "<div style='1 px solid black; padding =20px'>" + "<h1>" + "YOUR OTP IS : " + otp + "</h1>"
				+ "</div>";
		String to = email_forgotpass; // this is the email entered by user for forgot the password

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {

//			store generated otp in session so we can verify it after user enter their otp
			session.setAttribute("session_otp", otp);
			session.setAttribute("session_email", email_forgotpass);

			return "verify_otp";
//			return "/verify_otp";
//			"/....." will not work when we deploy our app so dont provide it...

		} else {
			session.setAttribute("message", "Your otp not sent !! please check your email id  ");

			return "forgot_email_form";
//			return "/forgot_email_form";
		}

	}

//	verify otp handler
	
	@PostMapping("/verifyy_otp")
	public String verifyOtp(@RequestParam("user_filled_otp") Integer user_filled_otp,HttpSession session) {
		
		Integer myOtp=(Integer) session.getAttribute("session_otp");
		String  myEmail=(String) session.getAttribute("session_email");
		
		
		System.out.println("session otp "+myOtp);
		System.out.println("session email "+myEmail);
		System.out.println("user_filled_otp : "+user_filled_otp);
		
		if(myOtp.equals(user_filled_otp)) {
			//otp matched go to change password page
			
			User user = this.userRepository.getUserByUserName(myEmail);
			
			if(user == null) {
				//send error message
				
				session.setAttribute("message", "user does not exists with this email id...!  ");

				return "forgot_email_form";
//				return "/forgot_email_form";
				
			}else {
				//send change password form
				return "change_password";
//				return "/change_password";
			}
			
		}else {

			session.setAttribute("message", "YOu Entered Wrong Otp Please try Again...!!");
		return "verify_otp";
//		return "/verify_otp";
		}
	}
	
	
	
	
	
	
//	handler for /change-password from settings
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newpassword, HttpSession session) {
		
		String  myEmail=(String) session.getAttribute("session_email");

		User user = this.userRepository.getUserByUserName(myEmail);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		
		//save user after updating password
		this.userRepository.save(user);
		
		return "redirect:/custom_login?change=password changed successfully...!!";
	}
}
