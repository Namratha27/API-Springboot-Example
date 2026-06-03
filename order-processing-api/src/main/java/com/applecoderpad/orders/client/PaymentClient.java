package com.applecoderpad.orders.client;

import com.applecoderpad.orders.dto.PaymentAuthorization;
import com.applecoderpad.orders.dto.PaymentRequest;
import com.applecoderpad.orders.model.CustomerOrder;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentClient {
  private final RestClient restClient;
  private final boolean dryRun;
  private final String paymentUrl;

  public PaymentClient(
      RestClient restClient,
      @Value("${orders.dry-run}") boolean dryRun,
      @Value("${orders.payment-url}") String paymentUrl) {
    this.restClient = restClient;
    this.dryRun = dryRun;
    this.paymentUrl = paymentUrl;
  }

  public PaymentAuthorization authorize(CustomerOrder order) {
    PaymentRequest request =
        new PaymentRequest(order.id(), order.customerId(), order.totalAmount());
    if (dryRun) return new PaymentAuthorization("auth-" + order.id());
    return restClient
        .post()
        .uri(URI.create(paymentUrl))
        .body(request)
        .retrieve()
        .body(PaymentAuthorization.class);
  }

  public void voidAuthorization(String authorizationId) {
    if (dryRun) return;
    restClient
        .post()
        .uri(URI.create(paymentUrl + "/" + authorizationId + "/void"))
        .retrieve()
        .toBodilessEntity();
  }
}
