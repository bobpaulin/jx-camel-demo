package com.example;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

	private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
	
    @Override
    public void configure() {
    	
    	
    	restConfiguration().component("netty4-http").port(8880).bindingMode(RestBindingMode.json);
    	
    	rest("greeting").get()
			.outType(Greeting.class)
			.to("direct:greeting");
    	
    	rest("actuator/health").get()
    		.to("direct:health");
    	
    	from("direct:greeting")
		.process(new Processor() {
			
			public void process(Exchange exchange) throws Exception {
				String name = exchange.getIn().getHeader("name", String.class);
				
				exchange.getIn().setBody(new Greeting(counter.incrementAndGet(),
                        String.format(template, name)));
			}
		});
    	
    	from("direct:health")
    	.process(new Processor() {
			
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setBody(new HashMap());
			}
		});
    	
        from("timer:hello?period={{timer.period}}").routeId("hello")
            .transform().method("myBean", "saySomething")
            .filter(simple("${body} contains 'foo'"))
                .to("log:foo")
            .end()
            .to("stream:out");
    }

}
