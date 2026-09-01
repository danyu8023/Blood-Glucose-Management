package com.tangan.glucose.common;

import java.util.UUID;

/** ThreadLocal 只保存已认证用户 ID，避免 Controller 接触 JWT 解析细节。 */
public final class AuthContext {
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();
    private AuthContext() { }
    public static void set(UUID userId) { CURRENT_USER.set(userId); }
    public static UUID requireUserId() {
        UUID userId = CURRENT_USER.get();
        if (userId == null) throw ApiException.unauthorized("请先登录");
        return userId;
    }
    public static void clear() { CURRENT_USER.remove(); }
}
