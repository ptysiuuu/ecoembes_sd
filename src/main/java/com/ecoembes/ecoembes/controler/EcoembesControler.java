package com.ecoembes.ecoembes.controler;

import com.ecoembes.ecoembes.dto.*;
import com.ecoembes.ecoembes.exception.InvalidTokenException;
import com.ecoembes.ecoembes.service.DumpsterService;
import com.ecoembes.ecoembes.service.EmployeeService;
import com.ecoembes.ecoembes.service.PlantService;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Main API controller - routes all requests to appropriate services.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ecoembes API", description = "Main API for Ecoembes Server Prototype 1")
public class EcoembesControler {

    private final EmployeeService employeeService;
    private final DumpsterService dumpsterService;
    private final PlantService plantService;
    private final SessionManager sessionManager;

    public EcoembesControler(EmployeeService employeeService, DumpsterService dumpsterService, PlantService plantService, SessionManager sessionManager) {
        this.employeeService = employeeService;
        this.dumpsterService = dumpsterService;
        this.plantService = plantService;
        this.sessionManager = sessionManager;
    }

    /**
     * Validates token and returns employee data if valid.
     * Throws exception if token is invalid/expired.
     */
    private EmployeeDataDTO validate(String token) {
        if (!sessionManager.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired token.");
        }
        EmployeeDataDTO employeeData = sessionManager.getEmployeeData(token);
        if (employeeData == null) {
            throw new InvalidTokenException("Token valid but no employee data found.");
        }
        return employeeData;
    }

    // --- Employee & Session Endpoints ---

    @Operation(summary = "Login an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthTokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokenDTO> login(@Valid @RequestBody LoginCredentialDTO credentials) {
        AuthTokenDTO authToken = employeeService.login(credentials.email(), credentials.password());
        return ResponseEntity.ok(authToken);
    }

    @Operation(summary = "Logout an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token) {
        validate(token);
        employeeService.logout(token);
        return ResponseEntity.ok().build();
    }

    // --- Dumpster Endpoints ---

    @Operation(summary = "Create a new dumpster")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dumpster created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DumpsterStatusDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/dumpsters")
    public ResponseEntity<DumpsterStatusDTO> createNewDumpster(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Valid @RequestBody NewDumpsterDTO newDumpster
    ) {
        validate(token);
        DumpsterStatusDTO createdDumpster = dumpsterService.createNewDumpster(newDumpster.location(), newDumpster.initialCapacity());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDumpster);
    }

    @Operation(summary = "Update dumpster status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dumpster updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DumpsterStatusDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "Dumpster not found")
    })
    @PutMapping("/dumpsters/{id}")
    public ResponseEntity<DumpsterStatusDTO> updateDumpster(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Parameter(description = "Dumpster ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateDumpsterDTO updateData
    ) {
        validate(token);
        DumpsterStatusDTO updatedDumpster = dumpsterService.updateDumpsterStatus(
                id,
                updateData.fillLevel(),
                updateData.containersNumber()
        );
        return ResponseEntity.ok(updatedDumpster);
    }

    @Operation(summary = "Check dumpster status for a specific area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dumpster status"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @GetMapping("/dumpsters/status")
    public ResponseEntity<List<DumpsterStatusDTO>> getDumpsterStatus(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Parameter(description = "Postal code of the area to check", required = true) @RequestParam String postalCode,
            @Parameter(description = "Date to check status for (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        validate(token);
        List<DumpsterStatusDTO> statusList = dumpsterService.getDumpsterStatus(postalCode, date);
        return ResponseEntity.ok(statusList);
    }

    @Operation(summary = "Query dumpster usage over a time period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dumpster usage data"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @GetMapping("/dumpsters/usage")
    public ResponseEntity<List<DumpsterUsageDTO>> getDumpsterUsage(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Parameter(description = "Start date for query (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for query (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        validate(token);
        List<DumpsterUsageDTO> usageList = dumpsterService.queryDumpsterUsage(startDate, endDate);
        return ResponseEntity.ok(usageList);
    }

    // --- Recycling Plant Endpoints ---

    @Operation(summary = "Get all recycling plants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all plants"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @GetMapping("/plants")
    public ResponseEntity<List<PlantCapacityDTO>> getAllPlants(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token
    ) {
        validate(token);
        List<PlantCapacityDTO> plants = plantService.getAllPlants();
        return ResponseEntity.ok(plants);
    }

    @Operation(summary = "Check available capacity at recycling plants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved plant capacities"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "Plant not found")
    })
    @GetMapping("/plants/capacity")
    public ResponseEntity<List<PlantCapacityDTO>> getPlantCapacity(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Parameter(description = "Date to check capacity for (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Optional plant ID to filter specific plant") @RequestParam(required = false) String plantId
    ) {
        validate(token);
        List<PlantCapacityDTO> capacityList = plantService.getPlantCapacity(date, plantId);
        return ResponseEntity.ok(capacityList);
    }

    @Operation(summary = "Assign one or more dumpsters to a recycling plant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dumpsters assigned successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/plants/assign")
    public ResponseEntity<AssignmentResponseDTO> assignDumpstersToPlant(
            @Parameter(description = "Session token received at login") @RequestHeader("Authorization") String token,
            @Valid @RequestBody AssignDumpsterDTO assignment
    ) {
        EmployeeDataDTO employeeData = validate(token);
        AssignmentResponseDTO response = plantService.assignDumpsters(
                employeeData.employeeID(),
                assignment.plantID(),
                assignment.dumpsterIDs()
        );
        return ResponseEntity.ok(response);
    }
}