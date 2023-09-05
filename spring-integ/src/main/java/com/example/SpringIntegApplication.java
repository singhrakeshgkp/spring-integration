package com.example;

import java.time.Instant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
public class SpringIntegApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegApplication.class, args);
	}

	/*Message channel is kind of pipe, the message moves through channel to components
	* to another channel to another component so on until it eventually terminates.*/
	@Bean
	MessageChannel myChannel(){
		return MessageChannels.direct().getObject();
	}

  @Bean
  IntegrationFlow flow() {
    return IntegrationFlow.from(
            (MessageSource<String>)
                () ->
                    MessageBuilder.withPayload(
                            "Hi welcome to spring integration demo " + Instant.now() + " ")
                        .build(),
            poller -> poller.poller(pm -> pm.fixedRate(1000)))
				/*Additional steps so we can keep on going. this will transfor the msg in upper case*/
				.transform((GenericTransformer<String, String>) source -> source.toUpperCase())
				.handle(
						(GenericHandler<String>)
								(payload, headers) -> {
									System.out.println("payload is :- "+payload);
									return null;//returns null its also terminate/end of the flow
								})
        .get();
  }

}
