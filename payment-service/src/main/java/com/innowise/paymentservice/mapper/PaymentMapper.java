package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.document.Payment;
import com.innowise.paymentservice.model.dto.PaymentRequest;
import com.innowise.paymentservice.model.dto.PaymentResponse;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment paymentRequestToPayment(PaymentRequest paymentRequest);

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    PaymentResponse paymentToPaymentResponse(Payment payment);

    List<PaymentResponse> paymentsToPaymentsResponse(List<Payment> payments);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

}
