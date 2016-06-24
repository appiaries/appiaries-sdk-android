package example.app.model;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABDevice;
import com.appiaries.baas.sdk.ABQuery;

import java.util.HashMap;
import java.util.Map;

@ABCollection
public class ExtendedDevice extends ABDevice {

    public ExtendedDevice() { super(); }
    public ExtendedDevice(String collectionID) { super(collectionID); }
    public ExtendedDevice(String collectionID, Map<String, Object> map) { super(collectionID, map); }
    public ExtendedDevice(Map<String, Object> map) { super(map); }

    public static ABQuery<ExtendedDevice> query() {
        return ABQuery.query(ExtendedDevice.class);
    }

}
