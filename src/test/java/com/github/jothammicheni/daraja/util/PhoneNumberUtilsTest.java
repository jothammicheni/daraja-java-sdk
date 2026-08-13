package com.github.jothammicheni.daraja.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberUtilsTest {

    @Test
    void shouldValidateAllKenyanPhoneFormats() {
        assertThat(PhoneNumberUtils.isValidKenyanPhone("+254712345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("254712345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("0712345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("712345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("+254112345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("254112345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("0112345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("112345678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("+254 712 345 678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("0712-345-678")).isTrue();
        assertThat(PhoneNumberUtils.isValidKenyanPhone("(254)712345678")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+254712345678",
            "254712345678",
            "0712345678",
            "712345678",
            "+254 712 345 678",
            "0712-345-678"
    })
    void shouldValidateValidPhoneNumbers(String phoneNumber) {
        assertThat(PhoneNumberUtils.isValidKenyanPhone(phoneNumber)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+255712345678",
            "255712345678",
            "+25471234567",
            "+2547123456789",
            "+254512345678",
            "0512345678",
            "12345678",
            "abc123",
            "+2547abcdefg"
    })
    void shouldInvalidateWrongPhoneNumbers(String phoneNumber) {
        assertThat(PhoneNumberUtils.isValidKenyanPhone(phoneNumber)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void shouldInvalidateNullOrEmptyPhoneNumbers(String phoneNumber) {
        assertThat(PhoneNumberUtils.isValidKenyanPhone(phoneNumber)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "+254712345678, 254712345678",
            "254712345678, 254712345678",
            "0712345678, 254712345678",
            "712345678, 254712345678",
            "+254 712 345 678, 254712345678",
            "0712-345-678, 254712345678",
            "+254112345678, 254112345678",
            "254112345678, 254112345678",
            "0112345678, 254112345678",
            "112345678, 254112345678",
            "+254 (0712) 345-678, 254712345678",
            "+2547123456789012, 254712345678"
    })
    void shouldNormalizePhoneNumbersCorrectly(String input, String expected) {
        assertThat(PhoneNumberUtils.normalizeKenyanPhone(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456",
            "abc123",
            "+2547abcdefg"
    })
    void shouldThrowExceptionForInvalidNormalization(String phoneNumber) {
        assertThatThrownBy(() -> PhoneNumberUtils.normalizeKenyanPhone(phoneNumber))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForNullAndEmptyNormalization() {
        assertThatThrownBy(() -> PhoneNumberUtils.normalizeKenyanPhone(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PhoneNumberUtils.normalizeKenyanPhone(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PhoneNumberUtils.normalizeKenyanPhone(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDetectPrepaidPhoneTypes() {
        assertThat(PhoneNumberUtils.detectPhoneType("+254712345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_PREPAID);
        assertThat(PhoneNumberUtils.detectPhoneType("0712345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_PREPAID);
        assertThat(PhoneNumberUtils.detectPhoneType("712345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_PREPAID);
    }

    @Test
    void shouldDetectPostpaidPhoneTypes() {
        assertThat(PhoneNumberUtils.detectPhoneType("+254112345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_POSTPAID);
        assertThat(PhoneNumberUtils.detectPhoneType("0112345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_POSTPAID);
        assertThat(PhoneNumberUtils.detectPhoneType("112345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.SAFARICOM_POSTPAID);
    }

    @Test
    void shouldDetectUnknownPhoneTypes() {
        assertThat(PhoneNumberUtils.detectPhoneType("+254512345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.UNKNOWN);
        assertThat(PhoneNumberUtils.detectPhoneType("0512345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.UNKNOWN);
        assertThat(PhoneNumberUtils.detectPhoneType("+255712345678"))
                .isEqualTo(PhoneNumberUtils.PhoneType.UNKNOWN);
    }

    @Test
    void shouldDetectInvalidPhoneTypes() {
        assertThat(PhoneNumberUtils.detectPhoneType("123"))
                .isEqualTo(PhoneNumberUtils.PhoneType.INVALID);
        assertThat(PhoneNumberUtils.detectPhoneType("abc"))
                .isEqualTo(PhoneNumberUtils.PhoneType.INVALID);
        assertThat(PhoneNumberUtils.detectPhoneType(null))
                .isEqualTo(PhoneNumberUtils.PhoneType.INVALID);
        assertThat(PhoneNumberUtils.detectPhoneType(""))
                .isEqualTo(PhoneNumberUtils.PhoneType.INVALID);
    }

    @ParameterizedTest
    @CsvSource({
            "+254712345678, ******5678",
            "0712345678, ******5678",
            "254712345678, ******5678",
            "+254112345678, ******5678",
            "123, ****",
            "12, ****"
    })
    void shouldMaskPhoneNumbers(String input, String expected) {
        assertThat(PhoneNumberUtils.maskPhoneNumber(input)).isEqualTo(expected);
    }

    @Test
    void shouldMaskNullAndEmptyPhoneNumbers() {
        assertThat(PhoneNumberUtils.maskPhoneNumber(null)).isEqualTo("****");
        assertThat(PhoneNumberUtils.maskPhoneNumber("")).isEqualTo("****");
        assertThat(PhoneNumberUtils.maskPhoneNumber("123")).isEqualTo("****");
        assertThat(PhoneNumberUtils.maskPhoneNumber("12")).isEqualTo("****");
    }

    @Test
    void shouldCleanPhoneNumbersWithVariousFormats() {
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+254 712 345 678"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("0712-345-678"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("(254)712345678"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+254.712.345.678"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+254 (0712) 345-678"))
                .isEqualTo("254712345678");
    }

    @Test
    void shouldHandleEdgeCases() {
        // Extremely long numbers should truncate correctly
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+2547123456789012"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+2540712345678"))
                .isEqualTo("254712345678");
        assertThat(PhoneNumberUtils.normalizeKenyanPhone("+254 0 712 345 678"))
                .isEqualTo("254712345678");
    }

    @Test
    void shouldHaveDisplayNamesForPhoneTypes() {
        assertThat(PhoneNumberUtils.PhoneType.SAFARICOM_PREPAID.getDisplayName()).isEqualTo("Prepaid");
        assertThat(PhoneNumberUtils.PhoneType.SAFARICOM_POSTPAID.getDisplayName()).isEqualTo("Postpaid");
        assertThat(PhoneNumberUtils.PhoneType.UNKNOWN.getDisplayName()).isEqualTo("Unknown");
        assertThat(PhoneNumberUtils.PhoneType.INVALID.getDisplayName()).isEqualTo("Invalid");
    }
}