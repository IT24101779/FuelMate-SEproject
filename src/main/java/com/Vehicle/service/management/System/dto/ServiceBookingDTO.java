package com.Vehicle.service.management.System.dto;

import com.Vehicle.service.management.System.entity.ServiceBooking.BookingStatus;
import com.Vehicle.service.management.System.entity.ServiceBooking.Priority;
import java.time.LocalDateTime;

public class ServiceBookingDTO {
    private Long id;
    private Long customerId;
    private Long mechanicId;
    private String vehicleNumber;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String serviceType;
    private String description;
    private String scheduledDate; // Will be converted to LocalDateTime
    private String scheduledTime; // Will be combined with scheduledDate
    private LocalDateTime scheduledDateTime;
    private Integer estimatedDuration;
    private Double estimatedCost;
    private Double actualCost;
    private BookingStatus status;
    private Priority priority;
    private String customerNotes;
    private String mechanicNotes;
    private String completionNotes;

    // Constructors
    public ServiceBookingDTO() {}

    public ServiceBookingDTO(String vehicleNumber, String vehicleMake, String vehicleModel,
                             String serviceType, String scheduledDate, String scheduledTime) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.serviceType = serviceType;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status = BookingStatus.PENDING;
        this.priority = Priority.MEDIUM;
        this.estimatedCost = 0.0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }

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

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(LocalDateTime scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { this.actualCost = actualCost; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getMechanicNotes() { return mechanicNotes; }
    public void setMechanicNotes(String mechanicNotes) { this.mechanicNotes = mechanicNotes; }

    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }

    @Override
    public String toString() {
        return "ServiceBookingDTO{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", mechanicId=" + mechanicId +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", vehicleMake='" + vehicleMake + '\'' +
                ", vehicleModel='" + vehicleModel + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}