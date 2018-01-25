package com.project.Authentication.controllers;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.Authentication.models.UserModel;
import com.project.Authentication.repositories.*;
import com.project.Authentication.services.UserService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class UserControl{
	//Member variables go here
	private UserService userService;
	private BCryptPasswordEncoder bcrypt;

	public UserControl(UserService userService){
		this.userService = userService;
		this.bcrypt = new BCryptPasswordEncoder();

	}

	public void setAtt(String key, Object object, HttpSession session){
		session.setAttribute(key, object);
	}

	public Object getAtt(String key, HttpSession session){
		return session.getAttribute(key);
	}

	@RequestMapping("adminDash")
	public String adminDash(HttpSession session, Model model, UserModel user){
		UserModel loggedInUser = (UserModel) session.getAttribute("current_user");
		if(session.getAttribute("current_user")!=null && loggedInUser.isAdmin()){
			List<UserModel> users = userService.all();
			model.addAttribute("users", users);
			return "adminDash";
		}else{
			return "redirect:/errorPage";
		}

	}

	@RequestMapping("dashboard")
	public String dashboard(HttpSession session, Model model){
		if(session.getAttribute("current_user") !=null){
			return "dashboard";
		}else{
			return "redirect:/";
		}
	}

	@PostMapping("/login")
	public String login(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session){
		UserModel user = userService.findByEmail(email);

		if(user == null){
			return "redirect:/";
		}else if(user.isAdmin() == true && userService.isMatch(password, user.getPassword())){
			session.setAttribute("current_user", user);
			return "redirect:adminDash";
		}else{
			if(userService.isMatch(password, user.getPassword())){
				session.setAttribute("current_user", user);//STORES THE PERSON WHO JUST LOGGED IN INTO SESSION, THERE IS NOW NO NEED TO REACH BACK INTO SERVICES FOR LOGGED IN USER INFO
				return "redirect:/dashboard";
			}else{
				return "redirect:/";

			}
		}
	}

	@RequestMapping("/register")
	public String register(@ModelAttribute("user") UserModel user, HttpSession session){
		session.setAttribute("current_user", null);
		return "register";
	}

	@PostMapping("/register")
	public String create(@Valid @ModelAttribute("user") UserModel user, BindingResult result, HttpSession session){
		if(result.hasErrors()){
			return "register";
		}else{
			if(userService.all().size() == 0 ){
				user.setAdmin(true);
			}
			userService.create(user);
			session.setAttribute("current_user", user);
		
			return "redirect:/dashboard";
		}
	}
	
	@RequestMapping("/logout")
	public String logout(HttpSession session){
		session.setAttribute("current_user", null);
		return "redirect:/register";
	}
	@RequestMapping("/delete/{id}")
	public String delete(@PathVariable("id") long id, HttpSession session){
		UserModel loggedInUser = (UserModel) session.getAttribute("current_user");
		if(session.getAttribute("current_user")!=null && loggedInUser.isAdmin()){
			userService.destroy(id);
			return "redirect:/adminDash";
		}else{
			return "redirect:/errorPage";
		}
	}	
	@RequestMapping("/edit/{id}")
	public String edit(@PathVariable("id")long id, Model model, HttpSession session){
		UserModel loggedInUser = (UserModel) session.getAttribute("current_user");
		if(session.getAttribute("current_user")!=null && loggedInUser.isAdmin()){
			UserModel user = userService.findById(id);
			model.addAttribute("user", user);
			System.out.println(user.getPassword());
			return "edit";
		}else{
			return "redirect:/errorPage";
		}
	}

	@RequestMapping("/adminDash/create")
	public String adminCreateForm(@ModelAttribute("user") UserModel user,HttpSession session){
		UserModel loggedInUser = (UserModel) session.getAttribute("current_user");
		if(session.getAttribute("current_user")!=null && loggedInUser.isAdmin()){
			return "adminCreate";
		}else{
			return "redirect:/errorPage";
		}
	}
	@PostMapping("/adminDash/create")
	public String adminCreateSubmit(@Valid @ModelAttribute("user") UserModel newUser, BindingResult result, @RequestParam ("admin") boolean admin){
		if(result.hasErrors()){
			return "adminCreate";
		}else{
			newUser.setAdmin(admin);
			userService.create(newUser);
			System.out.println("test");
			return "redirect:/adminDash";
	}
}
	@PostMapping("/edit/{id}/update")
	public String update(
		@PathVariable("id") long id,
		@RequestParam("firstName") String firstName, 
		@RequestParam("lastName") String lastName, 
		@RequestParam("email")String email,
		@RequestParam("admin") boolean admin
		){
		UserModel user = userService.findById(id);
		System.out.println(user.getPassword());
		user.setAdmin(admin);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		// String password = user.getPassword();
		System.out.println("BEFORE PASSWORD");
		user.setPassword(user.getPassword());
		System.out.println("AFTER PASSWORD");
		userService.update(user);
		return "redirect:/adminDash";
	}
}

