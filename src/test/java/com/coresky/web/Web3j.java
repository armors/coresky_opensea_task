package com.coresky.web;

import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Locale;

import static org.web3j.crypto.Sign.signedMessageHashToKey;

/**
 * @Description: Web3j签名验签
 */
public class Web3j {
    // 钱包私钥
    private static final String priKey = "cfdb7480d367cca9a270d20753dcb3125986a79e98edd5486f4a71c33fbe4261";
    // 钱包地址
    private static final String walletAddress = "0x5ebacac108d665819398e5c37e12b0162d781398";

    public static void main(String[] args) {
        System.out.println(checkSign(
                "27",
                "0xa906450fcfce700c3c63190a7f27dffe5ab85dd0de0aa8eef8d7a6bcb8918164",
                "0x3b6929bfd1958ecf75fda9c94c59558dff1b48532cc83f1f268c7593e6fa55f5"
        ));
    }

    public static boolean checkSign(String vs, String r, String s){
        byte[] vv = Numeric.hexStringToByteArray(vs);
        byte v = vv[0];
        if (v < 27) {
            v += 27;
        }
        final Sign.SignatureData sd = new Sign.SignatureData(
                v,
                Numeric.hexStringToByteArray(r),
                Numeric.hexStringToByteArray(s)
        );
        String hashOrder = hashOrder();
        //hashOrder = "a906450fcfce700c3c63190a7f27dffe5ab85dd0de0aa8eef8d7a6bcb89181643b6929bfd1958ecf75fda9c94c59558dff1b48532cc83f1f268c7593e6fa55f51b";
        byte[] data = getEthereumMessageHash(Numeric.hexStringToByteArray(hashOrder));
        System.out.println("hashOrder->" + hashOrder);
        System.out.println("data->" + data.toString());
        return isSignatureValid(sd, data);
    }

    public static boolean isSignatureValid(Sign.SignatureData sd, final byte[] msgHash) {
        try {
            boolean match = false;
            String addressRecovered = null;
            // Iterate for each possible key to recover
            for (int i = 0; i < 4; i++) {
                final BigInteger publicKey = Sign.recoverFromSignature((byte) i, new ECDSASignature(
                        new BigInteger(1, sd.getR()),
                        new BigInteger(1, sd.getS())), msgHash);

                if (publicKey != null) {
                    addressRecovered = "0x" + Keys.getAddress(publicKey);
                    System.out.println("address: " + addressRecovered);
//                    if (addressRecovered.equalsIgnoreCase(address)) {
//                        match = true;
//                        break;
//                    }
                }
            }
            return match;
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }


    public static String hashOrder() {
        return Hash.sha3("aaaaaaaaaaaaaaaaaaaaaaaaaaaa").substring(2);
    }

    public static byte[] getEthereumMessagePrefix(int messageLength) {
        return "\u0019Ethereum Signed Message:\n".concat(String.valueOf(messageLength)).getBytes();
    }

    public static byte[] getEthereumMessageHash(byte[] message) {
        byte[] prefix = getEthereumMessagePrefix(message.length);
        byte[] result = new byte[prefix.length + message.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);
        return Hash.sha3(result);
    }
}