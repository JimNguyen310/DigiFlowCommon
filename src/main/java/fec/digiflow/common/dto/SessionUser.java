package fec.digiflow.common.dto;

import java.util.List;

public record SessionUser(String id, String username, List<String> authorities) {
}

