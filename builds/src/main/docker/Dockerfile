FROM frolvlad/alpine-oraclejdk8
VOLUME /tmp
ADD builds.jar app.jar
RUN touch /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
