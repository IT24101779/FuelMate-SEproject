package com.Vehicle.service.management.System.controller;

import com.Vehicle.service.management.System.dto.ServiceBookingDTO;
import com.Vehicle.service.management.System.entity.ServiceBooking;
import com.Vehicle.service.management.System.entity.ServiceBooking.BookingStatus;
import com.Vehicle.service.management.System.entity.ServiceBooking.Priority;
import com.Vehicle.service.management.System.entity.User;
import com.Vehicle.service.management.System.service.ServiceBookingService;
import com.Vehicle.service.management.System.service.UserService;
import com.Vehicle.service.management.System.repository.ServiceBookingRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/service-bookings")
public class ServiceBookingController {

    @Autowired
    private ServiceBookingService serviceBookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceBookingRepository serviceBookingRepository;

    @GetMapping
    public String serviceBookingDashboard(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (currentUser.getRole() == User.UserRole.CUSTOMER) {
                System.out.println("Customer accessing main dashboard - redirecting to customer page");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            List<ServiceBooking> serviceBookings;
            List<ServiceBooking> pendingBookings;
            List<ServiceBooking> activeBookings;

            switch (currentUser.getRole()) {
                case MECHANIC:
                    serviceBookings = serviceBookingService.getBookingsByMechanic(currentUser);
                    pendingBookings = serviceBookingService.getPendingBookings();
                    activeBookings = serviceBookingService.getActiveBookings().stream()
                            .filter(booking -> booking.getMechanic() != null &&
                                    booking.getMechanic().getId().equals(currentUser.getId()))
                            .toList();
                    break;
                default:
                    serviceBookings = serviceBookingService.getAllServiceBookings();
                    pendingBookings = serviceBookingService.getPendingBookings();
                    activeBookings = serviceBookingService.getActiveBookings();
                    break;
            }

            List<ServiceBooking> todaysBookings = serviceBookingService.getTodaysBookings();
            List<ServiceBooking> upcomingBookings = serviceBookingService.getUpcomingBookings();
            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();
            List<User> availableMechanics = serviceBookingService.getAvailableMechanics();

            model.addAttribute("pageTitle", "Service Booking Management - FuelMate");
            model.addAttribute("serviceBookings", serviceBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("activeBookings", activeBookings);
            model.addAttribute("todaysBookings", todaysBookings);
            model.addAttribute("upcomingBookings", upcomingBookings);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("availableMechanics", availableMechanics);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("bookingStatuses", BookingStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", currentUser);

            return "service-booking-dashboard";
        } catch (Exception e) {
            System.out.println("Error in service booking dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load service bookings: " + e.getMessage());
            return "service-booking-dashboard";
        }
    }

    @PostMapping("/add")
    public String addServiceBooking(@ModelAttribute ServiceBookingDTO serviceBookingDTO,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            Long customerId = currentUser.getId();

            if (serviceBookingDTO.getCustomerId() != null &&
                    (currentUser.getRole() == User.UserRole.ADMIN || currentUser.getRole() == User.UserRole.MANAGER)) {
                customerId = serviceBookingDTO.getCustomerId();
            }

            System.out.println("Creating service booking: " + serviceBookingDTO);

            ServiceBooking booking = serviceBookingService.createServiceBooking(serviceBookingDTO, customerId);
            redirectAttributes.addFlashAttribute("message",
                    "Service booking created successfully! Booking ID: " + booking.getId());
        } catch (Exception e) {
            System.out.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Failed to create service booking: " + e.getMessage());
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && currentUser.getRole() == User.UserRole.CUSTOMER) {
            return "redirect:/service-bookings/customer/my-bookings";
        }
        return "redirect:/service-bookings";
    }

    @PostMapping("/update/{id}")
    public String updateServiceBooking(@PathVariable("id") Long id,
                                       @ModelAttribute ServiceBookingDTO serviceBookingDTO,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating service booking ID: " + id + " with data: " + serviceBookingDTO);

            ServiceBooking updatedBooking = serviceBookingService.updateServiceBooking(id, serviceBookingDTO);
            redirectAttributes.addFlashAttribute("message",
                    "Service booking updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Failed to update service booking: " + e.getMessage());
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && currentUser.getRole() == User.UserRole.CUSTOMER) {
            return "redirect:/service-bookings/customer/my-bookings";
        }
        return "redirect:/service-bookings";
    }

