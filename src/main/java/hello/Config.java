package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import hello.Iris;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.JSONObject;
import com.mongodb.client.MongoCursor;


@EnableScheduling
@Configuration
public class Config{

		@Autowired
		SimpMessagingTemplate template;
		
		@Autowired
		RestTemplate restTemplate;
		
		
		@Scheduled(fixedDelay = 3000)
		public void envio() throws JsonProcessingException{			
		       
	        MongoClientURI uri  = new MongoClientURI("mongodb://admin:admin1@ds117423.mlab.com:17423/iris_proj"); 
	        MongoClient client = new MongoClient(uri);
	        MongoDatabase db = client.getDatabase(uri.getDatabase());
	     
	        
	        MongoCollection<Document> param = db.getCollection("parametros");

	        Document orderBy = new Document("id", -1);

	        MongoCursor<Document> cursor = param.find().sort(orderBy).iterator();
	
	            Document doc = cursor.next();
	            double sl = (double) doc.get("sl");
	            double sw = (double) doc.get("sw");
	            double pl = (double) doc.get("pl");
	            double pw = (double) doc.get("pw");	            
	            
	            Iris data = new Iris(sl, sw, pl, pw);
	            data.setSl(sl);
	            data.setSw(sw);
	            data.setPl(pl);
	            data.setPw(pw);
	            
	            ObjectMapper objectMapper = new ObjectMapper();
            	String jsonString = objectMapper.writeValueAsString(data);
            	JSONObject jsonObj = new JSONObject(jsonString);
            	
            	System.out.println(sl);
 	            System.out.println(sw);
 	            System.out.println(pl);
 	            System.out.println(pw);
	            System.out.println(jsonObj);
	            System.out.println("------------------------------------------");
	            
	            String url_connect = "https://irispost.herokuapp.com/";
	            
	            try {

	            	
	            	DefaultHttpClient cliente = new DefaultHttpClient();
	        		HttpPost post = new HttpPost(url_connect);
	        		post.setHeader("Accept", "application/json");
	        		post.setHeader("headerValue", "HeaderInformation");
	        		//setting json object to post request.
	        		StringEntity entity = new StringEntity(jsonObj.toString(), "UTF8");
	        		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        		post.setEntity(entity);
	        		

	        		//this is your response:
     		
	        		HttpResponse response = cliente.execute(post);
	        		String json = EntityUtils.toString(response.getEntity());       
	                System.out.println(json);

	        		
	        		template.convertAndSend("/topic/greetings", new Greeting(json)); 


				} catch (Exception e) {
					
					System.out.println(e);
					
				}

		}

}
