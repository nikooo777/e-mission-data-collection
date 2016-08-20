package ch.supsi.dti.e_missionconsumes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import ch.supsi.dti.e_missionconsumes.FuelType;

/**
 * Created by Niko on 8/12/2016.
 */
public class Tools {
    public static String shortMd5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }

            return sb.substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String[] FuelNames() {
        java.util.LinkedList<String> list = new LinkedList<>();
        for (FuelType s : FuelType.values()) {
            list.add(s.name());
        }
        return list.toArray(new String[list.size()]);
    }
}

