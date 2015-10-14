package ch.ethz.inf.vs.lubu.cyrptdbmodule.util;

/**
 * Created by lukas on 24.03.15.
 * General Settings for thw whole Module
 */
public class Setting {

    /**
     * Log in Android Log
     */
    public static final boolean LOGGING_ON = true;

    /**
     * Crypto on or off
     */
    public static boolean CRYPTO_ON = true;

    /**
     * Defines if Paillier or EC-ElGamal is used in
     * the HOM Layer
     */
    public static final boolean USE_PAILLIER = false;

    public static final int mOPE_PORT = 8082;
    public static final boolean USE_mOPE = true;

    /**
     * Defines the size of the Lookup-Table for EC-ElGamal (To solve ECDLP)
     * max 19
     */
    public static final String TABLE_POW_2_SIZE = "20";
    public static final int NUM_THREADS = 1;
}
