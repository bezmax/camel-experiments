package com.nulogy.cameledipoc

import org.apache.camel.Exchange
import org.apache.camel.Handler
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.netty4.http.NettyHttpMessage
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.util.jndi.JndiContext

class MethodExtractor {
    @Handler
    def print(Exchange exchange) {
        return exchange.getIn(NettyHttpMessage.class).getHttpRequest().method().name();
    }
}

class Printer {
    @Handler
    def print(String body) {
        System.out.println("Received content: " + body);
    }
}

def context = new JndiContext();
context.bind("printer", new Printer());
context.bind("methodExtractor", new MethodExtractor());

def camelContext = new DefaultCamelContext(context);
camelContext.addRoutes(new RouteBuilder() {
    void configure() {
        from("netty4-http:http://localhost:8080/test")
                .pipeline()
                .to("bean:methodExtractor")
                .to("bean:printer")
                .end()
                .transform().constant("Done");
    }
});
camelContext.start();

Runtime.runtime.addShutdownHook(new Thread({
    try {
        camelContext.stop();
    } catch (Exception e) {
        e.printStackTrace();
    }
}));