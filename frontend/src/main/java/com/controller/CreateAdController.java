package com.controller;

import com.BazarakFrontendApplication;
import com.model.Advertisement;
import com.model.Category;
import com.model.City;
import com.model.User;
import com.service.AdService;
import com.service.CategoryService;
import com.service.CityService;
import com.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.List;

public class CreateAdController {

    // ============================================
    // FXML FIELDS
    // ============================================

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private ComboBox<City> cityCombo;

    @FXML
    private Label errorLabel;

    // ============================================
    // INITIALIZE
    // ============================================

    @FXML
    private void initialize() {
        loadCategories();
        loadCities();
    }

    // ============================================
    // LOAD DATA FROM BACKEND
    // ============================================

    private void loadCategories() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
            categoryCombo.setPromptText("Select Category");
        } catch (Exception e) {
            errorLabel.setText("Failed to load categories: " + e.getMessage());
        }
    }

    private void loadCities() {
        try {
            List<City> cities = CityService.getAllCities();
            cityCombo.setItems(FXCollections.observableArrayList(cities));
            cityCombo.setPromptText("Select City");
        } catch (Exception e) {
            errorLabel.setText("Failed to load cities: " + e.getMessage());
        }
    }



}