package app.corg.desk;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InquiryControllerTest {
  private static final UUID TENANT = UUID.fromString("12345678-1234-1234-1234-123456789012");

  @Test void createAlwaysPersistsConfiguredTenant() {
    JdbcTemplate db = mock(JdbcTemplate.class);
    InquiryClassifier classifier = mock(InquiryClassifier.class);
    when(classifier.classify("Can I get a quote?")).thenReturn(new InquiryClassifier.Result("quote", "Quote inquiry"));
    var controller = new InquiryController(db, classifier, TENANT);
    var result = controller.create(new InquiryController.InquiryInput("Test", "customer@example.com", "Example", "Can I get a quote?"));
    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    verify(db).update(startsWith("INSERT INTO inquiries(id,tenant_id"), any(UUID.class), eq(TENANT),
      eq("Test"), eq("customer@example.com"), eq("Example"), eq("Can I get a quote?"), eq("quote"), eq("Quote inquiry"));
  }

  @Test void statusUpdateAlwaysFiltersByTenantAndInquiryId() {
    JdbcTemplate db = mock(JdbcTemplate.class);
    InquiryClassifier classifier = mock(InquiryClassifier.class);
    var controller = new InquiryController(db, classifier, TENANT);
    UUID id = UUID.randomUUID();
    when(db.update(anyString(), eq("done"), eq(TENANT), eq(id))).thenReturn(1);
    assertEquals(HttpStatus.NO_CONTENT, controller.update(id, new InquiryController.StatusInput("done")).getStatusCode());
    verify(db).update(contains("WHERE tenant_id=? AND id=?"), eq("done"), eq(TENANT), eq(id));
  }

  @Test void invalidStatusIsRejected() {
    var controller = new InquiryController(mock(JdbcTemplate.class), mock(InquiryClassifier.class), TENANT);
    assertThrows(org.springframework.web.server.ResponseStatusException.class,
      () -> controller.update(UUID.randomUUID(), new InquiryController.StatusInput("deleted")));
  }
}
