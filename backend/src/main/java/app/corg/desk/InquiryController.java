package app.corg.desk;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api")
public class InquiryController {
  private final JdbcTemplate db; private final InquiryClassifier classifier; private final UUID tenantId;
  public InquiryController(JdbcTemplate db, InquiryClassifier classifier, @Value("${app.tenant-id}") UUID tenantId) {
    this.db=db;this.classifier=classifier;this.tenantId=tenantId;
  }
  public record InquiryInput(@NotBlank @Size(max=100) String name,@NotBlank @Email @Size(max=200) String email,
    @Size(max=150) String company,@NotBlank @Size(max=4000) String message){}
  public record StatusInput(@NotBlank String status){}
  @GetMapping("/health") public Map<String,Boolean> health(){return Map.of("ok",true);}
  @PostMapping("/inquiries") public ResponseEntity<?> create(@Valid @RequestBody InquiryInput input){
    UUID id=UUID.randomUUID(); var c=classifier.classify(input.message());
    db.update("INSERT INTO inquiries(id,tenant_id,name,email,company,message,category,summary) VALUES (?,?,?,?,?,?,?,?)",
      id,tenantId,input.name().trim(),input.email().trim(),input.company()==null?null:input.company().trim(),input.message().trim(),c.category(),c.summary());
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",id,"received",true));
  }
  @GetMapping("/admin/inquiries") public List<Map<String,Object>> list(@RequestParam(required=false) String status){
    if(status!=null&&!Set.of("new","in_progress","done").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    var sql="SELECT id,name,email,company,message,category,summary,status,created_at FROM inquiries WHERE tenant_id=?"
      +(status==null?"":" AND status=?")+" ORDER BY created_at DESC LIMIT 200";
    org.springframework.jdbc.core.RowMapper<Map<String,Object>> mapper=(rs,n)->{
      Map<String,Object> row=new LinkedHashMap<>();
      for(var col:List.of("id","name","email","company","message","category","summary","status","created_at"))row.put(col,rs.getObject(col));
      return row;
    };
    return status==null?db.query(sql,mapper,tenantId):db.query(sql,mapper,tenantId,status);
  }
  @PatchMapping("/admin/inquiries/{id}") public ResponseEntity<?> update(
    @PathVariable UUID id,@Valid @RequestBody StatusInput input){
    if(!Set.of("new","in_progress","done").contains(input.status()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    return db.update("UPDATE inquiries SET status=?,updated_at=NOW() WHERE tenant_id=? AND id=?",input.status(),tenantId,id)==0
      ?ResponseEntity.notFound().build():ResponseEntity.noContent().build();
  }
}
