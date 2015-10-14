package ch.ethz.inf.vs.lubu.cyrptdbmodule.cryptoalg;


import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Created by lukas on 23.03.15.
 * Computes the Hypergeometric-Distribution for the OPE Scheme
 * This code is ported from CryptDB
 * http://css.csail.mit.edu/cryptdb/
 */
public class HypGeoDistFast {
    private int precision = 10;

    public HypGeoDistFast(int precision) {
        this.precision = precision;
    }

    public HypGeoDistFast() {
    }

    /**
     * KK is the number of elements drawn from an urn where there are NN1 white
     * balls and NN2 black balls; the result is the number of white balls in
     * the KK sample.
     * <p/>
     * The implementation is based on an adaptation of the H2PEC alg for large
     */
    public BigInteger HGD(BigInteger KK, BigInteger NN1, BigInteger NN2, IPRNG prng) {
        BigInteger sum = NN1.add(NN2).add(KK);
        precision = sum.bitLength() + 10;

        Apfloat Ap_ONE = Apfloat.ONE;
        Apfloat Ap_ZFIVE = new Apfloat(0.5, precision);
        Apfloat Ap_THREE = new Apfloat(3, precision);
        Apfloat Ap_ZTFIVE = new Apfloat(0.25, precision);


        Apfloat JX;      // the result
        Apfloat TN, N1, N2, K;
        Apfloat P, U, V, A, IX, XL, XR, M;
        Apfloat KL, KR, LAMDL, LAMDR, NK, NM, P1, P2, P3;

        boolean REJECT;
        Apfloat MINJX, MAXJX;

        Apfloat CON = new Apfloat(57.56462733, precision);
        Apfloat DELTAL = new Apfloat(0.0078, precision);
        Apfloat DELTAU = new Apfloat(0.0034, precision);
        Apfloat SCALE = new Apfloat(1.0e25, precision);

        REJECT = true;
        IX = Apfloat.ZERO;

        if (NN1.compareTo(NN2) >= 0) {
            N1 = new Apfloat(NN2, precision);
            N2 = new Apfloat(NN1, precision);
        } else {
            N1 = new Apfloat(NN1, precision);
            N2 = new Apfloat(NN2, precision);
        }


        TN = N1.add(N2);

        Apfloat doubleKK = new Apfloat(KK.add(KK), precision);
        if (doubleKK.compareTo(TN) >= 0) {
            K = TN.subtract(to_RR(KK));
        } else {
            K = to_RR(KK);
        }

        M = ((K.add(Ap_ONE)).multiply(N1.add(Ap_ONE))).divide(TN.add(new Apfloat(2, precision)));

        Apfloat subtract = K.subtract(N2);
        if (subtract.compareTo(Apfloat.ZERO) == -1) {
            MINJX = Apfloat.ZERO;
        } else {
            MINJX = subtract;
        }

        if (N1.compareTo(K) == -1) {
            MAXJX = N1;
        } else {
            MAXJX = K;
        }

        if (MINJX.compareTo(MAXJX) == 0) {
            IX = MAXJX;
        } else if ((M.subtract(MINJX)).compareTo(new Apfloat(10, precision)) == -1) {
            Apfloat W;
            if (K.compareTo(N2) == -1) {
                W = exp(CON.add(AFC(N2)).add(AFC(N1.add(N2).subtract(K))).subtract(AFC(N2.subtract(K))).subtract(AFC(N1.add(N2))));
            } else {
                W = exp(CON.add(AFC(N1)).add(AFC(K)).subtract(AFC(K.subtract(N2))).subtract(AFC(N1.add(N2))));
            }

            P = W;
            IX = MINJX;
            U = RAND(prng, precision).multiply(SCALE);

            while (U.compareTo(P) == 1) {
                U = U.subtract(P);
                P = P.multiply(N1.subtract(IX)).multiply(K.subtract(IX));
                IX = IX.add(Ap_ONE);
                P = P.divide(IX).divide(N2.subtract(K).add(IX));
                if (IX.compareTo(MAXJX) == 1) {
                    P = W;
                    IX = MINJX;
                    U = RAND(prng, precision).multiply(SCALE);
                }
            }
        } else {
            /*
         * ...H2PE...
         */
            Apfloat S;
            boolean notFinish = true;
            S = SqrRoot((TN.subtract(K)).multiply(K).multiply(N1).multiply(N2).divide(TN.subtract(Ap_ONE)).divide(TN).divide(TN));

        /*
         * ...REMARK:  D IS DEFINED IN REFERENCE WITHOUT INT.
         * THE TRUNCATION CENTERS THE CELL BOUNDARIES AT 0.5
         */
            Apfloat D = trunc((new Apfloat(1.5, precision)).multiply(S)).add(Ap_ZFIVE);
            XL = trunc(M.subtract(D).add(Ap_ZFIVE));
            XR = trunc(M.add(D).add(Ap_ZFIVE));
            A = AFC(M).add(AFC(N1.subtract(M))).add(AFC(K.subtract(M))).add(AFC(N2.subtract(K).add(M)));
            Apfloat expon = A.subtract(AFC(XL)).subtract(AFC(N1.subtract(XL))).subtract(AFC(K.subtract(XL))).subtract(AFC(N2.subtract(K).add(XL)));

            KL = exp(expon);
            KR = exp(A.subtract(AFC(XR.subtract(Ap_ONE))).subtract(AFC(N1.subtract(XR).add(Ap_ONE))).subtract(AFC(K.subtract(XR).add(Ap_ONE))).subtract(AFC(N2.subtract(K).add(XR).subtract(Ap_ONE))));
            LAMDL = log(XL.multiply(N2.subtract(K).add(XL)).divide(N1.subtract(XL).add(Ap_ONE)).divide(K.subtract(XL).add(Ap_ONE))).negate();
            LAMDR = log((N1.subtract(XR).add(Ap_ONE)).multiply(K.subtract(XR).add(Ap_ONE)).divide(XR).divide(N2.subtract(K).add(XR))).negate();
            P1 = D.multiply(new Apfloat(2, precision));
            P2 = P1.add(KL.divide(LAMDL));
            P3 = P2.add(KR.divide(LAMDR));

            while (notFinish) {
                do {
                    U = RAND(prng, precision).multiply(P3);
                    V = RAND(prng, precision);

                    if (U.compareTo(P1) == -1) {
                         /* ...RECTANGULAR REGION... */
                        IX = XL.add(U);
                    } else if (U.compareTo(P2) <= 0) {
                        /* ...LEFT TAIL... */
                        IX = XL.add(log(V).divide(LAMDL));
                        if (IX.compareTo(MINJX) == -1) {
                            break;
                        }
                        V = V.multiply(U.subtract(P1)).multiply(LAMDL);
                    } else {
                         /* ...RIGHT TAIL... */
                        IX = XR.subtract(log(V).divide(LAMDR));
                        if (IX.compareTo(MAXJX) == 1) {
                            break;
                        }
                        V = V.multiply(U.subtract(P2)).multiply(LAMDR);
                    }

                     /*
                     * ...ACCEPTANCE/REJECTION TEST...
                     */
                    Apfloat F;
                    if ((M.compareTo(new Apfloat(100, precision)) == -1) || (IX.compareTo(new Apfloat(50, precision)) <= 0)) {
                        /* ...EXPLICIT EVALUATION... */
                        F = Ap_ONE;
                        if (M.compareTo(IX) == -1) {
                            Apfloat I = M.add(Ap_ONE);
                            while (I.compareTo(IX) == -1) {
                                F = F.multiply(N1.subtract(I).add(Ap_ONE)).multiply(K.subtract(I).add(Ap_ONE)).divide(N2.subtract(K).add(I)).divide(I);
                                I = I.add(Ap_ONE);
                            }
                        } else if (M.compareTo(IX) == 1) {
                            Apfloat I = IX.add(Ap_ONE);
                            while (I.compareTo(M) == -1) {
                                F = F.multiply(I).multiply(N2.subtract(K).add(I)).divide(N1.subtract(I)).divide(K.subtract(I));
                                I = I.add(Ap_ONE);
                            }
                        }
                        if (V.compareTo(F) <= 0) {
                            REJECT = false;
                        }
                    } else {
                        /* ...SQUEEZE USING UPPER AND LOWER BOUNDS... */
                        Apfloat Y = IX;
                        Apfloat Y1 = Y.add(Ap_ONE);
                        Apfloat YM = Y.subtract(M);
                        Apfloat YN = N1.subtract(Y).add(Ap_ONE);
                        Apfloat YK = K.subtract(Y).add(Ap_ONE);
                        NK = N2.subtract(K).add(Y1);
                        Apfloat R = (YM.negate()).divide(Y1);
                        S = YM.divide(YN);
                        Apfloat T = YM.divide(YK);
                        Apfloat E = (YM.negate()).divide(NK);
                        Apfloat G = (YN.multiply(YK).divide(Y1.multiply(NK))).subtract(Ap_ONE);
                        Apfloat DG = Ap_ONE;
                        if (G.compareTo(Apfloat.ZERO) == -1) {
                            DG = G.add(Ap_ONE);
                        }
                        Apfloat GU = G.multiply(Ap_ONE.add(G.multiply(Ap_ZFIVE.negate().add(G.divide(Ap_THREE)))));
                        Apfloat GL = GU.subtract(Ap_ZTFIVE.multiply(sqr(sqr(G))).divide(DG));
                        Apfloat XM = M.add(Ap_ZFIVE);
                        Apfloat XN = N1.subtract(M).add(Ap_ZFIVE);
                        Apfloat XK = K.subtract(M).add(Ap_ZFIVE);
                        NM = N2.subtract(K).add(XM);
                        Apfloat t1, t2, t3, t4, t5;
                        t1 = (Y.multiply(GU)).subtract(M.multiply(GL)).add(DELTAU);
                        t2 = XM.multiply(R).multiply(Ap_ONE.add(R.multiply(Ap_ZFIVE.negate().add(R.divide(Ap_THREE)))));
                        t3 = XN.multiply(S).multiply(Ap_ONE.add(S.multiply(Ap_ZFIVE.negate().add(S.divide(Ap_THREE)))));
                        t4 = XK.multiply(T).multiply(Ap_ONE.add(T.multiply(Ap_ZFIVE.negate().add(T.divide(Ap_THREE)))));
                        t5 = NM.multiply(E).multiply(Ap_ONE.add(E.multiply(Ap_ZFIVE.negate().add(E.divide(Ap_THREE)))));
                        Apfloat UB = t1.add(t2).add(t3).add(t4).add(t5);

                        /* ...TEST AGAINST UPPER BOUND... */

                        Apfloat ALV = log(V);
                        if (ALV.compareTo(UB) == 1) {
                            REJECT = true;
                        } else {
                            /* ...TEST AGAINST LOWER BOUND... */

                            Apfloat DR = XM.multiply(sqr(sqr(R)));
                            if (R.compareTo(Apfloat.ZERO) == -1) {
                                DR = DR.divide(Ap_ONE.add(R));
                            }
                            Apfloat DS = XN.multiply(sqr(sqr(S)));
                            if (S.compareTo(Apfloat.ZERO) == -1) {
                                DS = DS.divide(Ap_ONE.add(S));
                            }
                            Apfloat DT = XK.multiply(sqr(sqr(T)));
                            if (T.compareTo(Apfloat.ZERO) == -1) {
                                DT = DT.divide(Ap_ONE.add(T));
                            }

                            Apfloat DE = NM.multiply(sqr(sqr(E)));
                            if (E.compareTo(Apfloat.ZERO) == -1) {
                                DE = DE.divide(Ap_ONE.add(E));
                            }
                            Apfloat c1, c2;
                            c1 = Ap_ZTFIVE.multiply(DR.add(DS).add(DT).add(DE));
                            c2 = (Y.add(M)).multiply(GL.subtract(GU));
                            if (ALV.compareTo(UB.subtract(c1).add(c2).subtract(DELTAL)) == -1) {
                                REJECT = false;
                            } else {
                            /* ...STIRLING'S FORMULA TO MACHINE ACCURACY... */
                                Apfloat d1, d2, d3;
                                d1 = AFC(N1.subtract(IX));
                                d2 = AFC(K.subtract(IX));
                                d3 = AFC(N2.subtract(K).add(IX));

                                if (ALV.compareTo(A.subtract(AFC(IX)).subtract(d1).subtract(d2).subtract(d3)) <= 0) {
                                    REJECT = false;
                                } else {
                                    REJECT = true;
                                }
                            }
                        }
                    }


                    notFinish = REJECT;

                } while (REJECT);
            }
        }

        if ((KK.add(KK)).compareTo(to_ZZ(TN)) >= 0) {
            if (NN1.compareTo(NN2) == 1) {
                IX = to_RR(KK.subtract(NN2)).add(IX);
            } else {
                IX = to_RR(NN1).subtract(IX);
            }
        } else {
            if (NN1.compareTo(NN2) == 1) {
                IX = to_RR(KK).subtract(IX);
            }
        }
        JX = IX;
        return to_ZZ(JX);
    }

