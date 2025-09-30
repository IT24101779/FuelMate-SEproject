package com.Vehicle.service.management.System.repository;

import com.Vehicle.service.management.System.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByCategory(String category);
    List<InventoryItem> findByStatus(InventoryItem.ItemStatus status);
    List<InventoryItem> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minStockLevel")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT DISTINCT i.category FROM InventoryItem i ORDER BY i.category")
    List<String> findAllCategories();
}