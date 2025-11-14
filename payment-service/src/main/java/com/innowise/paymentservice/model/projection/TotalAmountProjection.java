package com.innowise.paymentservice.model.projection;

import org.bson.types.Decimal128;

public interface TotalAmountProjection {

    Decimal128 getTotal();

}
