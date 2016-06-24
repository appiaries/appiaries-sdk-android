package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABFile;
import com.appiaries.baas.sdk.ABQuery;

import java.util.HashMap;
import java.util.Map;

@ABCollection("ExtendedFile")
public class ExtendedFile extends ABFile {

    public ExtendedFile() {
        super();
    }
    public ExtendedFile(String collectionID) {
        super(collectionID);
    }
    public ExtendedFile(String collectionID, Map<String, Object> map) {
        super(collectionID, map);
    }
    public ExtendedFile(Map<String, Object> map) { super(map); }

    public static ABQuery<ExtendedFile> query() {
        return ABQuery.query(ExtendedFile.class);
    }

}
