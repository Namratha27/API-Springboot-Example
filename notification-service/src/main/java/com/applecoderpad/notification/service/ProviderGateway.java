package com.applecoderpad.notification.service;

import com.applecoderpad.notification.dto.ProviderRequest;
import com.applecoderpad.notification.dto.ProviderResponse;
import com.applecoderpad.notification.model.Channel;
import com.applecoderpad.notification.model.NotificationRecord;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ProviderGateway {
  private final RestClient restClient;
  private final boolean dryRun;
  private final Map<Channel, String> providerUrls;

  public ProviderGateway(
      RestClient restClient,
      @Value("${notifications.dry-run}") boolean dryRun,
      @Value("${notifications.email-url}") String emailUrl,
      @Value("${notifications.sms-url}") String smsUrl,
      @Value("${notifications.push-url}") String pushUrl) {
    this.restClient = restClient;
    this.dryRun = dryRun;
    this.providerUrls = Map.of(Channel.EMAIL, emailUrl, Channel.SMS, smsUrl, Channel.PUSH, pushUrl);
  }

  public ProviderResponse deliver(NotificationRecord notification, Channel channel) {
    ProviderRequest request =
        new ProviderRequest(
            notification.id(),
            notification.recipient(),
            notification.subject(),
            notification.body());
    if (dryRun)
      return new ProviderResponse(
          "dry-run-" + channel.name().toLowerCase() + "-" + notification.id());
    return restClient
        .post()
        .uri(URI.create(providerUrls.get(channel)))
        .body(request)
        .retrieve()
        .body(ProviderResponse.class);
  }
}
