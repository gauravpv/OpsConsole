package com.opsconsole.tester;

import com.opsconsole.tester.util.AesCbcCrypto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesCbcCryptoTest {

    @Test
    void encryptDecrypt_roundTrip() {
        String key = "2026Unpf7T7Mr4kNAHecXKolYoD9tiOT";
        String iv = "2026JHNjiJSboivg";
        String plain = "{\"channel\":\"hqoYIrMWpVbGXPh2pqBKJxfTo110as\"}";

        String encrypted = AesCbcCrypto.encryptUtf8(plain, key, iv);
        String decrypted = AesCbcCrypto.decryptUtf8(encrypted, key, iv);

        assertThat(decrypted).isEqualTo(plain);
    }
}
