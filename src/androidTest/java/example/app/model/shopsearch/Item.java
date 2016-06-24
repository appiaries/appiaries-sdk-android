package example.app.model.shopsearch;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABDBObject;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABQuery;

import java.util.Date;

@ABCollection("Items")
public class Item extends ABDBObject {

    public static class Field extends ABDBObject.Field {
        public static final ABField TITLE        = new ABField("title", String.class);
        public static final ABField SPEC         = new ABField("spec", String.class);
        public static final ABField BRAND        = new ABField("brand", String.class);
        public static final ABField PRICE        = new ABField("price", double.class);
        public static final ABField ARRIVAL_DATE = new ABField("arrivalDate", Date.class);
        public static final ABField ON_SALE      = new ABField("onSale", boolean.class);
        public static final ABField STOCK        = new ABField("stock", int.class);
    }

    public String getTitle() { return (String)get(Field.TITLE); }
    public void setTitle(String title) { put(Field.TITLE, title); }

    public String getSpec() { return (String)get(Field.SPEC); }
    public void setSpec(String spec) { put(Field.SPEC, spec); }

    public String getBrand() { return (String)get(Field.BRAND); }
    public void setBrand(String brand) { put(Field.BRAND, brand); }

    public double getPrice() { return (double)get(Field.PRICE); }
    public void setPrice(double price) { put(Field.PRICE, price); }

    public Date getArrivalDate() { return (Date)get(Field.ARRIVAL_DATE); }
    public void setArrivalDate(Date arrivalDate) { put(Field.ARRIVAL_DATE, arrivalDate); }

    public boolean isOnSale() { return (boolean)get(Field.ON_SALE); }
    public void setOnSale(boolean onSale) { put(Field.ON_SALE, onSale); }

    public int getStock() { return (int)get(Field.STOCK); }
    public void setStock(int stock) { put(Field.STOCK, stock); }

    public static ABQuery<Item> query() {
        return ABQuery.query(Item.class);
    }

}
