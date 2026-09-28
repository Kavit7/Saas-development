package com.saas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class PropertyCategoryRequest {
      private String name;
      private String description;

}
