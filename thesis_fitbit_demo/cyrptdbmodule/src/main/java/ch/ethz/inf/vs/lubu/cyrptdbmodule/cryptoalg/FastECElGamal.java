package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

/**
 * Created by lukas on 16.05.15.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyPair;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.ContextHolder;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.Setting;

/**
 * Implementation of EC-ElGamal with Baby Step Giant Step algorithm.
 * Look up table lies in native heap.
 * Also uses parallelism for better performance
 */
public class FastECElGamal extends NativeECElGamalCrypto {

    private static final int CURVE_ID = 0;

    static {
        System.loadLibrary("ecelgamal");
        setUP(CURVE_ID);
    }

    // Constants
    private static final int TABLE_SIZE_POW = Integer.valueOf(Setting.TABLE_POW_2_SIZE);

    private static final boolean LOAD_TABLE_FROM_FILE = true;


    private final NativeECELGamalPublicKey pubKey;

    private final NativeECELGamalPrivateKey privKey;

    private IPRNG rand;

    public FastECElGamal(KeyPair kp, IPRNG rand) throws IllegalArgumentException {
        NativeECELGamalPublicKey pub = null;
        NativeECELGamalPrivateKey priv = null;
        try {
            pub = (NativeECELGamalPublicKey) kp.getPublic();
            priv = (NativeECELGamalPrivateKey) kp.getPrivate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong Key");
        }
        this.pubKey = pub;
        this.privKey = priv;
        this.rand = rand;
    }

    @Override
    public NativeECElgamalCipher encrypt(BigInteger plain) {
        BigInteger k;
        String cipher;
        k = generateK();
        cipher = encryptNative(plain.toString(), k.toString(), this.pubKey.getY());
        return new NativeECElgamalCipher(cipher);

    }

    @Override
    public BigInteger decrypt(NativeECElgamalCipher cipher, int sizeNum) throws Exception {
        if (!TableLoader.isLoaded()) {
            loadLookUpTable();
        }

        String decryptPoint = decryptNative(cipher.getCipher(), privKey.getX().toString());
        int numStr = solveDiscreteLogBSGS(decryptPoint,sizeNum);

        return BigInteger.valueOf(numStr);
    }



    public static KeyPair generateKeys(IPRNG rand) {
        NativeECELGamalPrivateKey privKey;
        NativeECELGamalPublicKey pubKey;
        BigInteger x;
        String Y;

        BigInteger prime = getCurveOrderJ();
        x = rand.getRandMod(prime);
        privKey = new NativeECELGamalPrivateKey(x);
        Y = computePubKey(x.toString());
        pubKey = new NativeECELGamalPublicKey(CURVE_ID, Y);

        return new KeyPair(pubKey, privKey);
    }

    public static void loadLookUpTable() {
        if (LOAD_TABLE_FROM_FILE) {
            TableLoader.loadTableFast(1 << TABLE_SIZE_POW);
        } else {
            initTable();
            computeTable(1 << TABLE_SIZE_POW);
        }
    }

    private int solveDiscreteLogBSGS(String point, int sizeByte) throws Exception {
        if(Setting.NUM_THREADS>1) {
            return ParECDLPSolver.solveECDLP(point,sizeByte*8,TABLE_SIZE_POW);
        } else {
            int size;
            int exp =  (sizeByte * 8)-TABLE_SIZE_POW-1;
            if(exp<0)
                size = 1;
            else
                size = 1 << exp;
            return solveECDLPBsGs(point, size);
        }
    }

    @Override
    public NativeECElgamalCipher addCiphers(NativeECElgamalCipher cipherA, NativeECElgamalCipher cipherB) {
        String res = addCipherNative(cipherA.getCipher(), cipherB.getCipher());
        return new NativeECElgamalCipher(res);
    }

    private BigInteger generateK() {
        BigInteger num = getCurveOrderJ();
        return rand.getRandMod(num);
    }

    private static BigInteger getCurveOrderJ() {
        return new BigInteger(getCurveOrder());
    }

    private static BigInteger getPrimeOfGfJ() {
        return new BigInteger(getPrimeOfGf());
    }

    // NATIVE

    public static native void initTable();
    public static  native void destroyTable();
    public static native int putInTable(String key, int value);
    public static native int getFromTable(String key);
    public static native void computeTable(int size);
    public static native void setTableSizeConstants(int size);

    public static native int  solveECDLPBsGs(String m, int maxIt);

    private native static void setUP(int curveNr);
    public native static void tearDOWN();

    private native static String computePubKey(String secret);

    private static native String getCurveOrder();
    private static native String getPrimeOfGf();

    private static native String encryptNative(String plain, String k, String pubKey);
    private static native String decryptNative(String cipher,String secret);
    private static native String addCipherNative(String cipher1,String cipher2);

    public static native String computeGenTimes(String num);

