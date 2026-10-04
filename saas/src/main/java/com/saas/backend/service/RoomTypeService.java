package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.response.RoomTypeResponse;

public interface RoomTypeService {
    RoomTypeResponse createRoomtype(String name);
    List<RoomTypeResponse> getAllRoomType();
    RoomTypeResponse updateRoomType(UUID roomTypeId, String name);
    void deleteRoomType(UUID roomTypeId);
}
