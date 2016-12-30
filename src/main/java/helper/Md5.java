package helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

    public static final int KEY_SIZE = 32;

    public static final String md5(InputStream inputStream){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        md.reset();
        byte[] bytes = new byte[1024];
        int numBytes;
        try {
            while ((numBytes = bufferedInputStream.read(bytes)) != -1)
                md.update(bytes, 0, numBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] hash = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : hash) {
            sb.append(String.format("%02x", b & 0xff));
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }
}
