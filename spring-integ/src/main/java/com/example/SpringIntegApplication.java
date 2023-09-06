package com.example;

import java.io.File;
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
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.SystemPropertyUtils;

@SpringBootApplication
public class SpringIntegApplication {

  public static void main(String[] args) throws InterruptedException {
    SpringApplication.run(SpringIntegApplication.class, args);
    Thread.currentThread().join();
  }

  /*Message channel is kind of pipe, the message moves through channel to components
   * to another channel to another component so on until it eventually terminates.*/
  @Bean
  MessageChannel myChannel() {
    return MessageChannels.direct().getObject();
  }


  static final String REQUESTS_CHANNEL = "requests";
  static final String REPLIES_CHANNEL = "replies";
  @Bean(name = REQUESTS_CHANNEL)
  MessageChannel requests(){
    return  MessageChannels.direct().getObject();
  }

  @Bean(name=REPLIES_CHANNEL)
  MessageChannel replies(){
   return MessageChannels.direct().getObject();
  }

	@Bean
	IntegrationFlow inboundFlow(){
		var dir = new File(SystemPropertyUtils.resolvePlaceholders("${HOME}/Desktop/in"));
		var files = Files.inboundAdapter(dir).autoCreateDirectory(true);
    return IntegrationFlow.from(files, poller -> poller.poller(pm -> pm.fixedRate(1000)))
        .transform(new FileToStringTransformer())
        .handle(
            (GenericHandler<String>) (payload, headers) -> {
              System.out.println("inbound flow start");
              headers.forEach((key, value) -> System.out.println(key + " = " + value));
              return payload;
            }).channel(requests())
        .get();
	}

  @Bean
  IntegrationFlow flow(){
    return IntegrationFlow
        .from(requests())
        .filter(String.class,source->source.contains("Welcome"))
        .transform((GenericTransformer<String, String>)  String::toUpperCase)
        .channel(replies())
        .get();
  }
@Bean
IntegrationFlow outboundFlow(){
    var dir = new File(SystemPropertyUtils.resolvePlaceholders("${HOME}/Desktop/out"));
    return IntegrationFlow
        .from(replies())
        .handle((payload, headers) -> {
          System.out.println("inside outbound flow");
          headers.forEach((key,value)->System.out.println(key +" = "+value));
          return payload;
        }).handle(Files.outboundAdapter(dir).autoCreateDirectory(true))
        .get();
}
}
