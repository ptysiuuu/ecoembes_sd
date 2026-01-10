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
        com.ecoembes.ecoembes.domain.Employee employee = sessionManager.getEmployee(token);
        if (employee == null) {
            throw new InvalidTokenException("Token valid but no employee data found.");
        }
        return new EmployeeDataDTO(employee.getEmployeeId(), employee.getName(), employee.getEmail());
    }

    // --- Employee & Session Endpoints ---

    @Operation(summary = "Login an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthTokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokenDTO> login(@Valid @RequestBody LoginCredentialDTO credentials) {
        com.ecoembes.ecoembes.domain.Employee employee = employeeService.login(credentials.email(), credentials.password());

        String token = employeeService.createSessionToken(employee);
        long timestamp = Long.parseLong(token);
        AuthTokenDTO authToken = new AuthTokenDTO(token, timestamp);
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
        com.ecoembes.ecoembes.domain.Dumpster dumpster = dumpsterService.createNewDumpster(newDumpster.location(), newDumpster.initialCapacity());
        DumpsterStatusDTO dto = new DumpsterStatusDTO(
                dumpster.getDumpsterId(),
                dumpster.getLocation(),
                dumpster.getFillLevel(),
                dumpster.getContainersNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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
        com.ecoembes.ecoembes.domain.Dumpster dumpster = dumpsterService.updateDumpsterStatus(
                id,
                updateData.fillLevel(),
                updateData.containersNumber()
        );
        DumpsterStatusDTO dto = new DumpsterStatusDTO(
                dumpster.getDumpsterId(),
                dumpster.getLocation(),
                dumpster.getFillLevel(),
                dumpster.getContainersNumber()
        );
        return ResponseEntity.ok(dto);
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
        List<com.ecoembes.ecoembes.domain.Dumpster> dumpsters = dumpsterService.getDumpsterStatus(postalCode, date);
        List<DumpsterStatusDTO> statusList = dumpsters.stream()
                .map(d -> new DumpsterStatusDTO(
                        d.getDumpsterId(),
                        d.getLocation(),
                        d.getFillLevel(),
                        d.getContainersNumber()
                ))
                .collect(java.util.stream.Collectors.toList());
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
        List<com.ecoembes.ecoembes.domain.Usage> usages = dumpsterService.queryDumpsterUsage(startDate, endDate);
        List<DumpsterUsageDTO> usageList = usages.stream()
                .map(u -> new DumpsterUsageDTO(
                        u.getDumpster().getDumpsterId(),
                        u.getDate(),
                        u.getFillLevel(),
                        u.getContainersCount()
                ))
                .collect(java.util.stream.Collectors.toList());
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
        List<com.ecoembes.ecoembes.domain.Plant> plants = plantService.getAllPlants();
        LocalDate now = LocalDate.now();
        List<PlantCapacityDTO> plantDTOs = plants.stream()
                .map(p -> {
                    try {
                        Double capacity = plantService.getPlantCapacity(p.getPlantId(), now);
                        return new PlantCapacityDTO(p.getPlantId(), p.getName(), capacity != null ? capacity : p.getAvailableCapacity());
                    } catch (Exception e) {
                        return new PlantCapacityDTO(p.getPlantId(), p.getName(), p.getAvailableCapacity());
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(plantDTOs);
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
        List<com.ecoembes.ecoembes.domain.Plant> plants = plantService.getPlantCapacityByDate(date, plantId);
        List<PlantCapacityDTO> capacityList = plants.stream()
                .map(p -> {
                    try {
                        Double capacity = plantService.getPlantCapacity(p.getPlantId(), date);
                        return new PlantCapacityDTO(p.getPlantId(), p.getName(), capacity != null ? capacity : p.getAvailableCapacity());
                    } catch (Exception e) {
                        return new PlantCapacityDTO(p.getPlantId(), p.getName(), p.getAvailableCapacity());
                    }
                })
                .collect(java.util.stream.Collectors.toList());
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
        List<com.ecoembes.ecoembes.domain.Assignment> assignments = plantService.assignDumpsters(
                employeeData.employeeID(),
                assignment.plantID(),
                assignment.dumpsterIDs(),
                assignment.date()
        );

        // Convert domain to DTO
        com.ecoembes.ecoembes.domain.Assignment firstAssignment = assignments.get(0);
        List<String> dumpsterIds = assignments.stream()
                .map(a -> a.getDumpster().getDumpsterId())
                .collect(java.util.stream.Collectors.toList());

        AssignmentResponseDTO response = new AssignmentResponseDTO(
                firstAssignment.getEmployee().getEmployeeId(),
                firstAssignment.getEmployee().getName(),
                firstAssignment.getPlant().getPlantId(),
                dumpsterIds,
                firstAssignment.getAssignmentDate().toString(),
                firstAssignment.getStatus()
        );

        return ResponseEntity.ok(response);
    }
}