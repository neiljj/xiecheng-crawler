FROM java:8
EXPOSE 8080
VOLUME /tmp
ADD xiechengCrawler-0.0.1-SNAPSHOT.jar xiechengcrawler.jar
RUN sh -c 'touch /xiechengcrawler.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /xiechengcrawler.jar" ]
