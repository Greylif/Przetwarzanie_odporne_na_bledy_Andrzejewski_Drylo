package com.example.lab_02.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestLoggingFilter implements Filter {

  private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

    File file = new File("Log_lab2.txt");

    String message = "REQUEST\n" + "URL: " + wrappedRequest.getRequestURL() + "\nMethod: " + wrappedRequest.getMethod() + "\nQuery: " + wrappedRequest.getQueryString() + "\n";

    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(message);
      LOGGER.info("Plik zostal zapisany pomyslnie: " + file.getAbsolutePath());
      LOGGER.info("Zapisana wiadomosc: " + message);
    } catch (IOException e) {
      LOGGER.severe("Blad podczas zapisu pliku: " + e.getMessage());
      return;
    }

    chain.doFilter(wrappedRequest, wrappedResponse);

    String responseBody = new String(wrappedResponse.getContentAsByteArray(), response.getCharacterEncoding());
    StringBuilder simplifiedLog = new StringBuilder();
    boolean simplifiedLogged = false;

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(responseBody);

      if (root.isArray()) {
        for (JsonNode node : root) {
          if (node.has("id") && node.has("title") && node.has("body")) {
            long id = node.get("id").asLong();
            String title = node.get("title").asText();
            String bodyText = node.get("body").asText();
            simplifiedLog.append(id)
                .append(" ")
                .append(title)
                .append("\n")
                .append(bodyText)
                .append("\n\n");
            simplifiedLogged = true;
          } else {
            simplifiedLogged = false;
            break;
          }
        }
      } else if (root.isObject() && root.has("id") && root.has("title") && root.has("body")) {
        long id = root.get("id").asLong();
        String title = root.get("title").asText();
        String bodyText = root.get("body").asText();
        simplifiedLog.append(id)
            .append(" ")
            .append(title)
            .append("\n")
            .append(bodyText)
            .append("\n");
        simplifiedLogged = true;
      }

      if (!simplifiedLogged) {
        simplifiedLog.append(responseBody);
      }

    } catch (Exception e) {
      simplifiedLog.append(responseBody);
    }

    String logEntry = "RESPONE\nStatus: " + wrappedResponse.getStatus() + "\n" + simplifiedLog + "\n";
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(logEntry);
      LOGGER.info("Zapisana wiadomosc: " + logEntry);
    } catch (IOException e) {
      LOGGER.severe("Błąd podczas zapisu pliku: " + e.getMessage());
    }

    wrappedResponse.resetBuffer();
    wrappedResponse.getOutputStream().write(simplifiedLog.toString().getBytes(StandardCharsets.UTF_8));
    wrappedResponse.setContentLength(simplifiedLog.length());

    wrappedResponse.copyBodyToResponse();
  }
}
