package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

/**
 * Created by lukas on 20.04.15.
 */
public class CryptoFactory {

    public static ICryptoManager getCryptoManager() {
        return new CryptoManager(new KeyManager());
    }

}
