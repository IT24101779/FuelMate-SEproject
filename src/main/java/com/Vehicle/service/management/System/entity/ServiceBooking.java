package com.Vehicle.service.management.System.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_bookings")
public class ServiceBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    private User mechanic;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleMake;

    @Column(nullable = false)
    private String vehicleModel;

    private Integer vehicleYear;

    @Column(nullable = false)
    private String serviceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes

    @Column(name = "estimated_cost")
    private Double estimatedCost = 0.0;

    @Column(name = "actual_cost")
    private Double actualCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "customer_notes")
    private String customerNotes;

    @Column(name = "mechanic_notes")
    private String mechanicNotes;

    @Column(name = "completion_notes")
    private String completionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum BookingStatus {
        PENDING,        // Newly created booking
        CONFIRMED,      // Admin/Manager confirmed the booking
        ASSIGNED,       // Mechanic has been assigned
        IN_PROGRESS,    // Work has started
        COMPLETED,      // Service completed
        CANCELLED,      // Booking cancelled
        NO_SHOW        // Customer didn't show up
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    // Constructors
    public ServiceBooking() {}

    public ServiceBooking(User customer, String vehicleNumber, String vehicleMake,
                          String vehicleModel, String serviceType, LocalDateTime scheduledDateTime) {
        this.customer = customer;
        this.vehicleNumber = vehicleNumber;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.serviceType = serviceType;
        this.scheduledDateTime = scheduledDateTime;
        this.status = BookingStatus.PENDING;
        this.priority = Priority.MEDIUM;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // PrePersist and PreUpdate methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (estimatedCost == null) {
            estimatedCost = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Auto-set started time when status becomes IN_PROGRESS
        if (status == BookingStatus.IN_PROGRESS && startedAt == null) {
            startedAt = LocalDateTime.now();
        }

        // Auto-set completed time when status becomes COMPLETED
        if (status == BookingStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public User getMechanic() { return mechanic; }
    public void setMechanic(User mechanic) { this.mechanic = mechanic; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(LocalDateTime scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost != null ? estimatedCost : 0.0; }

    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { this.actualCost = actualCost; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) {
        this.status = status != null ? status : BookingStatus.PENDING;
    }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
    }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getMechanicNotes() { return mechanicNotes; }
    public void setMechanicNotes(String mechanicNotes) { this.mechanicNotes = mechanicNotes; }

    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Helper methods
    public String getVehicleFullName() {
        StringBuilder sb = new StringBuilder();
        if (vehicleYear != null) {
            sb.append(vehicleYear).append(" ");
        }
        sb.append(vehicleMake).append(" ").append(vehicleModel);
        return sb.toString();
    }

    public String getFormattedVehicle() {
        return getVehicleFullName() + " (" + vehicleNumber + ")";
    }

    public boolean canBeStarted() {
        return status == BookingStatus.ASSIGNED || status == BookingStatus.CONFIRMED;
    }

    public boolean canBeCompleted() {
        return status == BookingStatus.IN_PROGRESS;
    }

    public boolean canBeCancelled() {
        return status != BookingStatus.COMPLETED && status != BookingStatus.CANCELLED;
    }

    public boolean isActive() {
        return status != BookingStatus.COMPLETED && status != BookingStatus.CANCELLED && status != BookingStatus.NO_SHOW;
    }

    public String getStatusDisplayName() {
        return status.name().replace("_", " ");
    }

    public String getPriorityDisplayName() {
        return priority.name().substring(0, 1) + priority.name().substring(1).toLowerCase();
    }
}