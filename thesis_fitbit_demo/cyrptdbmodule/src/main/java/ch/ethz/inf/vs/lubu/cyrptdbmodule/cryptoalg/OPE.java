package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;

import com.google.common.base.Predicate;

import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 11.03.15.
 * Implementation of the OPE Scheme from CryptDB
 * This code is partly ported from CryptDB
 * http://css.csail.mit.edu/cryptdb/
 */
public class OPE {

    private int pbits, cbits;

    private byte[] key;

    private Map<BigInteger, BigInteger> dgap_cache;

    private HMac hmac = null;
    private int funcSize;

    private HypGeoDistFast hgd = new HypGeoDistFast();


    public OPE(byte[] key, int plainBits, int cipherBits) {
        this.pbits = plainBits;
        this.cbits = cipherBits;
        this.key = key;
        dgap_cache = new HashMap<BigInteger, BigInteger>(1000);
        Digest func = new SHA256Digest();
        hmac = new HMac(func);
        hmac.init(new KeyParameter(key));
        funcSize = func.getDigestSize();
    }


    public BigInteger domain_gap(BigInteger ndomain, BigInteger nrange, BigInteger rgap, PRNGAesBlock prng) {
        //Stopwatch s = Stopwatch.createUnstarted();
        //s.start();
        BigInteger res = hgd.HGD(rgap, ndomain, nrange.subtract(ndomain), prng);
        //s.stop();
        //Log.i("HDG",String.valueOf(s.elapsed(TimeUnit.MILLISECONDS)));
        return res;
    }


    private OpeDomainRange lazy_sample(BigInteger d_lo, BigInteger d_hi, BigInteger r_lo, BigInteger r_hi, Predicate<BigInteger[]> go_low, PRNGAesBlock prng) {
        BigInteger ndomain = d_hi.subtract(d_lo).add(BigInteger.ONE);
        BigInteger nrange = r_hi.subtract(r_lo).add(BigInteger.ONE);
        //throw_c(nrange >= ndomain);

        if (ndomain.compareTo(BigInteger.ONE) == 0)
            return new OpeDomainRange(d_lo, r_lo, r_hi);


        String arg = d_lo.toString() + "/" + d_hi.toString() + "/" + r_lo.toString() + "/" + r_hi.toString();
        byte[] hash = performHmacFunc(arg);
        prng.setCtr(hash);

        BigInteger rgap = nrange.divide(BigInteger.valueOf(2));
        BigInteger dgap;

        BigInteger sum = r_lo.add(rgap);
        if (!dgap_cache.containsKey(sum)) {
            dgap = domain_gap(ndomain, nrange, nrange.divide(BigInteger.valueOf(2)), prng);
            dgap_cache.put(sum, dgap);

        } else {
            dgap = dgap_cache.get(sum);
        }

        BigInteger sum1, sum2;
        sum1 = d_lo.add(dgap);
        sum2 = r_lo.add(rgap);
        if (go_low.apply(new BigInteger[]{sum1, sum2}))
            return lazy_sample(d_lo, sum1.subtract(BigInteger.ONE), r_lo, sum2.subtract(BigInteger.ONE), go_low, prng);
        else
            return lazy_sample(sum1, d_hi, sum2, r_hi, go_low, prng);
    }

    private OpeDomainRange search(Predicate<BigInteger[]> go_low) {
        PRNGAesBlock rand = new PRNGAesBlock(key);
        return lazy_sample(BigInteger.ZERO, BigInteger.ONE.shiftLeft(pbits), BigInteger.ZERO, BigInteger.ONE.shiftLeft(cbits), go_low, rand);
    }

    private byte[] performHmacFunc(String s) {
        hmac.update(s.getBytes(), 0, s.getBytes().length);
        byte[] resBuf = new byte[funcSize];
        hmac.doFinal(resBuf, 0);
        return resBuf;
    }


    public BigInteger encrypt(BigInteger ptext) {
        final BigInteger ptemp = ptext;
        Predicate<BigInteger[]> pred = new Predicate<BigInteger[]>() {
            public boolean apply(BigInteger[] nums) {
                return (ptemp.compareTo(nums[0]) == -1);
            }
        };

        OpeDomainRange dr = search(pred);

        Digest func = new SHA256Digest();
        func.update(ptext.toString().getBytes(), 0, ptext.toString().getBytes().length);
        byte[] res = new byte[func.getDigestSize()];
        func.doFinal(res, 0);

        PRNGAesBlock rand = new PRNGAesBlock(key);
        rand.setCtr(res);

        BigInteger nrange = dr.r_hi.subtract(dr.r_lo).add(BigInteger.ONE);
        return dr.r_lo.add(rand.getRandModHGD(nrange));
    }


    public BigInteger decrypt(BigInteger ctext) {
        final BigInteger ctemp = ctext;
        Predicate<BigInteger[]> pred = new Predicate<BigInteger[]>() {
            public boolean apply(BigInteger[] nums) {
                return (ctemp.compareTo(nums[1]) == -1);
            }
        };
        OpeDomainRange dr = search(pred);
        return dr.d;
    }


    public class OpeDomainRange {
        private BigInteger d, r_lo, r_hi;

        public OpeDomainRange(BigInteger d_arg, BigInteger r_lo_arg, BigInteger r_hi_arg) {
            this.d = d_arg;
            this.r_lo = r_lo_arg;
            this.r_hi = r_hi_arg;
        }
    }

    ;


}
