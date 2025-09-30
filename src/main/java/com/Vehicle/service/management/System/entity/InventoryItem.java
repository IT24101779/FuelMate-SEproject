package com.Vehicle.service.management.System.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 5;

    @Column(name = "price_per_unit", nullable = false)
    private Double unitPrice = 0.0;

    @Column(nullable = false)
    private String type = "GENERAL"; // Added type field with default value

    private String description;

    private String supplier;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    public enum ItemStatus {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    }

    // Constructors
    public InventoryItem() {
        this.status = ItemStatus.OUT_OF_STOCK;
    }

    public InventoryItem(String name, String category, Integer quantity, Integer minStockLevel, Double unitPrice, String type) {
        this.name = name;
        this.category = category;
        this.quantity = quantity != null ? quantity : 0;
        this.minStockLevel = minStockLevel != null ? minStockLevel : 5;
        this.unitPrice = unitPrice != null ? unitPrice : 0.0;
        this.type = type != null ? type : "GENERAL";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateStatus();
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
            updateStatus();
        }
        if (type == null) {
            type = "GENERAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    // Update status based on quantity
    private void updateStatus() {
        if (minStockLevel == null) {
            minStockLevel = 5;
        }
        if (quantity == null) {
            quantity = 0;
        }

        if (quantity == 0) {
            this.status = ItemStatus.OUT_OF_STOCK;
        } else if (quantity <= minStockLevel) {
            this.status = ItemStatus.LOW_STOCK;
        } else {
            this.status = ItemStatus.IN_STOCK;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity != null ? quantity : 0;
        updateStatus();
    }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel != null ? minStockLevel : 5;
        updateStatus();
    }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : 0.0;
    }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type != null ? type : "GENERAL";
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    // Helper methods
    public Double getTotalValue() {
        return (quantity != null && unitPrice != null) ? quantity * unitPrice : 0.0;
    }

    public boolean needsRestocking() {
        return status == ItemStatus.LOW_STOCK || status == ItemStatus.OUT_OF_STOCK;
    }
}