package com.lazyledger.importer.queue;

import com.lazyledger.importer.service.ImportJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ImportJobConsumer {

    private static final Logger log = LoggerFactory.getLogger(ImportJobConsumer.class);

    private final ImportJobProcessor importJobProcessor;

    public ImportJobConsumer(ImportJobProcessor importJobProcessor) {
        this.importJobProcessor = importJobProcessor;
    }

    @RabbitListener(queues = "${lazyledger.import.queue.name}")
    public void onMessage(ImportTaskMessage message) {
        log.info("Received import job {}", message.jobId());
        importJobProcessor.process(message);
    }
}