    public Apfloat AFC(Apfloat I) {
    /*
     * FUNCTION TO EVALUATE LOGARITHM OF THE FACTORIAL I
     * IF (I .GT. 7), USE STIRLING'S APPROXIMATION
     * OTHERWISE,  USE TABLE LOOKUP
     */
        double[] AL = new double[]{0.0, 0.0, 0.6931471806, 1.791759469, 3.178053830, 4.787491743, 6.579251212, 8.525161361};
        int cmp = I.compareTo(new Apfloat(7, precision));
        if (cmp <= 0) {
            return new Apfloat(AL[ApfloatMath.round(I, precision, RoundingMode.HALF_UP).intValue()], precision);
        } else {
            Apfloat LL = log(I);
            return ((((I.add(new Apfloat(5, precision))).multiply(LL)).subtract(I).add(new Apfloat(0.399089934, precision))));
        }
    }

    private Apfloat log(Apfloat in) {
        return ApfloatMath.log(in);
    }

    private Apfloat to_RR(BigInteger in) {
        return new Apfloat(in, precision);
    }

    private Apfloat exp(Apfloat in) {
        return ApfloatMath.exp(in);
    }

    private Apfloat SqrRoot(Apfloat in) {
        return ApfloatMath.sqrt(in);
    }

    private Apfloat sqr(Apfloat in) {
        return ApfloatMath.pow(in, 2);
    }

    private Apfloat trunc(Apfloat in) {
        return new Apfloat(in.truncate().toBigInteger(), precision);
    }

    private BigInteger to_ZZ(Apfloat in) {
        return in.truncate().toBigInteger();
    }

    private Apfloat RAND(IPRNG IPRNG, int precision) {
        BigInteger div = BigInteger.ONE.shiftLeft(precision);
        BigInteger rzz = IPRNG.getRandModHGD(div);
        Apfloat res = new Apfloat(rzz, precision);
        Apfloat divf = new Apfloat(div, precision);
        return res.divide(divf);
    }
}
