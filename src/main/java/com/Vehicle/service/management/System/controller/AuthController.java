package com.Vehicle.service.management.System.controller;

import com.Vehicle.service.management.System.dto.LoginRequest;
import com.Vehicle.service.management.System.dto.RegisterRequest;
import com.Vehicle.service.management.System.entity.User;
import com.Vehicle.service.management.System.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("pageTitle", "Register - FuelMate");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest registerRequest,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate that user is not trying to register as Admin or Mechanic
            if (registerRequest.getRole() == User.UserRole.ADMIN ||
                    registerRequest.getRole() == User.UserRole.MECHANIC) {
                redirectAttributes.addFlashAttribute("error",
                        "Admin and Mechanic accounts cannot be created through registration.");
                return "redirect:/register";
            }

            User user = userService.registerUser(registerRequest);
            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("pageTitle", "Login - FuelMate");
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequest loginRequest,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Login attempt for: " + loginRequest.getEmail());
            User user = userService.loginUser(loginRequest);
            System.out.println("User logged in: " + user.getEmail() + " with role: " + user.getRole());

            // Store user in session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("userName", user.getFirstName() + " " + user.getLastName());

            System.out.println("Session attributes set for user: " + user.getFullName());

            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    System.out.println("Redirecting admin to dashboard");
                    return "redirect:/admin/dashboard";
                case MANAGER:
                    System.out.println("Redirecting manager to dashboard");
                    return "redirect:/manager/dashboard";
                case MECHANIC:
                    System.out.println("Redirecting mechanic to dashboard");
                    return "redirect:/mechanic/dashboard";
                case FUEL_ATTENDANT:
                    System.out.println("Redirecting attendant to dashboard");
                    return "redirect:/attendant/dashboard";
                case CUSTOMER:
                    System.out.println("Redirecting customer directly to bookings");
                    return "redirect:/service-bookings/customer/my-bookings";
                default:
                    System.out.println("Unknown role, redirecting to customer bookings");
                    return "redirect:/service-bookings/customer/my-bookings";
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
            return "redirect:/login";
        }
    }
}