package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import android.util.Log;

import java.math.BigInteger;
import java.security.KeyPair;

/**
 * Created by lukas on 11.03.15.
 * Implementation of the private part of the Paillier Crypto-System
 * This code is partly ported from CryptDB
 * http://css.csail.mit.edu/cryptdb/
 */
public class PaillierPriv extends Paillier {

    /* Private key, including g from public part; n=pq */
    private BigInteger p, q;
    private BigInteger a;      /* non-zero for fast mode */

    /* Cached values */
    private boolean fast = false;
    private BigInteger p2, q2;
    private BigInteger two_p, two_q;
    private BigInteger pinv, qinv;
    private BigInteger hp, hq;

    public PaillierPriv(KeyPair sk) {
        super((PaillierPubKey) sk.getPublic());
        PaillierPrivKey priv = (PaillierPrivKey) sk.getPrivate();
        p = priv.getP();
        q = priv.getQ();
        a = priv.getA();
        fast = (a.compareTo(BigInteger.ZERO) != 0);
        p2 = p.multiply(p);
        q2 = q.multiply(q);
        BigInteger constTwo = BigInteger.valueOf(2);
        two_p = constTwo.pow(numBits(p));
        two_q = constTwo.pow(numBits(q));
        pinv = p.modInverse(two_p);
        qinv = q.modInverse(two_q);
        if (fast) {
            hp = Lfast((g.mod(p2)).modPow(a, p2), pinv, two_p, p).modInverse(p);
            hq = Lfast((g.mod(q2)).modPow(a, q2), qinv, two_q, q).modInverse(q);
        } else {
            hp = Lfast((g.mod(p2)).modPow(p.subtract(BigInteger.ONE), p2), pinv, two_p, p).modInverse(p);
            hq = Lfast((g.mod(q2)).modPow(p.subtract(BigInteger.ONE), q2), qinv, two_q, q).modInverse(q);
        }
    }


