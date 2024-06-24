package org.jbpm.persistence.integration.elasticsearch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class ElasticSearchEventEmitter implements EventEmitter {
    
    private static String dateFormatStr = System.getProperty("org.kie.server.json.date_format", "yyyy-MM-dd'T'hh:mm:ss.SSSZ");
    private static String elasticSearchUrl = System.getProperty("org.jbpm.integration.elasticsearch.url", "http://localhost:9200");
    private static String elasticSearchIndex = System.getProperty("org.jbpm.integration.elasticsearch.index", "jbpm");
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchEventEmitter.class);

    private ObjectMapper mapper = new ObjectMapper();
    
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    @SuppressWarnings("deprecation")// need to use mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true); once KIE Server uses 2.6.2 or higher of jackson
    public ElasticSearchEventEmitter() {
        mapper.setDateFormat(new SimpleDateFormat(dateFormatStr));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);       
        mapper.setVisibilityChecker(
                mapper.getSerializationConfig().
                getDefaultVisibilityChecker().
                withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                withGetterVisibility(JsonAutoDetect.Visibility.NONE));
    }
    
    public void deliver(Collection<InstanceView<?>> data) {
        // no-op

    }

    public void apply(Collection<InstanceView<?>> data) {
        if (data.isEmpty()) {
            return;
        }
        
        executor.execute(() -> {    
            StringBuilder content = new StringBuilder();
            
            for (InstanceView<?> view : data) {
                try {                
                    String json = mapper.writeValueAsString(view);
                    
                    String type = "unknown";
                    String id = "";
                    if (view instanceof ProcessInstanceView) {
                        type = "processes";
                        id = ((ProcessInstanceView) view).getCompositeId();
                    } else if (view instanceof TaskInstanceView) {
                        type = "tasks";
                        id = ((TaskInstanceView) view).getCompositeId();
                    }
                    
                    content.append("{ \"index\" : { \"_index\" : \"" + elasticSearchIndex +"\", \"_type\" : \"" + type +"\", \"_id\" : \"" + id + "\" } }\n");
                    content.append(json);
                    content.append("\n");
                                        
                } catch (JsonProcessingException e) {
                    logger.error("Error while serializing {} to JSON", view, e);
                }
            }
            
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPut httpPut = new HttpPut(elasticSearchUrl + "/_bulk");
                httpPut.setEntity(new StringEntity(content.toString()));

                logger.debug("Executing request " + httpPut.getRequestLine());
                httpPut.setHeader("Content-Type", "application/x-ndjson");

                // Create a custom response handler
                ResponseHandler<String> responseHandler = response -> {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                };
                String responseBody = httpclient.execute(httpPut, responseHandler);
                logger.info(responseBody);
            } catch (IOException e) {                    
                e.printStackTrace();
            }
        });
    }

    public void drop(Collection<InstanceView<?>> data) {
        // no-op

    }

    public EventCollection newCollection() {
        return new BaseEventCollection();
    }

    @Override
    public void close() {
        executor.shutdown();
        logger.info("Elasticsearch event emitter closed successfully");
    }

}
