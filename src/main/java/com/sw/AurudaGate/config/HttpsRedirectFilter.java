package com.sw.AurudaGate.config;

import com.sun.jdi.connect.Connector;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class HttpsRedirectFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // HTTP 요청인지 확인
            if (exchange.getRequest().getURI().getScheme().equals("http")) {
                // HTTPS로 리다이렉트
                String httpsUrl = "https://" +
                        exchange.getRequest().getURI().getHost() +
                        ":" + 8000 + // HTTPS 포트
                        exchange.getRequest().getURI().getPath();
                exchange.getResponse().setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                exchange.getResponse().getHeaders().set("Location", httpsUrl);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }
}

