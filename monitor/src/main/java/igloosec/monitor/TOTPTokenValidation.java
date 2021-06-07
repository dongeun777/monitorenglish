package igloosec.monitor;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

public class TOTPTokenValidation {


    public static boolean validate(String inputCode, String userSecretKey) {
        String code = getTOTPCode(userSecretKey);
        return code.equals(inputCode);
    }

    public static String getTOTPCode(String userSecretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(userSecretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

}