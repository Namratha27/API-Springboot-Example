package com.applecoderpad.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.urlshortener.dto.CreateLinkRequest;
import com.applecoderpad.urlshortener.dto.LinkResponse;
import com.applecoderpad.urlshortener.service.LinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UrlShortenerApiApplicationTests {
  @Autowired private LinkService links;

  @Test
  void createsAndResolvesShortLink() {
    LinkResponse created =
        links.create(new CreateLinkRequest("https://developer.apple.com", "appledocs", null));
    LinkResponse resolved = links.resolve(created.code());
    assertThat(resolved.originalUrl()).isEqualTo("https://developer.apple.com");
    assertThat(resolved.clicks()).isEqualTo(1);
  }
}
