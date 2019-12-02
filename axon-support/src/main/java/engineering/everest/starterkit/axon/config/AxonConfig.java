package engineering.everest.starterkit.axon.config;

import engineering.everest.starterkit.axon.LoggingMessageHandlerInterceptor;
import engineering.everest.starterkit.axon.CommandValidatingMessageHandlerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
@Configuration
@Profile("!standalone")
public class AxonConfig {

    @Bean
    public RetryScheduler retryScheduler() {
        return IntervalRetryScheduler.builder()
                .retryInterval(1000)
                .maxRetryCount(1)
                .retryExecutor(new ScheduledThreadPoolExecutor(1))
                .build();
    }

    @Bean
    public DefaultCommandGateway defaultCommandGateway(CommandBus commandBus,
                                                       RetryScheduler retryScheduler,
                                                       List<MessageDispatchInterceptor<? super CommandMessage<?>>> dispatchInterceptors) {
        return DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .retryScheduler(retryScheduler)
                .dispatchInterceptors(dispatchInterceptors)
                .build();
    }

    @Bean
    public SimpleCommandBus commandBus(TransactionManager txManager,
                                       AxonConfiguration axonConfiguration,
                                       CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor,
                                       LoggingMessageHandlerInterceptor loggingMessageHandlerInterceptor) {
        var simpleCommandBus = SimpleCommandBus.builder()
                .transactionManager(txManager)
                .messageMonitor(axonConfiguration.messageMonitor(CommandBus.class, "commandBus"))
                .build();
        simpleCommandBus.registerHandlerInterceptor(new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders()));
        simpleCommandBus.registerHandlerInterceptor(commandValidatingMessageHandlerInterceptor);
        simpleCommandBus.registerHandlerInterceptor(loggingMessageHandlerInterceptor);
        return simpleCommandBus;
    }

    @Autowired
    public void configure(EventProcessingConfigurer config,
                          @Value("${application.axon.tracking-event-processor:false}") boolean useTrackingEventProcessor) {
        config.byDefaultAssignTo("default");
        if (useTrackingEventProcessor) {
            LOGGER.info("Configuring Axon with tracking event processors");
            config.usingTrackingEventProcessors();
        } else {
            LOGGER.info("Configuring Axon with subscribing event processors");
            config.usingSubscribingEventProcessors();
        }
    }

    @Bean
    public AnnotationCommandTargetResolver annotationCommandTargetResolver() {
        return AnnotationCommandTargetResolver.builder().build();
    }
}
