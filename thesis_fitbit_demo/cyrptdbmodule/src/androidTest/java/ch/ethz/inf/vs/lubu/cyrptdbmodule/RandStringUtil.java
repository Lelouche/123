package ch.ethz.inf.vs.lubu.cyrptdbmodule;

import java.util.Random;

/**
 * Created by lukas on 16.06.15.
 */
public class RandStringUtil {

    private static final String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz !?";

    private static final Random rm = new Random();

    public static String getRandomName(int size) {
        StringBuilder sb = new StringBuilder();
        char[] valids = validChars.toCharArray();
        byte[] buff = new byte[size];
        rm.nextBytes(buff);

        for(int i=0;i<size;i++) {
            int cur = buff[i];
            if(cur<0)
                cur=-cur;
            sb.append(valids[cur % valids.length]);
        }

        return sb.toString();
    }

    public static String getRandomNameMaxLength(int size) {
        int max = rm.nextInt(size);
        return  getRandomName(max);
    }
}
