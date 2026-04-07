package com.ms.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Meta {
    private String timestamp;
    private String requestId;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}