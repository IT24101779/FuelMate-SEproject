package com.Vehicle.service.management.System.controller;

import com.Vehicle.service.management.System.dto.InventoryItemDTO;
import com.Vehicle.service.management.System.entity.InventoryItem;
import com.Vehicle.service.management.System.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String inventoryDashboard(Model model) {
        try {
            List<InventoryItem> inventoryItems = inventoryService.getAllInventoryItems();
            List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
            List<String> categories = inventoryService.getAllCategories();

            model.addAttribute("pageTitle", "Inventory Management - FuelMate");
            model.addAttribute("inventoryItems", inventoryItems);
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("categories", categories);
            model.addAttribute("inventoryItemDTO", new InventoryItemDTO());

            return "inventory-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load inventory: " + e.getMessage());
            return "inventory-dashboard";
        }
    }

    @PostMapping("/add")
    public String addInventoryItem(@ModelAttribute InventoryItemDTO inventoryItemDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Adding item: " + inventoryItemDTO);

            // Set default values if null
            if (inventoryItemDTO.getQuantity() == null) {
                inventoryItemDTO.setQuantity(0);
            }
            if (inventoryItemDTO.getMinStockLevel() == null) {
                inventoryItemDTO.setMinStockLevel(5);
            }
            if (inventoryItemDTO.getUnitPrice() == null) {
                inventoryItemDTO.setUnitPrice(0.0);
            }
            if (inventoryItemDTO.getType() == null || inventoryItemDTO.getType().isEmpty()) {
                inventoryItemDTO.setType("GENERAL");
            }

            InventoryItem item = inventoryService.createInventoryItem(inventoryItemDTO);
            redirectAttributes.addFlashAttribute("message", "Inventory item added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add inventory item: " + e.getMessage());
        }

        return "redirect:/inventory";
    }

    @PostMapping("/update/{id}")
    public String updateInventoryItem(@PathVariable("id") Long id,
                                      @ModelAttribute InventoryItemDTO inventoryItemDTO,
                                      RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating item ID: " + id + " with data: " + inventoryItemDTO);

            // Set default values if null
            if (inventoryItemDTO.getQuantity() == null) {
                inventoryItemDTO.setQuantity(0);
            }
            if (inventoryItemDTO.getMinStockLevel() == null) {
                inventoryItemDTO.setMinStockLevel(5);
            }
            if (inventoryItemDTO.getUnitPrice() == null) {
                inventoryItemDTO.setUnitPrice(0.0);
            }
            if (inventoryItemDTO.getType() == null || inventoryItemDTO.getType().isEmpty()) {
                inventoryItemDTO.setType("GENERAL");
            }

            InventoryItem updatedItem = inventoryService.updateInventoryItem(id, inventoryItemDTO);
            redirectAttributes.addFlashAttribute("message", "Inventory item updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update inventory item: " + e.getMessage());
        }

        return "redirect:/inventory";
    }

    @PostMapping("/delete/{id}")
    public String deleteInventoryItem(@PathVariable("id") Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Deleting item ID: " + id);
            inventoryService.deleteInventoryItem(id);
            redirectAttributes.addFlashAttribute("message", "Inventory item deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete inventory item: " + e.getMessage());
        }

        return "redirect:/inventory";
    }

    @GetMapping("/category/{category}")
    public String getItemsByCategory(@PathVariable String category, Model model) {
        List<InventoryItem> categoryItems = inventoryService.getItemsByCategory(category);
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        List<String> categories = inventoryService.getAllCategories();

        model.addAttribute("pageTitle", category + " Inventory - FuelMate");
        model.addAttribute("inventoryItems", categoryItems);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("inventoryItemDTO", new InventoryItemDTO());

        return "inventory-dashboard";
    }

    @GetMapping("/low-stock")
    public String getLowStockItems(Model model) {
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        List<String> categories = inventoryService.getAllCategories();

        model.addAttribute("pageTitle", "Low Stock Items - FuelMate");
        model.addAttribute("inventoryItems", lowStockItems);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("categories", categories);
        model.addAttribute("showLowStock", true);
        model.addAttribute("inventoryItemDTO", new InventoryItemDTO());

        return "inventory-dashboard";
    }

    @PostMapping("/search")
    public String searchItems(@RequestParam String searchTerm, Model model) {
        List<InventoryItem> searchResults = inventoryService.searchItems(searchTerm);
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        List<String> categories = inventoryService.getAllCategories();

        model.addAttribute("pageTitle", "Search Results - FuelMate");
        model.addAttribute("inventoryItems", searchResults);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("categories", categories);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("inventoryItemDTO", new InventoryItemDTO());

        return "inventory-dashboard";
    }

    @PostMapping("/adjust-quantity/{id}")
    public String adjustItemQuantity(@PathVariable Long id,
                                     @RequestParam Integer quantityChange,
                                     RedirectAttributes redirectAttributes) {
        try {
            InventoryItem updatedItem = inventoryService.updateItemQuantity(id, quantityChange);
            if (updatedItem != null) {
                redirectAttributes.addFlashAttribute("message", "Quantity updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Inventory item not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update quantity: " + e.getMessage());
        }

        return "redirect:/inventory";
    }
}