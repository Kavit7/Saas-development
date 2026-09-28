package com.saas.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.models.RoomType;
import com.saas.backend.service.RoomTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Room Types", description = "Manage room types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    @Operation(summary = "Create room type")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<RoomType> createRoomType(
            @RequestParam String name) {

        RoomType roomType = roomTypeService.createRoomtype(name);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomType);
    }

    @GetMapping
    @Operation(summary = "Get all room types")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<List<RoomType>> getAllRoomType() {

        List<RoomType> roomTypes = roomTypeService.getAllRoomType();

        return ResponseEntity.ok(roomTypes);
    }

    @PutMapping("/{roomTypeId}")
    @Operation(summary = "Update room type")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<RoomType> updateRoomType(
            @PathVariable UUID roomTypeId,
            @RequestParam String name) {

        RoomType roomType =
                roomTypeService.updateRoomType(roomTypeId, name);

        return ResponseEntity.ok(roomType);
    }

    @DeleteMapping("/{roomTypeId}")
    @Operation(summary = "Delete room type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoomType(
            @PathVariable UUID roomTypeId) {
        roomTypeService.deleteRoomType(roomTypeId);

        return ResponseEntity.noContent().build();
    }
}