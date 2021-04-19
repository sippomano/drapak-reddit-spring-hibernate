package com.sipp.drapakredditspringhibernate.redditapi.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class Token {

    private final WebClient client;
    private String token;
    private long refreshTime;

    private final String key;
    private final String id;
    private final String useragent;
    private final String grantParamName;
    private final String grantParamValue;
    private final String deviceParamName;
    private final long tokenLifetime;
    private final String tokenElementName;


    @Autowired
    public Token(@Value("${token.retrieval.url}") String url, @Value("${reddit.auth.secretKey}") String key,
                 @Value("${reddit.auth.scriptId}") String id, @Value("${token.retrieval.useragent") String useragent,
                 @Value("${token.retrieval.param.granttype.name}") String grantParamName, @Value("${token.retrieval.param.granttype.value}") String grantParamValue,
                 @Value("${token.retrieval.param.deviceid.name}") String deviceParamName, @Value("${token.retrieval.tokenlifetime}") long tokenLifetime,
                 @Value("${token.parsing.element.token.name}") String tokenElementName) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        this.client = WebClient.builder()
                .uriBuilderFactory(factory)
                .build();
        this.key = key;
        this.id = id;
        this.useragent = useragent;
        this.grantParamName = grantParamName;
        this.grantParamValue = grantParamValue;
        this.deviceParamName = deviceParamName;
        this.tokenLifetime = tokenLifetime;
        this.tokenElementName = tokenElementName;

    }

    public String getTokenHeaderValue() throws IOException {
        long current = new Date().getTime();
        if (refreshTime + tokenLifetime < current) {
            generateAndUpdateToken();
        }
        return "bearer " + token;
    }

    public void generateAndUpdateToken() throws JsonProcessingException {
        String deviceParamValue = UUID.randomUUID().toString();
        String response = client.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam(grantParamName, grantParamValue)
                        .queryParam(deviceParamName, deviceParamValue)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, encodeRequest(id, key))
                .header(HttpHeaders.USER_AGENT, useragent)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        this.token = extractTokenFromJson(response);
        this.refreshTime= new Date().getTime();
    }

    private String encodeRequest(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }

    private String extractTokenFromJson(String jsonBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonBody);
        return root.get(tokenElementName).asText();
    }
}
