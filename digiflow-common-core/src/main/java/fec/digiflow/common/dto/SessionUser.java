package fec.digiflow.common.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SessionUser(String id, String username, List<String> authorities) {
}
