package ventures.of.api.model;

import lombok.Data;

@Data
public class WowCryptoInfo {
    public byte[] salt;
    public byte[] verifier;

public WowCryptoInfo(byte[] salt, byte[] verifier) {
    this.salt = salt;
    this.verifier = verifier;
}

}
