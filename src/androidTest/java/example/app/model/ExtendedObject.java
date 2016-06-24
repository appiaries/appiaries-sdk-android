package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABModel;
import com.appiaries.baas.sdk.ABQuery;

@ABCollection("ExtendedObject")
public class ExtendedObject extends ABModel {

    public static ABQuery<ExtendedObject> query() {
        return ABQuery.query(ExtendedObject.class);
    }

}
