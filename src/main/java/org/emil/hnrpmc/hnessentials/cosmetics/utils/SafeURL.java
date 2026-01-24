package org.emil.hnrpmc.hnessentials.cosmetics.utils;


import java.util.Objects;

/**
 * Safe URL thing for keeping tokens safe.
 */
public final class SafeURL {
    private final String url;
    private final String safeUrl;

    public SafeURL(String url, String safeUrl) {
        this.url = url;
        this.safeUrl = safeUrl;
    }

    @Override
    public String toString() {
        return "SafeURL{" + this.safeUrl + "}";
    }

    public String url() {
        return url;
    }

    public String safeUrl() {
        return safeUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SafeURL that = (SafeURL) obj;
        return Objects.equals(this.url, that.url) &&
                Objects.equals(this.safeUrl, that.safeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, safeUrl);
    }

    public static SafeURL of(String baseUrl, String token) {
        return new SafeURL(baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=" + token, baseUrl);
    }

    public static SafeURL of(String baseUrl) {
        return new SafeURL(baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=", baseUrl);
    }

    public static SafeURL direct(String safeRequest) {
        return new SafeURL(safeRequest, safeRequest);
    }
}