    public static KeyPair keygen(IPRNG IPRNG, int nbits, int abits) {
        BigInteger p, q, n, g, a;

        do {
            if (abits != 0) {
                a = IPRNG.getRandPrime(abits);

                BigInteger cp = IPRNG.getRandomNumber((nbits / 2) - abits);
                BigInteger cq = IPRNG.getRandomNumber((nbits / 2) - abits);

                p = (a.multiply(cp)).add(BigInteger.ONE);

                while (!p.isProbablePrime(25))
                    p = p.add(a);

                q = (a.multiply(cq)).add(BigInteger.ONE);
                while (!q.isProbablePrime(25))
                    q = q.add(a);
            } else {
                a = BigInteger.ZERO;
                p = IPRNG.getRandPrime(nbits / 2);
                q = IPRNG.getRandPrime(nbits / 2);
            }
            n = p.multiply(q);
            Log.i("DEBUG", String.valueOf(numBits(n)) + " =? " + String.valueOf(nbits) + "  " + String.valueOf(p.compareTo(q)));
        } while ((nbits != numBits(n)) || p.compareTo(q) == 0);

        if (p.compareTo(q) == 1) {
            BigInteger temp = p;
            p = q;
            q = temp;
        }

        BigInteger lambda = LCM(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

        if (abits != 0) {
            g = BigInteger.valueOf(2).modPow(lambda.divide(a), n);
        } else {
            g = BigInteger.ONE;
            do {
                g = g.add(BigInteger.ONE);
            } while (L(g.modPow(lambda, n.multiply(n)), n).gcd(n).compareTo(BigInteger.ONE)!=0);
        }
        Log.i("DEBUG", "\nG: " + g.toString() + "\nn: " + n.toString() + "\np: " + p.toString() + "\nq: " + q.toString());

        PaillierPubKey pubk = new PaillierPubKey(n, g);
        PaillierPrivKey privk = new PaillierPrivKey(p, q, a);

        return new KeyPair(pubk, privk);
    }

    private static int numBits(BigInteger a) {
        return a.bitLength();
    }

    public BigInteger decrypt(BigInteger ciphertext) {
        BigInteger mp, mq;

        if (fast) {
            mp = (Lfast((ciphertext.mod(p2)).modPow(a, p2), pinv, two_p, p).multiply(hp)).mod(p);
            mq = (Lfast((ciphertext.mod(q2)).modPow(a, q2), qinv, two_q, q).multiply(hq)).mod(q);
        } else {
            mp = (Lfast((ciphertext.mod(p2)).modPow(p.add(BigInteger.valueOf(-1)), p2), pinv, two_p, p).multiply(hp)).mod(p);
            mq = (Lfast((ciphertext.mod(q2)).modPow(q.add(BigInteger.valueOf(-1)), q2), qinv, two_q, q).multiply(hq)).mod(q);
        }

        BigInteger m, pq;
        m = BigInteger.ZERO;
        pq = BigInteger.ONE;

        BigInteger[] res1 = CRT(m, pq, mp, p);
        m = res1[0];
        pq = res1[1];
        BigInteger[] res2 = CRT(m, pq, mq, q);
        m = res2[0];
        return m;
    }

    private static BigInteger L(BigInteger u, BigInteger n) {
        return (u.subtract(BigInteger.ONE)).divide(n);
    }

    private static BigInteger Lfast(BigInteger u, BigInteger ninv, BigInteger two_n, BigInteger n) {
        return (((u.subtract(BigInteger.ONE)).multiply(ninv)).mod(two_n)).mod(n);
    }

    private static BigInteger LCM(BigInteger a, BigInteger b) {
        return (a.multiply(b)).divide(a.gcd(b));
    }

    /**
     * Ported from the NTL library
     *
     * @param gg
     * @param a
     * @param G
     * @param p
     * @return
     */
    private static BigInteger[] CRT(BigInteger gg, BigInteger a, BigInteger G, BigInteger p) {

        BigInteger g;

        if (!CRTInRange(gg, a)) {
            BigInteger a1;
            g = gg.mod(a);// g = gg%a
            a1 = a.shiftRight(1);// a1 = (a >> 1)
            if (g.compareTo(a1) == 1)
                g = g.add(a.negate());
        } else
            g = gg;


        BigInteger p1;
        p1 = p.shiftRight(1);

        BigInteger a_inv;
        a_inv = a.mod(p);
        a_inv = a_inv.modInverse(p); // a_inv = a_inv^{-1} mod p, 0 <= a_inv < p

        BigInteger h;
        h = g.mod(p);

        h = (G.add(h.negate())).mod(p); // return h = (G-h)%p

        h = (h.multiply(a_inv)).mod(p); // return h = (h*a_inv)%p

        if (h.compareTo(p1) == 1)
            h = h.add(p.negate());

        if (!(h.compareTo(BigInteger.valueOf(0)) == 0)) {
            BigInteger ah;
            ah = a.multiply(h);

            if ((p.mod(BigInteger.valueOf(2)).compareTo(BigInteger.valueOf(0)) == 0) && g.compareTo(BigInteger.valueOf(0)) == 1 && (h.compareTo(p1)) == 0)
                g = g.add(ah.negate());
            else
                g = g.add(ah);
        }

        a = a.multiply(p);
        gg = g;
        BigInteger[] res = new BigInteger[2];
        res[0] = gg;
        res[1] = a;
        return res;
    }

    private static boolean CRTInRange(BigInteger g, BigInteger a) {
        boolean c1, c2, c3;
        BigInteger ahalf = a.divide(BigInteger.valueOf(2));
        c1 = a.compareTo(BigInteger.ZERO) == 1;
        c2 = g.compareTo(ahalf) <= 0;
        c3 = g.compareTo(ahalf.negate()) == 1;
        return c1 && c2 && c3;
    }

}