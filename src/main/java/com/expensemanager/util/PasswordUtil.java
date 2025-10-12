package com.expensemanager.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) return false;
        return BCrypt.checkpw(plain, hash);
    }
}
