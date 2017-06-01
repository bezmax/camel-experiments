package com.nulogy.cameledipoc;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.http.NettyHttpMessage;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;

public class Main {
    public static void main(String[] args) throws Exception {
        JndiContext context = new JndiContext();
        context.bind("printer", new Printer());
        context.bind("methodExtractor", new MethodExtractor());

        CamelContext camelContext = new DefaultCamelContext(context);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("netty4-http:http://localhost:8080/test")
                        .pipeline()
                            .to("bean:methodExtractor")
                            .to("bean:printer")
                        .end()
                        .transform().constant("Done");
            }
        });
        camelContext.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    camelContext.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class MethodExtractor {
        @Handler
        public String print(Exchange exchange) {
            return exchange.getIn(NettyHttpMessage.class).getHttpRequest().method().name();
        }
    }

    public static class Printer {
        @Handler
        public void print(String body, Exchange exchange) {
            System.out.println("Received content: "+body);
        }
    }
}
