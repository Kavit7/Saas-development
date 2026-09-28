package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.RoomType;
import com.saas.backend.repositories.RoomTypeRepository;
import com.saas.backend.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    public RoomType createRoomtype(String name) {

        boolean exists = roomTypeRepository.existsByNameIgnoreCase(name);

        if (exists) {
            throw new DuplicateException("The room type already exists");
        }

        RoomType roomType = new RoomType();
        roomType.setName(name);

        return roomTypeRepository.save(roomType);
    }

    @Override
    public List<RoomType> getAllRoomType() {

        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType updateRoomType(UUID roomTypeId, String name) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room type not found"));

        boolean exists = roomTypeRepository
                .existsByNameIgnoreCaseAndIdNot(name, roomTypeId);

        if (exists) {
            throw new DuplicateException("The room type already exists");
        }

        roomType.setName(name);

        return roomTypeRepository.save(roomType);
    }

    @Override
    public void deleteRoomType(UUID roomTypeId) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room type not found"));

        roomTypeRepository.delete(roomType);
    }
}