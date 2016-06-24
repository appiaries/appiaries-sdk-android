package example.app.model.shopsearch;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABField;
import com.appiaries.baas.sdk.ABFile;
import com.appiaries.baas.sdk.ABQuery;

@ABCollection("ItemImages")
public class ItemImage extends ABFile {

    public static class Field extends ABFile.Field {
        public static final ABField SIZE = new ABField("size", float[].class);
    }

    public float[] getSize() { return (float[])get(Field.SIZE); }
    public void setSize(float[] size) { put(Field.SIZE, size); }

    public static ABQuery<ItemImage> query() {
        return ABQuery.query(ItemImage.class);
    }

}
