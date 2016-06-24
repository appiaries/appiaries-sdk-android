package example.app.model.shopsearch;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABDBObject;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABQuery;

import java.util.List;

@ABCollection("Shops")
public class Shop extends ABDBObject {

    public static class Field extends ABDBObject.Field {
        public static final ABField NAME   = new ABField("name", String.class);
        public static final ABField BRANDS = new ABField("brands", List.class);
    }

    public String getName() { return (String)get(Field.NAME); }
    public void setName(String name) { put(Field.NAME, name); }

    public List<String> getBrands() { return (List<String>)get(Field.BRANDS); }
    public void setBrands(List<String> brands) { put(Field.BRANDS, brands); }

    public static ABQuery<Shop> query() {
        return ABQuery.query(Shop.class);
    }

}
