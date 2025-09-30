package com.Vehicle.service.management.System.dto;

public class InventoryItemDTO {
    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private Integer minStockLevel;
    private Double unitPrice;
    private String type; // Added type field
    private String description;
    private String supplier;

    // Constructors
    public InventoryItemDTO() {}

    public InventoryItemDTO(String name, String category, Integer quantity, Integer minStockLevel, Double unitPrice, String type) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.unitPrice = unitPrice;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { this.minStockLevel = minStockLevel; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
}