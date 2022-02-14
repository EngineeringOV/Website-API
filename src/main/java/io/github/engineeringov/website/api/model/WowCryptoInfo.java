package io.github.engineeringov.website.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class WowCryptoInfo {
    private byte[] salt;
    private byte[] verifier;

public WowCryptoInfo(byte[] salt, byte[] verifier) {
    this.salt = salt;
    this.verifier = verifier;
}

}
