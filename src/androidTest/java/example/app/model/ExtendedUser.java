package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABQuery;
import com.appiaries.baas.sdk.ABUser;

import java.util.HashMap;
import java.util.Map;

@ABCollection
public class ExtendedUser extends ABUser {

    static class Field extends ABUser.Field {
        static ABField NICKNAME;
        static ABField GENDER;
        static ABField AGE;
    }

//    // getter/setter の自動生成
//    @ABObjectProperty(key="nickname")
//    private String mNickname;
//
//    @ABObjectProperty(key="birth", inputKey="in_birth", outputKey="out_birth")
//    private Date mBirth;

    // カスタム getter
    public String getNickname() {
        //return getString("nickname");
        return (String)get(Field.NICKNAME);
    }

    // カスタム setter
    public void setNickname(String nickname) {
        //put("nickname", nickname);
        put(Field.GENDER, nickname);
    }

    public ExtendedUser() {
        super();
    }
    public ExtendedUser(String collectionID) {
        super(collectionID);
    }
    public ExtendedUser(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }
    public ExtendedUser(Map<String, Object> map) { super(map); }

    public static ABQuery<ExtendedUser> query() {
        return ABQuery.query(ExtendedUser.class);
    }

}