    // Par Stuff
    private static class ResultState {
        public int found = 0;
        public int result = 0;
    }
    private static native int solveECDLPBsGsPos(ResultState state, String M, int starti, int endI) throws Exception;
    private static native int solveECDLPBsGsNeg(ResultState state, String M, int starti, int endI) throws Exception;

    // Classes for Keys and Cipher, Loader

    /**
     * ECDLP Solver solves the Problem for signed Integers in parallel
     */
    private static class ParECDLPSolver {

        private static final int NUM_THREADS_PERSIDE = Setting.NUM_THREADS / 2;

        private static final long MAX_WAIT_TIME_BEFORE_ERROR = 3000;

        public static int solveECDLP(final String M, final int sizePow, int tableSize) throws Exception {
            ResultState state = new ResultState();
            Thread[] threads = new Thread[NUM_THREADS_PERSIDE*2 - 1];
            int sideRange = 1<<(sizePow-1-tableSize);
            int maxIt = sideRange/NUM_THREADS_PERSIDE;
            int curStart = 0;
            int curEnd= maxIt;

            for(int i = 0; i<NUM_THREADS_PERSIDE; i++) {
                threads[i] = createThread(state, M, curStart, curEnd, true);
                if(i!=0) {
                    threads[i+NUM_THREADS_PERSIDE-1] = createThread(state, M, curStart, curEnd, false);
                }
                curStart+=maxIt;
                curEnd+=maxIt;
            }

            for(Thread t : threads) {
                t.start();
            }

            int res = 0;
            boolean error = false;
            try {
                res = solveECDLPBsGsPos(state, M, 0, maxIt);
            } catch(Exception e) {
                error = true;
            }
            if(!error) {
                state.found=1;
                state.result = res;
            } else {
                long startime = System.currentTimeMillis();
                while(!(state.found==1)) {
                    if(System.currentTimeMillis()-startime>MAX_WAIT_TIME_BEFORE_ERROR) {
                        throw new RuntimeException("Result not found");
                    }
                    Thread.sleep(100);
                }
            }

            return state.result;
        }

        private static Thread createThread(final ResultState state, final String M, final int from, final int to, final boolean isNeg) {
            return new Thread() {

                private boolean error = false;

                @Override
                public void run() {
                    int res = 0;
                    try {
                        if(isNeg)
                            res = solveECDLPBsGsNeg(state, M, from, to);
                        else
                            res = solveECDLPBsGsPos(state, M, from, to);
                    } catch(Exception e) {
                        error = true;
                    }
                    if(!error) {
                        state.found = 1;
                        state.result = res;
                    }
                    super.run();
                }

            };
        }
    }


    public static class TableLoader {

        private static final String TABLE_NAME_FAST = "table2pow19";
        private static final String TABLE_NAME_FAST_EXT = "table2pow19_20";

        private static boolean tableFastLoaded = false;

        public static void loadTableFast(int size) {
            int rawId = ContextHolder.getContext().getResources().getIdentifier(TABLE_NAME_FAST, "raw", ContextHolder.getContext().getPackageName());
            int rawIdext = ContextHolder.getContext().getResources().getIdentifier(TABLE_NAME_FAST_EXT, "raw", ContextHolder.getContext().getPackageName());
            InputStream inputS= null;
            InputStreamReader buffreader= null;
            BufferedReader reader = null;
            BufferedReader readerExt = null;
            FastECElGamal.initTable();
            try {
                boolean needExt = false;
                if(Setting.TABLE_POW_2_SIZE.equals("20")) {
                    needExt = true;
                    size = 1<<19;
                }
                inputS = ContextHolder.getContext().getResources().openRawResource(rawId);
                buffreader = new InputStreamReader(inputS);
                reader = new BufferedReader(buffreader);
                String line;
                int count = 0;
                while((line=reader.readLine())!=null && count <= size) {
                    FastECElGamal.putInTable(line, count);
                    count++;
                }
                if(needExt) {
                    readerExt = new BufferedReader(new InputStreamReader(ContextHolder.getContext().getResources().openRawResource(rawIdext)));

                    while((line=readerExt.readLine())!=null && count <= 1<<20) {
                        FastECElGamal.putInTable(line, count);
                        count++;
                    }

                    FastECElGamal.setTableSizeConstants(1<<20);
                } else {
                    FastECElGamal.setTableSizeConstants(size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(inputS!=null)
                        inputS.close();
                    if(buffreader!=null)
                        buffreader.close();
                    if(reader!=null)
                        reader.close();
                    if(readerExt!=null)
                        readerExt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            tableFastLoaded = true;
        }

        public static boolean isLoaded() {
            return tableFastLoaded;
        }

        public static void freeTables() {
            if(isLoaded())
                FastECElGamal.destroyTable();
            tableFastLoaded = false;
        }
    }
}
