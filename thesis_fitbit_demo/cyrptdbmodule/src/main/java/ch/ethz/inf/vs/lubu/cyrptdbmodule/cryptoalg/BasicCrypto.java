package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.BlowfishEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

/**
 * Created by lukas on 09.03.15.
 * Basic Cryptographic Block Ciphers, implemented with SpongyCastle
 */
public class BasicCrypto {

    public static final int AES_BLOCK_BYTES = 16;

    public static final int BF_BLOCK_BYTES = 8;


    public static byte[] encrypt_AES(byte[] in, byte[] key) throws Exception {
        byte[] res = new byte[AES_BLOCK_BYTES];
        AESEngine aes = new AESEngine();
        aes.init(true, new KeyParameter(key));
        aes.processBlock(in, 0, res, 0);
        return res;
    }

    public static byte[] decrypt_AES(byte[] in, byte[] key) throws Exception {
        byte[] res = new byte[AES_BLOCK_BYTES];
        AESEngine aes = new AESEngine();
        aes.init(false, new KeyParameter(key));
        aes.processBlock(in, 0, res, 0);
        return res;
    }


    public static byte[] encrypt_BLOWFISH_CBC(byte[] in, byte[] key, byte[] iv, boolean doPad) throws Exception {
        byte[] plain = in;
        BufferedBlockCipher bf;
        boolean isExactSize = plain.length % BF_BLOCK_BYTES == 0;
        if (doPad) {
            bf = new PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
        } else {
            if (isExactSize)
                bf = new BufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
            else
                throw new IllegalArgumentException("Blocksize missmatch");
        }
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        bf.init(true, ivAndKey);
        return cipherData(bf, plain);
    }

    public static byte[] decrypt_BLOWFISH_CBC(byte[] in, byte[] key, byte[] iv, boolean isPad) throws Exception {
        byte[] cipher = in;
        BufferedBlockCipher bf;
        if (isPad)
            bf = new PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
        else
            bf = new BufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        bf.init(false, ivAndKey);
        return cipherData(bf, cipher);
    }


    public static byte[] encrypt_AES_CBC(byte[] in, byte[] key, byte[] iv, boolean doPad) throws Exception {
        byte[] plain = in;
        BufferedBlockCipher aes;
        if (!doPad)
            if (plain.length % AES_BLOCK_BYTES == 0)
                aes = new BufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            else
                throw new IllegalArgumentException("Blocksize missmatch");
        else
            aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        aes.init(true, ivAndKey);
        return cipherData(aes, plain);
    }

    public static byte[] decrypt_AES_CBC(byte[] in, byte[] key, byte[] iv, boolean isPad) throws Exception {
        byte[] cipher = in;
        BufferedBlockCipher aes;
        if (isPad)
            aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        else
            aes = new BufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        aes.init(false, ivAndKey);
        return cipherData(aes, cipher);
    }

    private static byte[] cipherData(BufferedBlockCipher cipher, byte[] data) throws Exception {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    public static byte[] encrypt_AES_CMC(byte[] in, byte[] key, byte[] iv) throws Exception {
        byte[] midRes = encrypt_AES_CBC(in, key, iv, true);
        return encrypt_AES_CBC(reverseAESBlocks(midRes), key, iv, false);
    }

    public static byte[] decrypt_AES_CMC(byte[] in, byte[] key, byte[] iv) throws Exception {
        byte[] midRes = decrypt_AES_CBC(in, key, iv, false);
        return decrypt_AES_CBC(reverseAESBlocks(midRes), key, iv, true);
    }

    private static byte[] discardLeadingSpaces(byte[] in, int max) {
        byte countBytes = 0;
        byte lastByte = in[in.length - 1];
        if (lastByte > max - 1 || lastByte <= 0)
            return in;
        byte before = lastByte;
        int ind = in.length - 1;
        for (; ind > 0; ind--) {
            if (countBytes == lastByte) {
                break;
            }
            if (before != in[ind])
                return in;
            before = in[ind];
            countBytes++;
        }
        byte[] out = new byte[ind + 1];
        System.arraycopy(in, 0, out, 0, out.length);
        return out;
    }

    private static byte[] reverseAESBlocks(byte[] in) {
        byte[] res = new byte[in.length];
        int noBlocks = in.length / AES_BLOCK_BYTES;

        for (int i = 0; i < noBlocks; i++)
            for (int j = 0; j < AES_BLOCK_BYTES; j++)
                res[i * (AES_BLOCK_BYTES) + j] = in[(AES_BLOCK_BYTES) * (noBlocks - 1 - i) + j];

        return res;
    }
}
