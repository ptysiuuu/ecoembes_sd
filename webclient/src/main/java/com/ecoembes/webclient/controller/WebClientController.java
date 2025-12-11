package com.ecoembes.webclient.controller;

import com.ecoembes.webclient.model.Model;
import com.ecoembes.webclient.proxy.IServiceProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Client Controller - handles HTTP requests from the browser
 * and delegates business logic to the Service Proxy.
 */
@Controller
public class WebClientController {

    private final IServiceProxy serviceProxy;
    private final Model sessionModel;

    public WebClientController(IServiceProxy serviceProxy, Model sessionModel) {
        this.serviceProxy = serviceProxy;
        this.sessionModel = sessionModel;
    }

    // ========== Index & Login ==========

    @GetMapping("/")
    public String index() {
        if (sessionModel.isAuthenticated()) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String password,
                       ModelMap model) {
        try {
            System.out.println("[CONTROLLER] Login attempt for: " + email);
            String token = serviceProxy.login(email, password);
            System.out.println("[CONTROLLER] Token received: " + token);

            if (token == null || token.isEmpty()) {
                System.err.println("[CONTROLLER] Token is null or empty!");
                model.addAttribute("error", "Invalid email or password");
                return "login";
            }

            sessionModel.setToken(token);
            sessionModel.setCurrentURL("/home");
            System.out.println("[CONTROLLER] Login successful, redirecting to /home");
            return "redirect:/home";
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Login failed with exception: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Invalid email or password: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        try {
            if (sessionModel.isAuthenticated()) {
                serviceProxy.logout(sessionModel.getToken());
            }
        } catch (Exception e) {
            // Log error but continue with logout
        }
        sessionModel.setToken(null);
        sessionModel.setCurrentURL(null);
        return "redirect:/login";
    }

    // ========== Home ==========

    @GetMapping("/home")
    public String home(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }
        sessionModel.setCurrentURL("/home");
        return "index";
    }

    // ========== Dumpsters ==========

    @GetMapping("/dumpsters/create")
    public String createDumpsterPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }
        sessionModel.setCurrentURL("/dumpsters/create");
        return "dumpsters/create";
    }

    @PostMapping("/dumpsters/create")
    public String createDumpster(@RequestParam String location,
                                 @RequestParam Double initialCapacity,
                                 ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> dumpsterData = new HashMap<>();
            dumpsterData.put("location", location);
            dumpsterData.put("initialCapacity", initialCapacity);

            Map<String, Object> result = serviceProxy.createDumpster(sessionModel.getToken(), dumpsterData);
            model.addAttribute("success", "Dumpster created successfully!");
            model.addAttribute("dumpster", result);
            return "dumpsters/create-success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create dumpster: " + e.getMessage());
            return "dumpsters/create";
        }
    }

    @GetMapping("/dumpsters/query")
    public String queryDumpstersPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }
        sessionModel.setCurrentURL("/dumpsters/query");
        return "dumpsters/query";
    }

    @PostMapping("/dumpsters/query")
    public String queryDumpsters(@RequestParam String startDate,
                                 @RequestParam String endDate,
                                 ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<Map<String, Object>> usage = serviceProxy.queryDumpsterUsage(sessionModel.getToken(), start, end);
            model.addAttribute("usageData", usage);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            return "dumpsters/query-results";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to query dumpsters: " + e.getMessage());
            return "dumpsters/query";
        }
    }

    @GetMapping("/dumpsters/status")
    public String dumpsterStatusPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }
        sessionModel.setCurrentURL("/dumpsters/status");
        return "dumpsters/status";
    }

    @PostMapping("/dumpsters/status")
    public String dumpsterStatus(@RequestParam String postalCode,
                                 @RequestParam String date,
                                 ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            LocalDate queryDate = LocalDate.parse(date);
            List<Map<String, Object>> status = serviceProxy.getDumpsterStatus(
                    sessionModel.getToken(), postalCode, queryDate);
            model.addAttribute("statusData", status);
            model.addAttribute("postalCode", postalCode);
            model.addAttribute("date", date);
            return "dumpsters/status-results";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to get dumpster status: " + e.getMessage());
            return "dumpsters/status";
        }
    }

    // ========== Plants ==========

    @GetMapping("/plants")
    public String plantsPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            List<Map<String, Object>> plants = serviceProxy.getAllPlants(sessionModel.getToken());
            model.addAttribute("plants", plants);
            sessionModel.setCurrentURL("/plants");
            return "plants/list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load plants: " + e.getMessage());
            return "plants/list";
        }
    }

    @GetMapping("/plants/capacity")
    public String plantCapacityPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            List<Map<String, Object>> plants = serviceProxy.getAllPlants(sessionModel.getToken());
            model.addAttribute("plants", plants);
            sessionModel.setCurrentURL("/plants/capacity");
            return "plants/capacity";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load plants: " + e.getMessage());
            return "plants/capacity";
        }
    }

    @PostMapping("/plants/capacity")
    public String plantCapacity(@RequestParam String date,
                                @RequestParam(required = false) String plantId,
                                ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            LocalDate queryDate = LocalDate.parse(date);
            List<Map<String, Object>> capacities = serviceProxy.getPlantCapacity(
                    sessionModel.getToken(), queryDate, plantId);
            model.addAttribute("capacities", capacities);
            model.addAttribute("date", date);
            model.addAttribute("plantId", plantId);
            return "plants/capacity-results";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to check plant capacity: " + e.getMessage());
            return "plants/capacity";
        }
    }

    // ========== Assignments ==========

    @GetMapping("/assignments/create")
    public String createAssignmentPage(ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            List<Map<String, Object>> plants = serviceProxy.getAllPlants(sessionModel.getToken());
            model.addAttribute("plants", plants);
            sessionModel.setCurrentURL("/assignments/create");
            return "assignments/create";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load plants: " + e.getMessage());
            return "assignments/create";
        }
    }

    @PostMapping("/assignments/create")
    public String createAssignment(@RequestParam String plantId,
                                   @RequestParam String dumpsterIds,
                                   @RequestParam String date,
                                   ModelMap model) {
        if (!sessionModel.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            // Split and trim dumpster IDs
            List<String> dumpsterIdList = List.of(dumpsterIds.split(","))
                    .stream()
                    .map(String::trim)
                    .toList();

            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("plantID", plantId);  // Note: plantID not plantId
            assignmentData.put("dumpsterIDs", dumpsterIdList);  // Note: dumpsterIDs not dumpsterIds
            assignmentData.put("date", date);

            Map<String, Object> result = serviceProxy.assignDumpstersToPlant(
                    sessionModel.getToken(), assignmentData);
            model.addAttribute("success", "Assignment created successfully!");
            model.addAttribute("assignment", result);
            return "assignments/create-success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create assignment: " + e.getMessage());
            List<Map<String, Object>> plants = serviceProxy.getAllPlants(sessionModel.getToken());
            model.addAttribute("plants", plants);
            return "assignments/create";
        }
    }
}

