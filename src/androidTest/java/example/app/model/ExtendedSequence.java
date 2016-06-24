package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABQuery;
import com.appiaries.baas.sdk.ABSequence;

import java.util.HashMap;
import java.util.Map;

@ABCollection("ExtendedSequence")
public class ExtendedSequence extends ABSequence {

    protected ExtendedSequence() {
        super();
    }
    public ExtendedSequence(String collectionID) {
        super(collectionID);
    }
    public ExtendedSequence(String collectionID, Map<String, Object> map) { super(collectionID, map); }
    public ExtendedSequence(Map<String, Object> map) { super(map); }

    public static ABQuery<ExtendedSequence> query() {
        return ABQuery.query(ExtendedSequence.class);
    }

}
