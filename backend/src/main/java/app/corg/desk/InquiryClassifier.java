package app.corg.desk;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;
@Service
public class InquiryClassifier {
  private final String key,model;
  private final ObjectMapper mapper = new ObjectMapper();
  private final RestClient client = RestClient.create("https://api.openai.com");
  public record Result(String category,String summary) {}
  public InquiryClassifier(@Value("${app.openai-key:}") String key,@Value("${app.openai-model:gpt-4.1-nano}") String model){
    this.key=key;this.model=model;
  }
  public Result classify(String message){
    if(key==null||key.isBlank())return new Result("unclassified","");
    try{
      var req=Map.of("model",model,"messages",List.of(
        Map.of("role","system","content","Classify a Korean business inquiry as quote, support, partnership or other. Reply category|short Korean summary, without personal details."),
        Map.of("role","user","content",message.substring(0,Math.min(3000,message.length())))
      ),"max_tokens",100);
      var raw=client.post().uri("/v1/chat/completions").contentType(MediaType.APPLICATION_JSON)
        .header("Authorization","Bearer "+key).body(req).retrieve().body(String.class);
      var node=mapper.readTree(raw);
      var value=node.path("choices").path(0).path("message").path("content").asText("");
      var parts=value.split("\\|",2);
      var category=parts[0].trim().toLowerCase(Locale.ROOT);
      if(!Set.of("quote","support","partnership","other").contains(category))category="other";
      var summary=parts.length>1?parts[1].trim():"";
      return new Result(category,summary.substring(0,Math.min(500,summary.length())));
    }catch(Exception ignored){return new Result("unclassified","");}
  }
}
