package com.Vehicle.service.management.System.service;

import com.Vehicle.service.management.System.dto.InventoryItemDTO;
import com.Vehicle.service.management.System.entity.InventoryItem;
import com.Vehicle.service.management.System.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryRepository.findAll();
    }

    public InventoryItem getInventoryItemById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    public InventoryItem createInventoryItem(InventoryItemDTO inventoryItemDTO) {
        try {
            InventoryItem item = new InventoryItem();
            item.setName(inventoryItemDTO.getName());
            item.setCategory(inventoryItemDTO.getCategory());
            item.setQuantity(inventoryItemDTO.getQuantity());
            item.setMinStockLevel(inventoryItemDTO.getMinStockLevel());
            item.setUnitPrice(inventoryItemDTO.getUnitPrice());
            item.setType(inventoryItemDTO.getType()); // Set type
            item.setDescription(inventoryItemDTO.getDescription());
            item.setSupplier(inventoryItemDTO.getSupplier());

            return inventoryRepository.save(item);
        } catch (Exception e) {
            System.out.println("Error creating inventory item: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public InventoryItem updateInventoryItem(Long id, InventoryItemDTO inventoryItemDTO) {
        try {
            InventoryItem item = inventoryRepository.findById(id).orElse(null);
            if (item != null) {
                item.setName(inventoryItemDTO.getName());
                item.setCategory(inventoryItemDTO.getCategory());
                item.setQuantity(inventoryItemDTO.getQuantity());
                item.setMinStockLevel(inventoryItemDTO.getMinStockLevel());
                item.setUnitPrice(inventoryItemDTO.getUnitPrice());
                item.setDescription(inventoryItemDTO.getDescription());
                item.setSupplier(inventoryItemDTO.getSupplier());

                return inventoryRepository.save(item);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error updating inventory item: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void deleteInventoryItem(Long id) {
        try {
            inventoryRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("Error deleting inventory item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<InventoryItem> getItemsByCategory(String category) {
        return inventoryRepository.findByCategory(category);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<InventoryItem> searchItems(String searchTerm) {
        return inventoryRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public List<String> getAllCategories() {
        return inventoryRepository.findAllCategories();
    }

    public InventoryItem updateItemQuantity(Long id, Integer quantityChange) {
        try {
            InventoryItem item = inventoryRepository.findById(id).orElse(null);
            if (item != null) {
                item.setQuantity(item.getQuantity() + quantityChange);
                return inventoryRepository.save(item);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error updating item quantity: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}