    @RequestMapping(value = "/customer/update/{id}", method = {RequestMethod.POST})
    public String updateCustomerBooking(@PathVariable("id") Long id,
                                        HttpServletRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes,
                                        @RequestParam(value = "vehicleNumber", defaultValue = "") String vehicleNumber,
                                        @RequestParam(value = "vehicleMake", defaultValue = "") String vehicleMake,
                                        @RequestParam(value = "vehicleModel", defaultValue = "") String vehicleModel,
                                        @RequestParam(value = "vehicleYear", required = false) Integer vehicleYear,
                                        @RequestParam(value = "serviceType", defaultValue = "") String serviceType,
                                        @RequestParam(value = "scheduledDate", defaultValue = "") String scheduledDate,
                                        @RequestParam(value = "scheduledTime", defaultValue = "") String scheduledTime,
                                        @RequestParam(value = "priority", defaultValue = "MEDIUM") String priority,
                                        @RequestParam(value = "description", defaultValue = "") String description,
                                        @RequestParam(value = "customerNotes", defaultValue = "") String customerNotes,
                                        @RequestParam(value = "estimatedDuration", required = false) Integer estimatedDuration,
                                        @RequestParam(value = "estimatedCost", required = false) Double estimatedCost) {

        System.out.println("=== CUSTOMER UPDATE BOOKING ===");
        System.out.println("Booking ID: " + id);
        System.out.println("Vehicle: " + vehicleNumber + " " + vehicleMake + " " + vehicleModel);

        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                System.out.println("No user in session");
                return "redirect:/login";
            }

            if (currentUser.getRole() != User.UserRole.CUSTOMER) {
                System.out.println("Non-customer trying to access customer endpoint");
                redirectAttributes.addFlashAttribute("error", "Access denied.");
                return "redirect:/service-bookings";
            }

            ServiceBooking existingBooking = serviceBookingService.getServiceBookingById(id);
            if (existingBooking == null) {
                System.out.println("Booking not found: " + id);
                redirectAttributes.addFlashAttribute("error", "Booking not found.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            if (!existingBooking.getCustomer().getId().equals(currentUser.getId())) {
                System.out.println("Booking belongs to different customer");
                redirectAttributes.addFlashAttribute("error", "You can only edit your own bookings.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            if (existingBooking.getStatus() != BookingStatus.PENDING) {
                System.out.println("Booking status is not PENDING: " + existingBooking.getStatus());
                redirectAttributes.addFlashAttribute("error", "Only pending bookings can be edited.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            ServiceBookingDTO serviceBookingDTO = new ServiceBookingDTO();
            serviceBookingDTO.setVehicleNumber(vehicleNumber);
            serviceBookingDTO.setVehicleMake(vehicleMake);
            serviceBookingDTO.setVehicleModel(vehicleModel);
            serviceBookingDTO.setVehicleYear(vehicleYear);
            serviceBookingDTO.setServiceType(serviceType);
            serviceBookingDTO.setScheduledDate(scheduledDate);
            serviceBookingDTO.setScheduledTime(scheduledTime);
            serviceBookingDTO.setDescription(description);
            serviceBookingDTO.setCustomerNotes(customerNotes);
            serviceBookingDTO.setEstimatedDuration(estimatedDuration);
            serviceBookingDTO.setEstimatedCost(estimatedCost);

            if (!priority.isEmpty()) {
                try {
                    serviceBookingDTO.setPriority(Priority.valueOf(priority));
                } catch (IllegalArgumentException e) {
                    serviceBookingDTO.setPriority(Priority.MEDIUM);
                }
            }

            System.out.println("Updating booking...");
            ServiceBooking updatedBooking = serviceBookingService.updateServiceBooking(id, serviceBookingDTO);
            System.out.println("Booking updated successfully");

            redirectAttributes.addFlashAttribute("message", "Your booking has been updated successfully!");

        } catch (Exception e) {
            System.out.println("ERROR updating customer booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update booking: " + e.getMessage());
        }

        return "redirect:/service-bookings/customer/my-bookings";
    }

    @PostMapping("/customer/delete/{id}")
    public String deleteCustomerBooking(@PathVariable("id") Long id,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (currentUser.getRole() != User.UserRole.CUSTOMER) {
                redirectAttributes.addFlashAttribute("error", "Access denied.");
                return "redirect:/service-bookings";
            }

            ServiceBooking existingBooking = serviceBookingService.getServiceBookingById(id);
            if (existingBooking == null) {
                redirectAttributes.addFlashAttribute("error", "Booking not found.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            if (!existingBooking.getCustomer().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only cancel your own bookings.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            if (existingBooking.getStatus() != BookingStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only pending bookings can be cancelled.");
                return "redirect:/service-bookings/customer/my-bookings";
            }

            System.out.println("Customer cancelling booking ID: " + id);
            serviceBookingService.deleteServiceBooking(id);
            redirectAttributes.addFlashAttribute("message", "Your booking has been cancelled successfully!");

        } catch (Exception e) {
            System.out.println("Error cancelling customer booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to cancel booking: " + e.getMessage());
        }

        return "redirect:/service-bookings/customer/my-bookings";
    }

    @PostMapping("/assign-mechanic/{id}")
    public String assignMechanic(@PathVariable("id") Long id,
                                 @RequestParam Long mechanicId,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Assigning mechanic " + mechanicId + " to booking " + id);

            ServiceBooking updatedBooking = serviceBookingService.assignMechanic(id, mechanicId);
            redirectAttributes.addFlashAttribute("message", "Mechanic assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign mechanic: " + e.getMessage());
        }

        return "redirect:/service-bookings";
    }

