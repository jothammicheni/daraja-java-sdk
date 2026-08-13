package com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record AccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") String expiresIn
) implements Serializable { // ⚡ FIXED: Added for seamless distributed object streaming on cloud clusters

    private static final long serialVersionUID = 1L;
}
