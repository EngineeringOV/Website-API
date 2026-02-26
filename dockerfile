FROM eclipse-temurin:18-jdk AS build
WORKDIR /app
RUN apt-get update && apt-get install -y gcc libgmp-dev make git wget unzip && rm -rf /var/lib/apt/lists/*

RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip
ENV PATH="/opt/gradle-8.5/bin:${PATH}"
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -Dorg.gradle.caching=false"

RUN git clone https://github.com/EngineeringOV/GMP-java.git /tmp/gmp-java \
    && cd /tmp/gmp-java && make

COPY build.gradle .
COPY settings.gradle .
RUN mkdir -p lib && cp $(find /tmp/gmp-java -name "*.jar" | head -1) lib/

# Clean any stale gradle cache and download dependencies
RUN gradle dependencies --no-daemon --no-build-cache || true

COPY src src
RUN gradle bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:18-jre
WORKDIR /app
RUN apt-get update && apt-get install -y libgmp10 default-mysql-client gettext-base && rm -rf /var/lib/apt/lists/*

COPY --from=build /tmp/gmp-java/libjcl.so /lib/
COPY --from=build /tmp/gmp-java/libnativegmp.so /lib/
COPY --from=build /app/build/libs/*.jar app.jar

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]