    // ✅ UPDATED METHOD - THIS IS THE KEY FIX
    @PostMapping("/update-status/{id}")
    public String updateBookingStatus(@PathVariable("id") Long id,
                                      @RequestParam BookingStatus status,
                                      @RequestParam(value = "notes", required = false) String notes,
                                      @RequestParam(value = "actualCost", required = false) Double actualCost,
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        try {
            System.out.println("=== UPDATE BOOKING STATUS ===");
            System.out.println("Booking ID: " + id);
            System.out.println("New Status: " + status);
            System.out.println("Notes: " + (notes != null ? notes : "null"));
            System.out.println("Actual Cost: " + (actualCost != null ? actualCost : "null"));

            // Update the status first
            ServiceBooking updatedBooking = serviceBookingService.updateBookingStatus(id, status, notes);

            // ✅ FIX: Only update actual cost if provided AND greater than 0
            if (actualCost != null && actualCost > 0 && status == BookingStatus.COMPLETED) {
                System.out.println("Updating actual cost to: " + actualCost);
                ServiceBookingDTO updateDTO = new ServiceBookingDTO();
                updateDTO.setActualCost(actualCost);
                serviceBookingService.updateServiceBooking(id, updateDTO);
                redirectAttributes.addFlashAttribute("message",
                        "Task completed successfully! Final cost: $" + String.format("%.2f", actualCost));
            } else if (status == BookingStatus.COMPLETED) {
                // ✅ FIX: Better message when no cost is provided
                Double finalCost = updatedBooking.getActualCost();
                if (finalCost != null && finalCost > 0) {
                    redirectAttributes.addFlashAttribute("message",
                            "Task completed successfully! Cost: $" + String.format("%.2f", finalCost));
                } else {
                    redirectAttributes.addFlashAttribute("message",
                            "Task completed successfully! Cost will be finalized later.");
                }
            } else {
                redirectAttributes.addFlashAttribute("message",
                        "Booking status updated to " + status.name().replace("_", " ") + "!");
            }
        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Failed to update booking status: " + e.getMessage());
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            if (currentUser.getRole() == User.UserRole.MECHANIC) {
                return "redirect:/service-bookings/mechanic/my-tasks";
            } else if (currentUser.getRole() == User.UserRole.CUSTOMER) {
                return "redirect:/service-bookings/customer/my-bookings";
            }
        }
        return "redirect:/service-bookings";
    }

