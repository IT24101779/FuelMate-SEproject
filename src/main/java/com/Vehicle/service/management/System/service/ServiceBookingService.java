package com.Vehicle.service.management.System.service;

import com.Vehicle.service.management.System.dto.ServiceBookingDTO;
import com.Vehicle.service.management.System.entity.ServiceBooking;
import com.Vehicle.service.management.System.entity.ServiceBooking.BookingStatus;
import com.Vehicle.service.management.System.entity.ServiceBooking.Priority;
import com.Vehicle.service.management.System.entity.User;
import com.Vehicle.service.management.System.repository.ServiceBookingRepository;
import com.Vehicle.service.management.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ServiceBookingService {

    @Autowired
    private ServiceBookingRepository serviceBookingRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ServiceBooking> getAllServiceBookings() {
        return serviceBookingRepository.findAll();
    }

    public ServiceBooking getServiceBookingById(Long id) {
        return serviceBookingRepository.findById(id).orElse(null);
    }

    public ServiceBooking createServiceBooking(ServiceBookingDTO serviceBookingDTO, Long customerId) {
        try {
            User customer = userRepository.findById(customerId).orElse(null);
            if (customer == null) {
                throw new RuntimeException("Customer not found");
            }

            LocalDateTime scheduledDateTime = parseScheduledDateTime(
                    serviceBookingDTO.getScheduledDate(),
                    serviceBookingDTO.getScheduledTime()
            );

            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Cannot schedule service in the past");
            }

            if (serviceBookingRepository.hasActiveBookingForVehicle(serviceBookingDTO.getVehicleNumber())) {
                throw new RuntimeException("This vehicle already has an active service booking");
            }

            ServiceBooking serviceBooking = new ServiceBooking();
            serviceBooking.setCustomer(customer);
            serviceBooking.setVehicleNumber(serviceBookingDTO.getVehicleNumber());
            serviceBooking.setVehicleMake(serviceBookingDTO.getVehicleMake());
            serviceBooking.setVehicleModel(serviceBookingDTO.getVehicleModel());
            serviceBooking.setVehicleYear(serviceBookingDTO.getVehicleYear());
            serviceBooking.setServiceType(serviceBookingDTO.getServiceType());
            serviceBooking.setDescription(serviceBookingDTO.getDescription());
            serviceBooking.setScheduledDateTime(scheduledDateTime);
            serviceBooking.setEstimatedDuration(serviceBookingDTO.getEstimatedDuration());
            serviceBooking.setEstimatedCost(serviceBookingDTO.getEstimatedCost());
            serviceBooking.setPriority(serviceBookingDTO.getPriority() != null ?
                    serviceBookingDTO.getPriority() : Priority.MEDIUM);
            serviceBooking.setCustomerNotes(serviceBookingDTO.getCustomerNotes());
            serviceBooking.setStatus(BookingStatus.PENDING);

            return serviceBookingRepository.save(serviceBooking);
        } catch (Exception e) {
            System.out.println("Error creating service booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create service booking: " + e.getMessage());
        }
    }

    public ServiceBooking updateServiceBooking(Long id, ServiceBookingDTO serviceBookingDTO) {
        try {
            ServiceBooking existingBooking = serviceBookingRepository.findById(id).orElse(null);
            if (existingBooking == null) {
                throw new RuntimeException("Service booking not found");
            }

            if (serviceBookingDTO.getVehicleNumber() != null) {
                existingBooking.setVehicleNumber(serviceBookingDTO.getVehicleNumber());
            }
            if (serviceBookingDTO.getVehicleMake() != null) {
                existingBooking.setVehicleMake(serviceBookingDTO.getVehicleMake());
            }
            if (serviceBookingDTO.getVehicleModel() != null) {
                existingBooking.setVehicleModel(serviceBookingDTO.getVehicleModel());
            }
            if (serviceBookingDTO.getVehicleYear() != null) {
                existingBooking.setVehicleYear(serviceBookingDTO.getVehicleYear());
            }
            if (serviceBookingDTO.getServiceType() != null) {
                existingBooking.setServiceType(serviceBookingDTO.getServiceType());
            }
            if (serviceBookingDTO.getDescription() != null) {
                existingBooking.setDescription(serviceBookingDTO.getDescription());
            }
            if (serviceBookingDTO.getEstimatedDuration() != null) {
                existingBooking.setEstimatedDuration(serviceBookingDTO.getEstimatedDuration());
            }
            if (serviceBookingDTO.getEstimatedCost() != null) {
                existingBooking.setEstimatedCost(serviceBookingDTO.getEstimatedCost());
            }
            if (serviceBookingDTO.getActualCost() != null) {
                existingBooking.setActualCost(serviceBookingDTO.getActualCost());
            }
            if (serviceBookingDTO.getPriority() != null) {
                existingBooking.setPriority(serviceBookingDTO.getPriority());
            }
            if (serviceBookingDTO.getStatus() != null) {
                existingBooking.setStatus(serviceBookingDTO.getStatus());
            }

            if (serviceBookingDTO.getScheduledDate() != null && serviceBookingDTO.getScheduledTime() != null) {
                LocalDateTime newScheduledDateTime = parseScheduledDateTime(
                        serviceBookingDTO.getScheduledDate(),
                        serviceBookingDTO.getScheduledTime()
                );
                existingBooking.setScheduledDateTime(newScheduledDateTime);
            }

            if (serviceBookingDTO.getCustomerNotes() != null) {
                existingBooking.setCustomerNotes(serviceBookingDTO.getCustomerNotes());
            }
            if (serviceBookingDTO.getMechanicNotes() != null) {
                existingBooking.setMechanicNotes(serviceBookingDTO.getMechanicNotes());
            }
            if (serviceBookingDTO.getCompletionNotes() != null) {
                existingBooking.setCompletionNotes(serviceBookingDTO.getCompletionNotes());
            }

            return serviceBookingRepository.save(existingBooking);
        } catch (Exception e) {
            System.out.println("Error updating service booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update service booking: " + e.getMessage());
        }
    }

    public ServiceBooking assignMechanic(Long bookingId, Long mechanicId) {
        try {
            ServiceBooking booking = serviceBookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                throw new RuntimeException("Service booking not found");
            }

            User mechanic = userRepository.findById(mechanicId).orElse(null);
            if (mechanic == null || mechanic.getRole() != User.UserRole.MECHANIC) {
                throw new RuntimeException("Valid mechanic not found");
            }

            List<ServiceBooking> conflicts = serviceBookingRepository.findConflictingBookings(
                    mechanic,
                    booking.getScheduledDateTime(),
                    booking.getScheduledDateTime().plusMinutes(booking.getEstimatedDuration() != null ? booking.getEstimatedDuration() : 60)
            );

            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Mechanic has conflicting bookings at this time");
            }

            booking.setMechanic(mechanic);
            booking.setStatus(BookingStatus.ASSIGNED);

            return serviceBookingRepository.save(booking);
        } catch (Exception e) {
            System.out.println("Error assigning mechanic: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to assign mechanic: " + e.getMessage());
        }
    }

    // ✅ FIXED: Better handling of status updates with proper cost management
    public ServiceBooking updateBookingStatus(Long bookingId, BookingStatus status, String notes) {
        try {
            ServiceBooking booking = serviceBookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                throw new RuntimeException("Service booking not found");
            }

            if (!isValidStatusTransition(booking.getStatus(), status)) {
                throw new RuntimeException("Invalid status transition from " + booking.getStatus() + " to " + status);
            }

            booking.setStatus(status);

            switch (status) {
                case IN_PROGRESS:
                    if (notes != null && !notes.trim().isEmpty()) {
                        booking.setMechanicNotes(notes);
                    }
                    break;

                case COMPLETED:
                    if (notes != null && !notes.trim().isEmpty()) {
                        booking.setCompletionNotes(notes);
                    }
                    // ✅ FIX: Auto-set actual cost to estimated cost if not already set
                    if (booking.getActualCost() == null && booking.getEstimatedCost() != null) {
                        booking.setActualCost(booking.getEstimatedCost());
                        System.out.println("Auto-setting actual cost to estimated cost: " + booking.getEstimatedCost());
                    }
                    break;

                case CANCELLED:
                    if (notes != null && !notes.trim().isEmpty()) {
                        booking.setMechanicNotes(notes);
                    }
                    break;
            }

            return serviceBookingRepository.save(booking);
        } catch (Exception e) {
            System.out.println("Error updating booking status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update booking status: " + e.getMessage());
        }
    }

    public void deleteServiceBooking(Long id) {
        try {
            ServiceBooking booking = serviceBookingRepository.findById(id).orElse(null);
            if (booking == null) {
                throw new RuntimeException("Service booking not found");
            }

            if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
                throw new RuntimeException("Cannot delete bookings that are in progress or completed");
            }

            serviceBookingRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("Error deleting service booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete service booking: " + e.getMessage());
        }
    }

    public List<ServiceBooking> getBookingsByCustomer(User customer) {
        return serviceBookingRepository.findByCustomerOrderByScheduledDateTimeDesc(customer);
    }

    public List<ServiceBooking> getBookingsByMechanic(User mechanic) {
        return serviceBookingRepository.findByMechanicOrderByScheduledDateTimeAsc(mechanic);
    }

    public List<ServiceBooking> getBookingsByStatus(BookingStatus status) {
        return serviceBookingRepository.findByStatusOrderByScheduledDateTimeAsc(status);
    }

    public List<ServiceBooking> getPendingBookings() {
        return serviceBookingRepository.findPendingBookings();
    }

    public List<ServiceBooking> getActiveBookings() {
        return serviceBookingRepository.findActiveBookings();
    }

    public List<ServiceBooking> getTodaysBookings() {
        return serviceBookingRepository.findTodayBookings(LocalDateTime.now());
    }

    public List<ServiceBooking> getUpcomingBookings() {
        return serviceBookingRepository.findUpcomingBookings(LocalDateTime.now());
    }

    public List<ServiceBooking> searchBookings(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllServiceBookings();
        }
        return serviceBookingRepository.searchBookings(searchTerm.trim());
    }

    public List<String> getAllServiceTypes() {
        return serviceBookingRepository.findAllServiceTypes();
    }

    public List<User> getAvailableMechanics() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.MECHANIC)
                .toList();
    }

    public List<Object[]> getBookingStatusStatistics() {
        return serviceBookingRepository.getBookingStatusStatistics();
    }

    public boolean isMechanicAvailable(User mechanic, LocalDateTime startTime, Integer durationMinutes) {
        if (mechanic == null || startTime == null) {
            return false;
        }
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes != null ? durationMinutes : 60);
        List<ServiceBooking> conflicts = serviceBookingRepository.findConflictingBookings(mechanic, startTime, endTime);
        return conflicts.isEmpty();
    }

    public User autoAssignMechanic(LocalDateTime scheduledTime, Integer durationMinutes) {
        List<User> availableMechanics = getAvailableMechanics();
        for (User mechanic : availableMechanics) {
            if (isMechanicAvailable(mechanic, scheduledTime, durationMinutes)) {
                return mechanic;
            }
        }
        return null;
    }

    private LocalDateTime parseScheduledDateTime(String date, String time) {
        try {
            if (date == null || time == null) {
                throw new RuntimeException("Date and time are required");
            }
            String dateTimeString = date + " " + time;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date or time format. Use yyyy-MM-dd for date and HH:mm for time");
        }
    }

    private boolean isValidStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.CANCELLED).contains(newStatus);
            case CONFIRMED:
                return Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.CANCELLED, BookingStatus.NO_SHOW).contains(newStatus);
            case ASSIGNED:
                return Arrays.asList(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED, BookingStatus.NO_SHOW).contains(newStatus);
            case IN_PROGRESS:
                return Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED).contains(newStatus);
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                return false;
            default:
                return false;
        }
    }

    public List<ServiceBooking> getRecentBookings() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return serviceBookingRepository.findRecentBookings(sevenDaysAgo);
    }

    public List<ServiceBooking> getOverdueBookings() {
        return serviceBookingRepository.findOverdueBookings(LocalDateTime.now());
    }
}