package com.smart.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import javassist.NotFoundException;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

//	commomData it is used to get user for all the methods and it will available in whole controller
	@ModelAttribute
	public void commonData(Model model, Principal principal) {

		String name = principal.getName();

		User user = userRepository.getUserByUserName(name);

		model.addAttribute("user", user);

	}

//	dashboard home
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User DashBoard - Smart Contact Manager");

		return "normal/user_dashboard";
	}

	// open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "add-contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/addContactForm";
	}

	// proccessing add contact form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principle, HttpSession session) {

		try {

			String name = principle.getName();

			User user = this.userRepository.getUserByUserName(name);
			contact.setUser(user);

//			processing and uploading file

			if (file.isEmpty()) {

				// message if file empty
				System.out.println("Your File is Empty...");
				contact.setImage("contact.png");

			} else {

				// add file to the folder and update the contact
				String originalFilename = file.getOriginalFilename();

				String randomId = UUID.randomUUID().toString();
				String renamed_originalFilename = randomId
						.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));

				contact.setImage(renamed_originalFilename);

				File saveFile = new ClassPathResource("static/image").getFile().getAbsoluteFile();

				Path path = Paths.get(saveFile + File.separator + renamed_originalFilename);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Uploaded...");

			}

			user.getContacts().add(contact);

			this.userRepository.save(user);

			// success message
			session.setAttribute("message",
					new Message("Your Contact is Added Successfully !! Add MOre.. ", "success"));

//			System.out.println("Added to database");

		} catch (Exception e) {
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Your Contact is  not Added !! Plz try Again... ", "danger"));

		}

		return "normal/addContactForm";
	}

	// show contact handler
	// pagination per page 2 current page 0
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "view-contact - Smart Contact Manager");

		// using contact Repository
		String Username = principal.getName();
		User user = this.userRepository.getUserByUserName(Username);
		// current page =page
		// contact per page =2
		Pageable pageable = PageRequest.of(page, 2);

		Page<Contact> contacts = this.contactRepository.findContactsByuser(user.getId(), pageable);

//		if (!contacts.isEmpty()) {
//			System.out.println("yes it it not empty ");
//		}

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/showContacts";
	}

	// show particular contact
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal,
			@ModelAttribute Contact contact1) {

		model.addAttribute("title ", contact1.getName() + "Profile - Smart Contact Manager");

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		// only login user can access the data or contact
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contactDetails";
	}

	
	
	
	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session,
			Principal principal) {

		// from contact repo find contactOptional using id and then we get actual
		// contact from that now we can delete it
		Contact contact = this.contactRepository.findById(cId).get();

		// we set user as null bcz contact is associated with user so it can't delete so
		// we set it to null
		// this line not required for me bt saved for reference --->
		// contact.setUser(null);

		// for matching id of user and the contact which we are deleting
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		try {

//			for delete Image profile photo...
			try {

				File deleteImage = new ClassPathResource("static/image").getFile();

				File file = new File(deleteImage, contact.getImage());
				file.delete();
//				System.out.println("file also deleted successfully...");

			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("message", new Message("Error Occured Image Not deleted..!!", "danger"));

			}

			if (user.getId() == contact.getUser().getId()) {

				contactRepository.delete(contact);

				session.setAttribute("message", new Message("Contact Deleted Successfully..!!", "success"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error Occured Contact not Deleted ..!!", "danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	
	
	
	// delete user account permanently

	@GetMapping("/delete_user")
	public String deleteUser(Model model, HttpSession session, Principal principal) {

		try {

			model.addAttribute("title","deleted Account - Smart Contact Manager");

			
//				getting user who logged in...
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

//			System.out.println(1/0);
			
//			code for delete user account permanently
			userRepository.delete(user);
			


		} catch (Exception e) {
			
			e.printStackTrace();
			
//			if account not deleted it will shows error in setting page with session method 
			session.setAttribute("message", new Message("Something Went Wrong...Your Account is  not deleted !! Plz try Again... ", "danger"));

			return "redirect:/user/settings";
			
//			 and below is with param method and successfull deleted account message will be shown with param 
//			return "redirect:/user/settings?notdeleted=Error Occured Account not Deleted ..!!";

		}

//         redirect to login page after delete user account and shows deleted message on the login page ( with param method )
		return "redirect:/custom_login?deleted=Your AccounT DeleTed successfully... You Can close this Tab Now...!! ";
	}

	
	
	
	
	
	
	
	
	// open update contact form handler
	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Model model) {

		model.addAttribute("title ", "update Contact - Smart Contact Manager");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/update_contact";
	}

	// process update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String processUpdateContact(@RequestParam("profile_Update-Image") MultipartFile file,
			@ModelAttribute Contact contact, Principal principle, HttpSession session, Model model) {

		try {

//			
//			find old user contact details it is showing no value have to fix this...

//			User oldContactDetails = this.userRepository.findById(contact.getcId()).get();

			// image or file working in update contact case
			if (!file.isEmpty()) {
//				
//				delete old photo

//				File deleteFile = new ClassPathResource("static/image").getFile();

//				File file1=new File(deleteFile);
////				
//				file1.delete();

//				update new photo
//				//here tried to give unique name to the profile picture
				String originalFilename = file.getOriginalFilename();

				String randomId = UUID.randomUUID().toString();
				String renamed_originalFilename = randomId
						.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));

				File saveFile = new ClassPathResource("static/image").getFile().getAbsoluteFile();

				Path path = Paths.get(saveFile + File.separator + renamed_originalFilename);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(renamed_originalFilename);

			} else {

//				tried something new not working

				contact.setImage(contact.getImage());

			}

			// this below two lines are important if not used it will delete contact in
			// place of update and only this lines updates all data except image file...
			User user = this.userRepository.getUserByUserName(principle.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your Contact is Updated Successfully", "success"));

		} catch (Exception e) {
//			System.out.println("in catch block...");
			e.printStackTrace();
		}

		return "redirect:/user/" + contact.getcId() + "/contact/";
	}

//	handler for your profile option
	@GetMapping("/your-profile")
	public String yourProfile(@ModelAttribute Contact contact, Model model) {

		model.addAttribute("title", "Your Profile - Smart Contact Manager");

		return "normal/your_profile";
	}

	// setting handler
	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler
	@PostMapping("/change-password")
	public String changePassword(Model model, @RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		model.addAttribute("title", "Change Password - Smart Contact Manager");

//		System.out.println("old pass " + oldPassword);
//		System.out.println("new pass " + newPassword);

		String userName = principal.getName();

		User currentUser = this.userRepository.getUserByUserName(userName);

		if (bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);

			session.setAttribute("message", new Message("Password Changed Successfully..!!", "success"));

		} else {
			// wrong old pass not change
			session.setAttribute("message", new Message("PLease Enter Correct old Password..!!", "danger"));

			return "redirect:/user/settings";
		}

		return "redirect:/user/index";

	}

}
