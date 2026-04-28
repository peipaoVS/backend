package com.mmlm.useradmin.common;

public final class AuthContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<Long>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<String>();

    private AuthContext() {
    }

    public static void set(Long userId, String username) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
    }
}
