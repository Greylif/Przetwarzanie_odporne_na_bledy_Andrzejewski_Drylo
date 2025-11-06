package com.example.lab_02.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

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
      LOGGER.info("Plik został zapisany pomyślnie: " + file.getAbsolutePath());
      LOGGER.info("Zapisana wiadomosc: " + message);
    } catch (IOException e) {
      LOGGER.severe("Błąd podczas zapisu pliku: " + e.getMessage());
      return;
    }

    chain.doFilter(wrappedRequest, wrappedResponse);

    message = "RESPONE\n" + "Status: " + wrappedResponse.getStatus() + "\nBody: " +   new String(wrappedResponse.getContentAsByteArray(), response.getCharacterEncoding()) + "\n";

    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(message);
      LOGGER.info("Plik został zapisany pomyślnie: " + file.getAbsolutePath());
      LOGGER.info("Zapisana wiadomosc: " + message);
    } catch (IOException e) {
      LOGGER.severe("Błąd podczas zapisu pliku: " + e.getMessage());
      return;
    }

    wrappedResponse.copyBodyToResponse();
  }
}
