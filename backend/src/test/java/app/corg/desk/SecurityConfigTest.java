package app.corg.desk;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
  private final SecurityConfig config = new SecurityConfig();
  @Test void bcryptHashIsUsedForAdminPassword(){
    PasswordEncoder encoder=config.passwordEncoder();
    var user=config.users("admin","a-long-test-password-123",encoder).loadUserByUsername("admin");
    assertNotEquals("a-long-test-password-123",user.getPassword());
    assertTrue(encoder.matches("a-long-test-password-123",user.getPassword()));
  }
  @Test void weakAdminPasswordFailsFast(){
    assertThrows(IllegalStateException.class,()->config.users("admin","short",config.passwordEncoder()));
  }
}
