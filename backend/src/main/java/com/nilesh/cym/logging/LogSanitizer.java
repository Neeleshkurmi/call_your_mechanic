package com.nilesh.cym.logging;

import com.nilesh.cym.token.AuthenticatedUser;

import java.util.Collection;
import java.util.Objects;

public final class LogSanitizer {

    private LogSanitizer() {
    }

    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return "n/a";
        }
        String trimmed = mobile.trim();
        int visible = Math.min(4, trimmed.length());
        String suffix = trimmed.substring(trimmed.length() - visible);
        return "***" + suffix;
    }

    public static String summarizePrincipal(AuthenticatedUser user) {
        if (user == null) {
            return "anonymous";
        }
        return "userId=" + user.userId() + ", role=" + user.role() + ", mobile=" + maskMobile(user.mobile());
    }

    public static String summarizeIds(String label, Collection<?> values) {
        int size = values == null ? 0 : values.size();
        return label + "Count=" + size;
    }

    public static String safe(Object value) {
        return Objects.toString(value, "n/a");
    }
}
