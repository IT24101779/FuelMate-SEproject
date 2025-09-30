package com.Vehicle.service.management.System.controller;

import com.Vehicle.service.management.System.entity.InventoryItem;
import com.Vehicle.service.management.System.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/mechanic/inventory")
public class MechanicInventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String mechanicInventoryDashboard(Model model,
                                             @RequestParam(value = "search", required = false) String searchTerm) {
        try {
            List<InventoryItem> inventoryItems;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // Search for spare parts
                inventoryItems = inventoryService.searchItems(searchTerm);
            } else {
                // Get all inventory items (or filter for spare parts specifically)
                inventoryItems = inventoryService.getAllInventoryItems();
            }

            // Filter for spare parts and consumables (mechanics don't need to see fuel)
            List<InventoryItem> spareParts = inventoryItems.stream()
                    .filter(item -> item.getCategory() != null &&
                            (item.getCategory().equalsIgnoreCase("Spare Parts") ||
                                    item.getCategory().equalsIgnoreCase("Tools") ||
                                    item.getCategory().equalsIgnoreCase("Consumables") ||
                                    item.getType().equalsIgnoreCase("SPARE_PART") ||
                                    item.getType().equalsIgnoreCase("TOOL") ||
                                    item.getType().equalsIgnoreCase("CONSUMABLE")))
                    .toList();

            List<InventoryItem> lowStockItems = inventoryService.getLowStockItems().stream()
                    .filter(item -> item.getCategory() != null &&
                            (item.getCategory().equalsIgnoreCase("Spare Parts") ||
                                    item.getCategory().equalsIgnoreCase("Tools") ||
                                    item.getCategory().equalsIgnoreCase("Consumables") ||
                                    item.getType().equalsIgnoreCase("SPARE_PART") ||
                                    item.getType().equalsIgnoreCase("TOOL") ||
                                    item.getType().equalsIgnoreCase("CONSUMABLE")))
                    .toList();

            model.addAttribute("pageTitle", "Spare Parts Inventory - FuelMate");
            model.addAttribute("inventoryItems", spareParts);
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("searchTerm", searchTerm);

            return "mechanic-inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load spare parts inventory: " + e.getMessage());
            return "mechanic-inventory";
        }
    }

    @GetMapping("/low-stock")
    public String getLowStockSpareParts(Model model) {
        try {
            List<InventoryItem> lowStockItems = inventoryService.getLowStockItems().stream()
                    .filter(item -> item.getCategory() != null &&
                            (item.getCategory().equalsIgnoreCase("Spare Parts") ||
                                    item.getCategory().equalsIgnoreCase("Tools") ||
                                    item.getCategory().equalsIgnoreCase("Consumables") ||
                                    item.getType().equalsIgnoreCase("SPARE_PART") ||
                                    item.getType().equalsIgnoreCase("TOOL") ||
                                    item.getType().equalsIgnoreCase("CONSUMABLE")))
                    .toList();

            model.addAttribute("pageTitle", "Low Stock Spare Parts - FuelMate");
            model.addAttribute("inventoryItems", lowStockItems);
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("showLowStock", true);

            return "mechanic-inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load low stock items: " + e.getMessage());
            return "mechanic-inventory";
        }
    }

    @GetMapping("/search")
    public String searchSpareParts(@RequestParam String searchTerm, Model model) {
        try {
            List<InventoryItem> searchResults = inventoryService.searchItems(searchTerm).stream()
                    .filter(item -> item.getCategory() != null &&
                            (item.getCategory().equalsIgnoreCase("Spare Parts") ||
                                    item.getCategory().equalsIgnoreCase("Tools") ||
                                    item.getCategory().equalsIgnoreCase("Consumables") ||
                                    item.getType().equalsIgnoreCase("SPARE_PART") ||
                                    item.getType().equalsIgnoreCase("TOOL") ||
                                    item.getType().equalsIgnoreCase("CONSUMABLE")))
                    .toList();

            List<InventoryItem> lowStockItems = inventoryService.getLowStockItems().stream()
                    .filter(item -> item.getCategory() != null &&
                            (item.getCategory().equalsIgnoreCase("Spare Parts") ||
                                    item.getCategory().equalsIgnoreCase("Tools") ||
                                    item.getCategory().equalsIgnoreCase("Consumables") ||
                                    item.getType().equalsIgnoreCase("SPARE_PART") ||
                                    item.getType().equalsIgnoreCase("TOOL") ||
                                    item.getType().equalsIgnoreCase("CONSUMABLE")))
                    .toList();

            model.addAttribute("pageTitle", "Search Results - FuelMate");
            model.addAttribute("inventoryItems", searchResults);
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("searchTerm", searchTerm);

            return "mechanic-inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to search spare parts: " + e.getMessage());
            return "mechanic-inventory";
        }
    }
}