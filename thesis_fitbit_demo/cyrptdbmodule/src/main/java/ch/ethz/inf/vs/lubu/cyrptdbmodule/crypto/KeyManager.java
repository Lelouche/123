package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg.BasicCrypto;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.exceptions.CDBException;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;

/**
 * Created by lukas on 30.03.15.
 */
public class KeyManager implements IKeyManager {


    //Not safe, just simple to push it in SharedPerfs
    private static final String alias = "cdbkey";
    private static final String sharePrefFile = "cdbfile";

    private byte[] mk = null;

    private SharedPreferences pref;

    public KeyManager() {
        pref = ContextHolder.getContext().getSharedPreferences(sharePrefFile, Context.MODE_PRIVATE);
    }

    private byte[] loadMK() throws CDBException {
        byte[] key = getKeyFromPref();

        if (key == null) {
            try {
                generateKey();
            } catch (Exception e) {
                throw new CDBException(e.getMessage());
            }
            key = getKeyFromPref();
        }
        return key;
    }

    private void generateKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        writeToPref(secretKey);
    }

    private void writeToPref(SecretKey k) {
        String store = Base64.encodeToString(k.getEncoded(), Base64.DEFAULT);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(alias, store);
        editor.apply();
    }

    private byte[] getKeyFromPref() {
        String key = pref.getString(alias, null);
        if (key == null)
            return null;

        return Base64.decode(key, Base64.DEFAULT);
    }

    @Override
    public CDBKey getKey(EncLayer.EncLayerType type, CDBColumn col) throws CDBException {
        byte[] res = new byte[BasicCrypto.AES_BLOCK_BYTES];
        if (this.mk == null)
            this.mk = loadMK();
        String id;
        if (type.isJOIN()) {
            ArrayList<String> hashes = new ArrayList<>();
            hashes.add(col.getHashName(type));
            for (CDBColumn joinTo : col.getJoinableCols()) {
                hashes.add(joinTo.getHashName(type));
            }
            id = CDBUtil.createCommonName(hashes) + "JOIN";
        } else {
            id = col.getHashName(type);
        }
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        md.update(id.getBytes());
        byte[] shaDig = md.digest();

        System.arraycopy(shaDig, 0, res, 0, BasicCrypto.AES_BLOCK_BYTES);

        CDBKey finalKey = null;
        try {
            finalKey = new CDBKey(BasicCrypto.encrypt_AES(res, mk));
        } catch (Exception e) {
            throw new CDBException(e.getMessage());
        }

        return finalKey;
    }


}
