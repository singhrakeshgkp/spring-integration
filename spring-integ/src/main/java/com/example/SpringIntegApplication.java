package com.example;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class SpringIntegApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringIntegApplication.class, args);
  }

  /*Message channel is kind of pipe, the message moves through channel to components
   * to another channel to another component so on until it eventually terminates.*/
  @Bean
  MessageChannel myChannel() {
    return MessageChannels.direct().getObject();
  }

	private static String msg(){
		double number =  Math.random();
		return number >.29 ?"Hi This is Even Number "+Instant.now()+" number "+number+"": "Hi This is Odd number, and number is "+number;
	}

	@Component
	static class MsgSource  implements  MessageSource<String>{

		@Override
		public Message<String> receive() {
			return MessageBuilder.withPayload(msg()).build();
		}
	}

	@Bean
	ApplicationRunner runner(MsgSource msgSource, IntegrationFlowContext context){
		return args -> {
var evenFlow = getFlow(msgSource,1,"Even");
		 var oddFlow = getFlow(msgSource,3,"Odd");
			List.of(evenFlow,oddFlow).forEach(
					flow -> context.registration(flow).register().start());
		};
	}


  IntegrationFlow getFlow(MessageSource messageSource, int seconds, String filteredMsg) {
    return IntegrationFlow.from(messageSource,
            poller -> poller.poller(pm -> pm.fixedRate(seconds*1000)))
				.filter(String.class,source -> source.contains(filteredMsg))
        .transform((GenericTransformer<String, String>) source -> source.toUpperCase())
        .handle(
            (GenericHandler<String>)
                (payload, headers) -> {
                  System.out.println("payload for filtered msg [ " +filteredMsg+"] is, "+ payload);
                  return null; // returns null its also terminate/end of the flow
                })
        .get();
  }
}
