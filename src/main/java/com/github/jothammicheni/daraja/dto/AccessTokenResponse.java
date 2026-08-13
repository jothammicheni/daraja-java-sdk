package com.github.jothammicheni.daraja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record AccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") String expiresIn
) implements Serializable {
    private static final long serialVersionUID = 1L;
}