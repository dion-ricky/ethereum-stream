FROM maven:3.8-eclipse-temurin-17 AS builder

COPY ./consumer /opt/consumer
RUN cd /opt/consumer; mvn clean verify

FROM apache/flink:1.20.1-java11

RUN wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.4.0-1.20/flink-sql-connector-kafka-3.4.0-1.20.jar; \
    wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/flink/flink-connector-jdbc/3.2.0-1.19/flink-connector-jdbc-3.2.0-1.19.jar; \
    wget -P /opt/flink/lib/ https://jdbc.postgresql.org/download/postgresql-42.7.5.jar;

COPY --from=builder /opt/consumer/target/consumer-*.jar /opt/flink/usrlib/consumer.jar

# RUN echo "execution.checkpointing.interval: 10s" >> /opt/flink/conf/flink-conf.yaml; \
#     echo "pipeline.object-reuse: true" >> /opt/flink/conf/flink-conf.yaml; \
#     echo "pipeline.time-characteristic: EventTime" >> /opt/flink/conf/flink-conf.yaml; \
#     echo "taskmanager.memory.jvm-metaspace.size: 256m" >> /opt/flink/conf/flink-conf.yaml;
