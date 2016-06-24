package com.appiaries.baas.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 匿名認証プロバイダ。
 *
 * @author Appiaries Corporation
 * @since 1.4.0
 */
class ABAnonymousAuthenticationProvider implements ABAuthenticationProvider {

    public static String ID = "anonymous";

    /**
     * コンストラクタ。
     * <p></p>
     */
    public ABAnonymousAuthenticationProvider() {
        super();
    }

    @Override
    public void authenticate(AuthenticationCallback pCallback) {
        pCallback.onSuccess(getAuthData());
    }

    @Override
    public void deauthenticate() { /** unused */ }

    public boolean restoreAuthentication(Map<String, Object> authData) { return true; }

    @Override
    public void cancel() { /** unused */ }

    @Override
    public void logOut() { /** unused */ }

    @Override
    public String getId() { return ABAnonymousAuthenticationProvider.ID; }

    /**
     * JSON形式の匿名認証情報を返す。
     * <p></p>
     *
     * @return JSON形式の匿名情報
     */
    public Map<String, Object> getAuthData() {
        //匿名認証用UUID(=User.ID)の生成
        //NOTE: 匿名認証用のUUIDは1つの端末で同じUUIDを使い回す仕様(ログアウト後に再度匿名ログインした場合に前回生成したUUIDを使用する)のため、
        //      1度生成したUUIDはSharedPreferencesへ保存し、必要に応じてロードして再利用する。
        //      ATTENTION: ただし、アプリをアンインストールした場合はSharedPreferencesへ保存したデータも消えるため、その際は別のUUIDが生成されることになる。
        String uuid = AB.Preference.load(AB.Preference.PREF_KEY_ANONYMOUS_UUID);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString().toUpperCase();
            AB.Preference.save(AB.Preference.PREF_KEY_ANONYMOUS_UUID, uuid);
        }
        Map<String, Object> authData = new HashMap<>();
        authData.put("id", uuid);
        return authData;
    }

}
