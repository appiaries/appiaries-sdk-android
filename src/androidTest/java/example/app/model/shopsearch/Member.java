package example.app.model.shopsearch;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABDBObject;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABQuery;
import com.appiaries.baas.sdk.ABUser;

import java.util.List;

@ABCollection
public class Member extends ABUser {

    public static class Field extends ABDBObject.Field {
        public static final ABField FAVORITE_BRANDS = new ABField("favoriteBrands", List.class);
    }

    public List<String> getFavoriteBrands() { return (List<String>)get(Field.FAVORITE_BRANDS); }
    public void setFavoriteBrands(List<String> brands) { put(Field.FAVORITE_BRANDS, brands); }

    public static ABQuery<Member> query() {
        return ABQuery.query(Member.class);
    }

}
