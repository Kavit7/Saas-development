package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.RoomType;
import com.saas.backend.repositories.RoomTypeRepository;
import com.saas.backend.response.RoomTypeResponse;
import com.saas.backend.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    private RoomTypeResponse mapToResponse(RoomType rt) {
        if (rt == null) return null;
        return RoomTypeResponse.builder()
                .id(rt.getId())
                .name(rt.getName())
                .createdAt(rt.getCreatedAt())
                .updatedAt(rt.getUpdatedAt())
                .build();
    }

    @Override
    public RoomTypeResponse createRoomtype(String name) {
        boolean exists = roomTypeRepository.existsByNameIgnoreCase(name);
        if (exists) {
            throw new DuplicateException("The room type already exists");
        }

        RoomType roomType = new RoomType();
        roomType.setName(name);

        roomTypeRepository.save(roomType);
        return mapToResponse(roomType);
    }

    @Override
    public List<RoomTypeResponse> getAllRoomType() {
        return roomTypeRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponse updateRoomType(UUID roomTypeId, String name) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room type not found"));

        boolean exists = roomTypeRepository
                .existsByNameIgnoreCaseAndIdNot(name, roomTypeId);

        if (exists) {
            throw new DuplicateException("The room type already exists");
        }

        roomType.setName(name);
        roomTypeRepository.save(roomType);
        return mapToResponse(roomType);
    }

    @Override
    public void deleteRoomType(UUID roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room type not found"));

        roomTypeRepository.delete(roomType);
    }
}