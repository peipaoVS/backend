package com.mmlm.useradmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] result = digest.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
                    StringBuilder builder = new StringBuilder();
                    for (byte value : result) {
                        String hex = Integer.toHexString(value & 0xff);
                        if (hex.length() == 1) {
                            builder.append('0');
                        }
                        builder.append(hex);
                    }
                    return builder.toString();
                } catch (NoSuchAlgorithmException exception) {
                    throw new IllegalStateException("SHA-256 不可用", exception);
                }
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encode(rawPassword).equals(encodedPassword);
            }
        };
    }
}
