package example.app.model.shopsearch;

import com.appiaries.baas.sdk.ABCollection;
import com.appiaries.baas.sdk.ABQuery;
import com.appiaries.baas.sdk.ABSequence;

@ABCollection("CouponCounterSeq")
public class CouponCounter extends ABSequence {

    public static ABQuery<CouponCounter> query() {
        return ABQuery.query(CouponCounter.class);
    }

}
