package com.Vehicle.service.management.System.repository;

import com.Vehicle.service.management.System.entity.ServiceBooking;
import com.Vehicle.service.management.System.entity.ServiceBooking.BookingStatus;
import com.Vehicle.service.management.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {

    // Find bookings by customer
    List<ServiceBooking> findByCustomerOrderByScheduledDateTimeDesc(User customer);

    // Find bookings by mechanic
    List<ServiceBooking> findByMechanicOrderByScheduledDateTimeAsc(User mechanic);

    // Find bookings by status
    List<ServiceBooking> findByStatusOrderByScheduledDateTimeAsc(BookingStatus status);

    // Find bookings by multiple statuses
    List<ServiceBooking> findByStatusInOrderByScheduledDateTimeAsc(List<BookingStatus> statuses);

    // Find today's bookings
    @Query("SELECT sb FROM ServiceBooking sb WHERE DATE(sb.scheduledDateTime) = DATE(:today) ORDER BY sb.scheduledDateTime ASC")
    List<ServiceBooking> findTodayBookings(@Param("today") LocalDateTime today);

    // Find bookings for a specific date
    @Query("SELECT sb FROM ServiceBooking sb WHERE DATE(sb.scheduledDateTime) = DATE(:date) ORDER BY sb.scheduledDateTime ASC")
    List<ServiceBooking> findBookingsByDate(@Param("date") LocalDateTime date);

    // Find bookings between dates
    List<ServiceBooking> findByScheduledDateTimeBetweenOrderByScheduledDateTimeAsc(LocalDateTime startDate, LocalDateTime endDate);

    // Find pending bookings (waiting for confirmation/assignment)
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.status IN ('PENDING', 'CONFIRMED') ORDER BY sb.createdAt ASC")
    List<ServiceBooking> findPendingBookings();

    // Find active bookings (not completed, cancelled, or no-show)
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY sb.scheduledDateTime ASC")
    List<ServiceBooking> findActiveBookings();

    // Find overdue bookings (scheduled in the past but not completed)
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.scheduledDateTime < :currentTime AND sb.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY sb.scheduledDateTime ASC")
    List<ServiceBooking> findOverdueBookings(@Param("currentTime") LocalDateTime currentTime);

    // Search bookings by vehicle number or customer name
    @Query("SELECT sb FROM ServiceBooking sb LEFT JOIN sb.customer c WHERE " +
            "LOWER(sb.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(sb.serviceType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY sb.scheduledDateTime DESC")
    List<ServiceBooking> searchBookings(@Param("searchTerm") String searchTerm);

    // Count bookings by status
    long countByStatus(BookingStatus status);

    // Count today's bookings
    @Query("SELECT COUNT(sb) FROM ServiceBooking sb WHERE DATE(sb.scheduledDateTime) = DATE(:today)")
    long countTodayBookings(@Param("today") LocalDateTime today);

    // Find mechanics' workload (count of assigned/in-progress bookings)
    @Query("SELECT sb.mechanic, COUNT(sb) FROM ServiceBooking sb " +
            "WHERE sb.mechanic IS NOT NULL AND sb.status IN ('ASSIGNED', 'IN_PROGRESS') " +
            "GROUP BY sb.mechanic")
    List<Object[]> findMechanicWorkload();

    // Find available time slots (bookings that don't conflict with given time range)
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.mechanic = :mechanic AND " +
            "sb.scheduledDateTime < :endTime AND " +
            "sb.scheduledDateTime >= :startTime AND " +
            "sb.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW')")
    List<ServiceBooking> findConflictingBookings(@Param("mechanic") User mechanic,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    // Get distinct service types for dropdown
    @Query("SELECT DISTINCT sb.serviceType FROM ServiceBooking sb ORDER BY sb.serviceType")
    List<String> findAllServiceTypes();

    // Get statistics for dashboard
    @Query("SELECT sb.status, COUNT(sb) FROM ServiceBooking sb GROUP BY sb.status")
    List<Object[]> getBookingStatusStatistics();

    // Find bookings created in the last N days
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.createdAt >= :fromDate ORDER BY sb.createdAt DESC")
    List<ServiceBooking> findRecentBookings(@Param("fromDate") LocalDateTime fromDate);

    // Find bookings by service type
    List<ServiceBooking> findByServiceTypeOrderByScheduledDateTimeDesc(String serviceType);

    // Check if vehicle has existing active booking
    @Query("SELECT COUNT(sb) > 0 FROM ServiceBooking sb WHERE " +
            "LOWER(sb.vehicleNumber) = LOWER(:vehicleNumber) AND " +
            "sb.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW')")
    boolean hasActiveBookingForVehicle(@Param("vehicleNumber") String vehicleNumber);

    // Find customer's booking history
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.customer = :customer " +
            "ORDER BY sb.scheduledDateTime DESC")
    List<ServiceBooking> findCustomerBookingHistory(@Param("customer") User customer);

    // Find upcoming bookings (scheduled for future)
    @Query("SELECT sb FROM ServiceBooking sb WHERE sb.scheduledDateTime > :currentTime AND " +
            "sb.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') " +
            "ORDER BY sb.scheduledDateTime ASC")
    List<ServiceBooking> findUpcomingBookings(@Param("currentTime") LocalDateTime currentTime);
}