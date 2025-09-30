package com.Vehicle.service.management.System.controller;

import com.Vehicle.service.management.System.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "FuelMate - Vehicle Service & Fuel Management");
        return "index";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us - FuelMate");
        return "contact";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us - FuelMate");
        return "about";
    }

    @GetMapping("/inventory-demo")
    public String inventoryDemo(Model model) {
        model.addAttribute("pageTitle", "Inventory Demo - FuelMate");
        return "redirect:/inventory";
    }

    // Add dashboard placeholders for different user roles
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session, redirecting to login");
                return "redirect:/login";
            }
            System.out.println("Admin dashboard accessed by: " + currentUser.getEmail());
            model.addAttribute("pageTitle", "Admin Dashboard - FuelMate");
            return "admin-dashboard";
        } catch (Exception e) {
            System.out.println("Error in admin dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session, redirecting to login");
                return "redirect:/login";
            }
            System.out.println("Manager dashboard accessed by: " + currentUser.getEmail());
            model.addAttribute("pageTitle", "Manager Dashboard - FuelMate");
            return "manager-dashboard";
        } catch (Exception e) {
            System.out.println("Error in manager dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/mechanic/dashboard")
    public String mechanicDashboard(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session for mechanic dashboard, redirecting to login");
                return "redirect:/login";
            }
            if (currentUser.getRole() != User.UserRole.MECHANIC) {
                System.out.println("Non-mechanic user trying to access mechanic dashboard: " + currentUser.getRole());
                return "redirect:/login";
            }
            System.out.println("Mechanic dashboard accessed by: " + currentUser.getEmail());
            model.addAttribute("pageTitle", "Mechanic Dashboard - FuelMate");
            return "mechanic-dashboard";
        } catch (Exception e) {
            System.out.println("Error in mechanic dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/attendant/dashboard")
    public String attendantDashboard(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session, redirecting to login");
                return "redirect:/login";
            }
            System.out.println("Attendant dashboard accessed by: " + currentUser.getEmail());
            model.addAttribute("pageTitle", "Attendant Dashboard - FuelMate");
            return "attendant-dashboard";
        } catch (Exception e) {
            System.out.println("Error in attendant dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        try {
            System.out.println("Customer dashboard accessed");
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session for customer dashboard, redirecting to login");
                return "redirect:/login";
            }
            if (currentUser.getRole() != User.UserRole.CUSTOMER) {
                System.out.println("Non-customer user trying to access customer dashboard: " + currentUser.getRole());
                return "redirect:/login";
            }
            System.out.println("Customer dashboard - redirecting to bookings for user: " + currentUser.getEmail());
            model.addAttribute("pageTitle", "Customer Dashboard - FuelMate");

            // Instead of returning a template, redirect directly to customer bookings
            return "redirect:/service-bookings/customer/my-bookings";
        } catch (Exception e) {
            System.out.println("Error in customer dashboard: " + e.getMessage());
            e.printStackTrace();
            // In case of error, redirect to a safe page
            return "redirect:/service-bookings/customer/my-bookings";
        }
    }
}