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
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

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

    // ✅ ENHANCED: Auto-assign mechanic when customer creates booking
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
            serviceBooking.setEstimatedDuration(serviceBookingDTO.getEstimatedDuration() != null ?
                    serviceBookingDTO.getEstimatedDuration() : 60);
            serviceBooking.setEstimatedCost(serviceBookingDTO.getEstimatedCost());
            serviceBooking.setPriority(serviceBookingDTO.getPriority() != null ?
                    serviceBookingDTO.getPriority() : Priority.MEDIUM);
            serviceBooking.setCustomerNotes(serviceBookingDTO.getCustomerNotes());
            serviceBooking.setStatus(BookingStatus.PENDING);

            // ✅ AUTO-ASSIGN MECHANIC
            Integer duration = serviceBooking.getEstimatedDuration();
            User autoAssignedMechanic = autoAssignMechanicSmart(scheduledDateTime, duration);

            if (autoAssignedMechanic != null) {
                serviceBooking.setMechanic(autoAssignedMechanic);
                serviceBooking.setStatus(BookingStatus.ASSIGNED);
                System.out.println("✅ AUTO-ASSIGNED: Mechanic " + autoAssignedMechanic.getFullName() +
                        " to booking for " + scheduledDateTime);
            } else {
                System.out.println("⚠️ NO MECHANIC AVAILABLE: Booking remains PENDING for manual assignment");
            }

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

    // ✅ ENHANCED: Allow reassignment even if mechanic already assigned
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

            // Check for time conflicts
            List<ServiceBooking> conflicts = serviceBookingRepository.findConflictingBookings(
                    mechanic,
                    booking.getScheduledDateTime(),
                    booking.getScheduledDateTime().plusMinutes(booking.getEstimatedDuration() != null ?
                            booking.getEstimatedDuration() : 60)
            );

            // Exclude current booking from conflict check
            conflicts = conflicts.stream()
                    .filter(b -> !b.getId().equals(bookingId))
                    .collect(Collectors.toList());

            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Mechanic has conflicting bookings at this time");
            }

            // Check if reassigning
            boolean isReassignment = booking.getMechanic() != null &&
                    !booking.getMechanic().getId().equals(mechanicId);

            booking.setMechanic(mechanic);
            booking.setStatus(BookingStatus.ASSIGNED);

            ServiceBooking savedBooking = serviceBookingRepository.save(booking);

            if (isReassignment) {
                System.out.println("✅ REASSIGNED: Booking #" + bookingId + " to mechanic " +
                        mechanic.getFullName());
            } else {
                System.out.println("✅ ASSIGNED: Mechanic " + mechanic.getFullName() + " to booking #" +
                        bookingId);
            }

            return savedBooking;
        } catch (Exception e) {
            System.out.println("Error assigning mechanic: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to assign mechanic: " + e.getMessage());
        }
    }

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
                .filter(user -> user.getRole() == User.UserRole.MECHANIC && user.isActive())
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

    // ✅ NEW: Smart auto-assignment - considers workload and availability
    public User autoAssignMechanicSmart(LocalDateTime scheduledTime, Integer durationMinutes) {
        List<User> availableMechanics = getAvailableMechanics();

        if (availableMechanics.isEmpty()) {
            System.out.println("⚠️ No mechanics in the system");
            return null;
        }

        // Get mechanic workload
        List<Object[]> workloadData = serviceBookingRepository.findMechanicWorkload();
        Map<Long, Long> workloadMap = workloadData.stream()
                .collect(Collectors.toMap(
                        arr -> ((User) arr[0]).getId(),
                        arr -> (Long) arr[1]
                ));

        // Filter mechanics available at the scheduled time
        List<User> availableAtTime = availableMechanics.stream()
                .filter(mechanic -> isMechanicAvailable(mechanic, scheduledTime, durationMinutes))
                .collect(Collectors.toList());

        if (availableAtTime.isEmpty()) {
            System.out.println("⚠️ No mechanics available at " + scheduledTime);
            return null;
        }

        // Sort by workload (least busy first)
        User selectedMechanic = availableAtTime.stream()
                .min(Comparator.comparingLong(m -> workloadMap.getOrDefault(m.getId(), 0L)))
                .orElse(null);

        if (selectedMechanic != null) {
            long workload = workloadMap.getOrDefault(selectedMechanic.getId(), 0L);
            System.out.println("🎯 Selected mechanic: " + selectedMechanic.getFullName() +
                    " (Current workload: " + workload + " active bookings)");
        }

        return selectedMechanic;
    }

    // ✅ DEPRECATED: Keep for backward compatibility but use autoAssignMechanicSmart instead
    @Deprecated
    public User autoAssignMechanic(LocalDateTime scheduledTime, Integer durationMinutes) {
        return autoAssignMechanicSmart(scheduledTime, durationMinutes);
    }

    // ✅ NEW: Get mechanic workload for display
    public Map<Long, Long> getMechanicWorkloadMap() {
        List<Object[]> workloadData = serviceBookingRepository.findMechanicWorkload();
        return workloadData.stream()
                .collect(Collectors.toMap(
                        arr -> ((User) arr[0]).getId(),
                        arr -> (Long) arr[1]
                ));
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
                return Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.ASSIGNED, BookingStatus.CANCELLED).contains(newStatus);
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