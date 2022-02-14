package io.github.engineeringov.website.api.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static io.github.engineeringov.website.api.common.utils.StringUtils.cleanWhiteSpaces;

@Service
@Log4j2
public class RestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public <E, C> C restCallToURL(String url, HttpMethod httpMethod, HttpEntity<E> entity, Class<C> clazz, boolean cleanWhitespaces) {
        try {
            String response = restTemplate.exchange(url, httpMethod, entity, String.class).getBody();
            if (cleanWhitespaces) {
                response = cleanWhiteSpaces(response);
            }
            return objectMapper.readValue(response, clazz);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode());
        } catch (JsonProcessingException e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

}
