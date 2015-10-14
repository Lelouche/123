package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by lukas on 16.05.15.
 */
public abstract class NativeECElGamalCrypto {

    public abstract NativeECElgamalCipher encrypt(BigInteger plain) throws Exception;

    public abstract BigInteger decrypt(NativeECElgamalCipher cipher, int sizeNum) throws Exception;

    public abstract NativeECElgamalCipher addCiphers(NativeECElgamalCipher cipherA, NativeECElgamalCipher cipherB);

    public static class NativeECElgamalCipher {

        private static final String DELIM = "?";

        private final String R;

        private final String S;

        public NativeECElgamalCipher(String cipher) throws IllegalArgumentException {
            String[] split = cipher.split("\\" + DELIM);
            if (split == null || split.length != 2)
                throw new IllegalArgumentException("Wrong cipher format");
            this.R = split[0];
            this.S = split[1];
        }

        public String getForDB() {
            return R + DELIM + S;
        }

        public String getCipher() {
            return R + DELIM + S;
        }

        public String getR() {
            return R;
        }

        public String getS() {
            return S;
        }

    }

    public static class NativeECELGamalPublicKey implements PublicKey {

        private final int curve;

        private final String Y;

        public NativeECELGamalPublicKey(int curve, String Y) {
            this.curve = curve;
            this.Y = Y;
        }

        public int getCurve() {
            return curve;
        }

        public String getY() {
            return Y;
        }

        @Override
        public String getAlgorithm() {
            return "EC-ElGamal";
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    }

    public static class NativeECELGamalPrivateKey implements PrivateKey {

        private final BigInteger x;

        public NativeECELGamalPrivateKey(BigInteger x) {
            this.x = x;
        }

        public NativeECELGamalPrivateKey(String x) {
            this.x = new BigInteger(x);
        }

        public BigInteger getX() {
            return x;
        }

        @Override
        public String getAlgorithm() {
            return "EC-ELGamal";
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    }

}
