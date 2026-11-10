package app.corg.desk;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@RestController @RequestMapping("/api")
public class InquiryController {
  private final JdbcTemplate db; private final InquiryClassifier classifier; private final byte[] adminKey;
  public InquiryController(JdbcTemplate db,InquiryClassifier classifier,@Value("${app.admin-key}") String key){
    if(key.length()<24)throw new IllegalStateException("ADMIN_API_KEY must be at least 24 characters");
    this.db=db;this.classifier=classifier;this.adminKey=key.getBytes(StandardCharsets.UTF_8);
  }
  public record InquiryInput(@NotBlank @Size(max=100) String name,@NotBlank @Email @Size(max=200) String email,
    @Size(max=150) String company,@NotBlank @Size(max=4000) String message){}
  public record StatusInput(@NotBlank String status){}
  private void auth(String key){
    if(!MessageDigest.isEqual(adminKey,(key==null?"":key).getBytes(StandardCharsets.UTF_8)))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid admin key");
  }
  @GetMapping("/health") public Map<String,Boolean> health(){return Map.of("ok",true);}
  @PostMapping("/inquiries") public ResponseEntity<?> create(@Valid @RequestBody InquiryInput input){
    UUID id=UUID.randomUUID(); var c=classifier.classify(input.message());
    db.update("INSERT INTO inquiries(id,name,email,company,message,category,summary) VALUES (?,?,?,?,?,?,?)",
      id,input.name().trim(),input.email().trim(),input.company()==null?null:input.company().trim(),input.message().trim(),c.category(),c.summary());
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",id,"received",true));
  }
  @GetMapping("/admin/inquiries") public List<Map<String,Object>> list(
    @RequestHeader(value="X-Admin-Key",required=false) String key,@RequestParam(required=false) String status){
    auth(key);
    if(status!=null&&!Set.of("new","in_progress","done").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    var sql="SELECT id,name,email,company,message,category,summary,status,created_at FROM inquiries"
      +(status==null?"":" WHERE status=?")+" ORDER BY created_at DESC LIMIT 200";
    org.springframework.jdbc.core.RowMapper<Map<String,Object>> mapper=(rs,n)->{
      Map<String,Object> row=new LinkedHashMap<>();
      for(var col:List.of("id","name","email","company","message","category","summary","status","created_at"))row.put(col,rs.getObject(col));
      return row;
    };
    return status==null?db.query(sql,mapper):db.query(sql,mapper,status);
  }
  @PatchMapping("/admin/inquiries/{id}") public ResponseEntity<?> update(
    @RequestHeader(value="X-Admin-Key",required=false) String key,@PathVariable UUID id,@Valid @RequestBody StatusInput input){
    auth(key);
    if(!Set.of("new","in_progress","done").contains(input.status()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    return db.update("UPDATE inquiries SET status=?,updated_at=NOW() WHERE id=?",input.status(),id)==0
      ?ResponseEntity.notFound().build():ResponseEntity.noContent().build();
  }
}
