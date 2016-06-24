package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABDBObject;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABQuery;

import java.util.HashMap;
import java.util.Map;

@ABCollection("ExtendedDBObject")
public class ExtendedDBObject extends ABDBObject {

    public static class Field extends ABDBObject.Field {
        public static final ABField NICKNAME = new ABField("nickname", String.class);
        public static final ABField GENDER = new ABField("gender", String.class);
        public static final ABField AGE = new ABField("age", int.class);
    }

    public String getNickname() {
        return (String)get(Field.NICKNAME);
    }
    public void setNickname(String nickname) {
        put(Field.NICKNAME, nickname);
    }

    public String getGender() {
        return (String)get(Field.GENDER);
    }
    public void setGender(String gender) {
        put(Field.GENDER, gender);
    }

    public int getAge() {
        return (int)get(Field.AGE);
    }
    public void setAge(int age) {
        put(Field.AGE, age);
    }

    public ExtendedDBObject() {
    super();
}
    public ExtendedDBObject(String collectionID) {
        super(collectionID);
    }
    public ExtendedDBObject(String collectionID, Map<String, Object> map) { super(collectionID, map); }
    public ExtendedDBObject(Map<String, Object> map) { super(map); }

    public static ABQuery<ExtendedDBObject> query() {
        return ABQuery.query(ExtendedDBObject.class);
    }

}
