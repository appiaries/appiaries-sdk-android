package com.appiaries.baas.sdk;

import java.util.Map;

/**
 * 認証プロバイダ・インタフェース
 *
 * @author Appiaries Corporation
 * @since 1.3.0
 */
abstract interface ABAuthenticationProvider {

    public abstract void authenticate(AuthenticationCallback callback);

    public abstract void deauthenticate();

    public abstract boolean restoreAuthentication(Map<String, Object> authData);

    public abstract void cancel();

    public abstract void logOut();

    public abstract String getId();

    /**
     * 認証コールバックハンドラ・インタフェース
     */
    public static abstract interface AuthenticationCallback {
        public abstract void onSuccess(Map<String, Object> authData);

        public abstract void onError(Throwable throwable);

        public abstract void onCancel();
    }

    /**
     * 認証プロバイダ・コールバックハンドラ・インタフェース
     */
    public abstract interface ProviderCallback {
        public abstract void onSuccess(Object object);

        public abstract void onFailure(Throwable throwable);

        public abstract void onCancel();
    }
}
