FROM amazoncorretto:19-alpine-jdk
VOLUME /tmp
ARG JAR_FILE
ARG AGENT_ARG
ENV AGENT_ENV=${AGENT_ARG}
ARG COMPRSS_DEVELOPMENT
ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
ARG SPRING_SQL_INIT_MODE
ARG SPRING_REDIS_HOST
ARG SPRING_REDIS_PASSWORD
COPY ${JAR_FILE} app.jar
ENV COMPRSS_DEVELOPMENT ${COMPRSS_DEVELOPMENT}
ENV SPRING_DATASOURCE_URL ${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME ${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD ${SPRING_DATASOURCE_PASSWORD}
ENV SPRING_SQL_INIT_MODE ${SPRING_SQL_INIT_MODE}
ENV SPRING_REDIS_HOST ${SPRING_REDIS_HOST}
ENV SPRING_REDIS_PASSWORD ${SPRING_REDIS_PASSWORD}
ENTRYPOINT java ${AGENT_ENV} -Djava.security.egd=file:/dev/./urandom -jar /app.jar
