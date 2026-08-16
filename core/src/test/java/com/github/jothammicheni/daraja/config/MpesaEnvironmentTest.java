package com.github.jothammicheni.daraja.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MpesaEnvironmentTest {

    @Test
    void shouldHaveTwoEnvironments() {
        MpesaEnvironment[] values = MpesaEnvironment.values();
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(MpesaEnvironment.SANDBOX, MpesaEnvironment.PRODUCTION);
    }

    @Test
    void shouldGetCorrectOrdinal() {
        assertThat(MpesaEnvironment.SANDBOX.ordinal()).isEqualTo(0);
        assertThat(MpesaEnvironment.PRODUCTION.ordinal()).isEqualTo(1);
    }

    @Test
    void shouldGetCorrectName() {
        assertThat(MpesaEnvironment.SANDBOX.name()).isEqualTo("SANDBOX");
        assertThat(MpesaEnvironment.PRODUCTION.name()).isEqualTo("PRODUCTION");
    }

    @Test
    void shouldConvertFromString() {
        assertThat(MpesaEnvironment.valueOf("SANDBOX")).isEqualTo(MpesaEnvironment.SANDBOX);
        assertThat(MpesaEnvironment.valueOf("PRODUCTION")).isEqualTo(MpesaEnvironment.PRODUCTION);
    }
}