    @PostMapping("/delete/{id}")
    public String deleteServiceBooking(@PathVariable("id") Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Deleting service booking ID: " + id);
            serviceBookingService.deleteServiceBooking(id);
            redirectAttributes.addFlashAttribute("message", "Service booking deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete service booking: " + e.getMessage());
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && currentUser.getRole() == User.UserRole.CUSTOMER) {
            return "redirect:/service-bookings/customer/my-bookings";
        }
        return "redirect:/service-bookings";
    }

    @GetMapping("/status/{status}")
    public String getBookingsByStatus(@PathVariable String status, Model model, HttpSession session) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<ServiceBooking> bookings = serviceBookingService.getBookingsByStatus(bookingStatus);
            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();
            List<User> availableMechanics = serviceBookingService.getAvailableMechanics();

            model.addAttribute("pageTitle", status.replace("_", " ") + " Bookings - FuelMate");
            model.addAttribute("serviceBookings", bookings);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("availableMechanics", availableMechanics);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("bookingStatuses", BookingStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", (User) session.getAttribute("user"));

            return "service-booking-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load bookings: " + e.getMessage());
            return "service-booking-dashboard";
        }
    }

    @GetMapping("/pending")
    public String getPendingBookings(Model model, HttpSession session) {
        try {
            List<ServiceBooking> pendingBookings = serviceBookingService.getPendingBookings();
            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();
            List<User> availableMechanics = serviceBookingService.getAvailableMechanics();

            model.addAttribute("pageTitle", "Pending Bookings - FuelMate");
            model.addAttribute("serviceBookings", pendingBookings);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("availableMechanics", availableMechanics);
            model.addAttribute("showPending", true);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("bookingStatuses", BookingStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", (User) session.getAttribute("user"));

            return "service-booking-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load pending bookings: " + e.getMessage());
            return "service-booking-dashboard";
        }
    }

    @GetMapping("/today")
    public String getTodaysBookings(Model model, HttpSession session) {
        try {
            List<ServiceBooking> todaysBookings = serviceBookingService.getTodaysBookings();
            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();
            List<User> availableMechanics = serviceBookingService.getAvailableMechanics();

            model.addAttribute("pageTitle", "Today's Bookings - FuelMate");
            model.addAttribute("serviceBookings", todaysBookings);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("availableMechanics", availableMechanics);
            model.addAttribute("showToday", true);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("bookingStatuses", BookingStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", (User) session.getAttribute("user"));

            return "service-booking-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load today's bookings: " + e.getMessage());
            return "service-booking-dashboard";
        }
    }

    @PostMapping("/search")
    public String searchBookings(@RequestParam String searchTerm, Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (currentUser.getRole() == User.UserRole.CUSTOMER) {
                return "redirect:/service-bookings/customer/my-bookings?search=" + searchTerm;
            }

            List<ServiceBooking> searchResults = serviceBookingService.searchBookings(searchTerm);
            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();
            List<User> availableMechanics = serviceBookingService.getAvailableMechanics();

            model.addAttribute("pageTitle", "Search Results - FuelMate");
            model.addAttribute("serviceBookings", searchResults);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("availableMechanics", availableMechanics);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("bookingStatuses", BookingStatus.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", currentUser);

            return "service-booking-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to search bookings: " + e.getMessage());
            return "service-booking-dashboard";
        }
    }

    @GetMapping("/customer/my-bookings")
    public String getCustomerBookings(@RequestParam(value = "search", required = false) String searchTerm,
                                      Model model,
                                      HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");

            System.out.println("=== CUSTOMER BOOKINGS PAGE ===");
            System.out.println("User in session: " + (currentUser != null ? currentUser.getEmail() : "null"));

            if (currentUser == null) {
                System.out.println("No user in session, redirecting to login");
                return "redirect:/login";
            }

            if (currentUser.getRole() != User.UserRole.CUSTOMER) {
                System.out.println("Non-customer user " + currentUser.getRole() + " tried to access customer bookings");
                return "redirect:/service-bookings";
            }

            System.out.println("Loading customer bookings for: " + currentUser.getEmail());

            List<ServiceBooking> customerBookings;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                System.out.println("Filtering bookings with search term: " + searchTerm);
                customerBookings = serviceBookingService.getBookingsByCustomer(currentUser).stream()
                        .filter(booking ->
                                booking.getVehicleNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                        booking.getServiceType().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                        booking.getVehicleMake().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                        booking.getVehicleModel().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                model.addAttribute("searchTerm", searchTerm);
            } else {
                customerBookings = serviceBookingService.getBookingsByCustomer(currentUser);
            }

            List<String> serviceTypes = serviceBookingService.getAllServiceTypes();

            System.out.println("Found " + customerBookings.size() + " bookings for customer");

            model.addAttribute("pageTitle", "My Bookings - FuelMate");
            model.addAttribute("serviceBookings", customerBookings);
            model.addAttribute("serviceTypes", serviceTypes);
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("currentUser", currentUser);

            System.out.println("Returning customer-bookings template");
            return "customer-bookings";

        } catch (Exception e) {
            System.out.println("ERROR in customer bookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load your bookings: " + e.getMessage());
            model.addAttribute("serviceBookings", new ArrayList<>());
            model.addAttribute("serviceTypes", new ArrayList<>());
            model.addAttribute("serviceBookingDTO", new ServiceBookingDTO());
            model.addAttribute("priorities", Priority.values());
            return "customer-bookings";
        }
    }

    @GetMapping("/mechanic/my-tasks")
    public String getMechanicTasks(Model model, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null || currentUser.getRole() != User.UserRole.MECHANIC) {
                return "redirect:/login";
            }

            List<ServiceBooking> mechanicBookings = serviceBookingService.getBookingsByMechanic(currentUser);
            List<ServiceBooking> pendingAssignments = serviceBookingService.getPendingBookings();

            model.addAttribute("pageTitle", "My Tasks - FuelMate");
            model.addAttribute("serviceBookings", mechanicBookings);
            model.addAttribute("pendingBookings", pendingAssignments);
            model.addAttribute("bookingStatuses", Arrays.asList(BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED, BookingStatus.CANCELLED));
            model.addAttribute("currentUser", currentUser);

            return "mechanic-tasks";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your tasks: " + e.getMessage());
            return "mechanic-tasks";
        }
    }

    @GetMapping("/api/available-timeslots")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvailableTimeSlots(
            @RequestParam String date,
            @RequestParam(required = false) Long excludeBookingId) {

        System.out.println("=== TIMESLOT API CALLED ===");
        System.out.println("Date: " + date + ", Exclude: " + excludeBookingId);

        try {
            LocalDate selectedDate = LocalDate.parse(date);

            if (selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Closed on Sundays");
                response.put("availableSlots", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            List<String> allTimeSlots = generateBusinessHourSlots(selectedDate);
            List<String> occupiedSlots = getOccupiedSlotsForDate(selectedDate, excludeBookingId);

            List<String> availableSlots = allTimeSlots.stream()
                    .filter(slot -> !occupiedSlots.contains(slot))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("availableSlots", availableSlots);
            response.put("occupiedSlots", occupiedSlots);
            response.put("businessHours", getBusinessHours(selectedDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Error in timeslot API: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to load time slots");
            response.put("availableSlots", new ArrayList<>());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private List<String> generateBusinessHourSlots(LocalDate date) {
        List<String> slots = new ArrayList<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        int startHour, endHour;

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            startHour = 9;
            endHour = 16;
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return slots;
        } else {
            startHour = 8;
            endHour = 18;
        }

        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                slots.add(String.format("%02d:%02d", hour, minute));
            }
        }

        return slots;
    }

    private List<String> getOccupiedSlotsForDate(LocalDate date, Long excludeBookingId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<ServiceBooking> bookingsForDate = serviceBookingRepository
                .findByScheduledDateTimeBetweenOrderByScheduledDateTimeAsc(startOfDay, endOfDay);

        return bookingsForDate.stream()
                .filter(booking -> excludeBookingId == null || !booking.getId().equals(excludeBookingId))
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .map(booking -> booking.getScheduledDateTime().toLocalTime().toString().substring(0, 5))
                .collect(Collectors.toList());
    }

    private Map<String, String> getBusinessHours(LocalDate date) {
        Map<String, String> hours = new HashMap<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                hours.put("open", "08:00");
                hours.put("close", "18:00");
                hours.put("status", "open");
                break;
            case SATURDAY:
                hours.put("open", "09:00");
                hours.put("close", "16:00");
                hours.put("status", "open");
                break;
            case SUNDAY:
                hours.put("open", "");
                hours.put("close", "");
                hours.put("status", "closed");
                break;
        }

        return hours;
    }

    @GetMapping("/api/test")
    @ResponseBody
    public Map<String, String> testAPI() {
        System.out.println("=== TEST API CALLED ===");
        Map<String, String> response = new HashMap<>();
        response.put("status", "working");
        response.put("message", "API is accessible");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}