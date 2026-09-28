package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.models.RoomType;

public interface RoomTypeService {
    RoomType createRoomtype(String name);
    List<RoomType> getAllRoomType();
     RoomType updateRoomType(UUID roomTypeId, String name);
     void deleteRoomType(UUID roomTypeId);
    
}
