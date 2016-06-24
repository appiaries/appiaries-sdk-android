package example.app.model;

import android.content.Intent;
import android.os.Bundle;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABPushMessage;
import com.appiaries.baas.sdk.ABQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ABCollection
public class ExtendedPushMessage extends ABPushMessage {

    public ExtendedPushMessage() { super(); }
    public ExtendedPushMessage(String collectionID) { super(collectionID); }
    public ExtendedPushMessage(String collectionID, Map<String, Object> map) { super(collectionID, map); }
    public ExtendedPushMessage(Map<String, Object> map) { super(map); }
    public ExtendedPushMessage(Intent intent) { super(intent); }

    public static ABQuery<ExtendedPushMessage> query() {
        return ABQuery.query(ExtendedPushMessage.class);
    }

}
