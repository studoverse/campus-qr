#debian based
FROM openjdk:11-jdk

ENV APPLICATION_USER ktor
RUN echo $APPLICATION_USER
RUN adduser --disabled-password --gecos '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

RUN git clone https://github.com/studo-app/campus-qr.git /src-code

RUN chown -R $APPLICATION_USER /src-code

USER $APPLICATION_USER

WORKDIR /src-code
RUN ./gradlew stage # Stage command will also be used by Heroku/Scalingo file

RUN cp Server.jar /app/Server.jar
WORKDIR /app

CMD ["java", "-jar", "Server.jar"]
