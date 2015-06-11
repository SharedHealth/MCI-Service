package org.sharedhealth.mci.web.infrastructure.security;

public class UserAuthInfo {
    private String clientId;
    private String email;

    public UserAuthInfo(String clientId, String email) {
        this.clientId = clientId;
        this.email = email;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAuthInfo)) return false;

        UserAuthInfo that = (UserAuthInfo) o;

        if (!clientId.equals(that.clientId)) return false;
        if (!email.equals(that.email)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }
}
