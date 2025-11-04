package com.example.lab_02.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

    System.out.println("REQUEST");
    System.out.println("URL: " + wrappedRequest.getRequestURL());
    System.out.println("Method: " + wrappedRequest.getMethod());
    System.out.println("Query: " + wrappedRequest.getQueryString());

    chain.doFilter(wrappedRequest, wrappedResponse);

    String responseBody = new String(wrappedResponse.getContentAsByteArray(), response.getCharacterEncoding());

    System.out.println("RESPONSE");
    System.out.println("Status: " + wrappedResponse.getStatus());
    System.out.println("Body: " + responseBody);

    wrappedResponse.copyBodyToResponse();
  }
}
