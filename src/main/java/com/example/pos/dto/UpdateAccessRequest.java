package com.example.pos.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UpdateAccessRequest {
    private Set<String> roles;
    private Set<String> permissions;
}
