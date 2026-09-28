package com.saas.backend.response;

import lombok.AllArgsConstructor;
import lombok.Data;

;

/**
 * RoomRequirementResponse
 */

@Data 
@AllArgsConstructor 
public class RoomRequirementResponse {
    private String roomType;
    private Integer quantity;
}
