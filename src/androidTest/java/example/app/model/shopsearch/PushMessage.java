package example.app.model.shopsearch;

import com.appiaries.baas.sdk.ABPushMessage;
import com.appiaries.baas.sdk.ABQuery;

public class PushMessage extends ABPushMessage {

    public static ABQuery<PushMessage> query() {
        return ABQuery.query(PushMessage.class);
    }

}
