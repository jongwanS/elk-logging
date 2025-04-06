### 로그 레벨의 의미
- trace : 가장 세부적인 수준의 로그, 세부적인 실행 경로를 추적할 때 사용
  - requst, 주로 비지니스 로직과 관련된다.
- debug : 디버깅 목적의 로그
- info : 정보성 로그, 중요한 이벤트나 상태 변화 기록
  - 비지니스 레벨
- warn : 잠재적 문제가 될 수 있는 상황, 즉각적인 영향을 주지 않는 경우 사용
  - 예를들어, 트래픽이 많아져서 api 속도가 특정 시간대를 찍고 서버를 늘리던가, 개선한다. 
- error : 중요한 문제가 발생했음을 나타낸다. 복구가 필요하거나 실패한 작업을 추적
- fatal : 시스템 운영을 계속할 수 없을 정도로 심각 한 오류

### 로그
- 어떤 내용을 로그로 남길지?
  - 콘솔
  - 파일

- 우리가 배울 것은
  - 파일 -> 로그스태시 -> 엘라스틱 서치 -> 키바나

#### 로깅 프레임워크 (Logback)
- logback 이 가장 많이 사용된다.
- 그외에 Log4j2, Log4j, java.util.logging
- **@Slf4j**는 Logback, Log4j, 기타 로깅 프레임워크의 구현체이다. 

#### RollingFileAppender
````xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_FILE}</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>application.%d{yyyy-MM-dd}.log</fileNamePattern><!-- 일단위로 파일이 쌓임 -->
        <maxHistory>30</maxHistory><!-- 30일 저장 -->
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
````
````shell
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "xpack.security.http.ssl.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.10.0 --platform linux/amd64

docker run -d --name logstash -p 5044:5044 -p 9600:9600 -v ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf docker.elastic.co/logstash/logstash:8.10.0

docker network create elastic-network
docker network connect elastic-network elasticsearch
docker network connect elastic-network logstash
````

````
http://localhost:9200/_cat/indices?v
> 인덱스 확인

샤드 : 데이터를 나눠서 저장
````

#### 엘라스틱 서치
- 인덱싱한 로깅 확인
  - http://localhost:9200/application-logs-2025.04.01/_search
- 에그리게이션(집계 하여, 에러로그가 일정 수준에 올라가면 알림이가게끔 할 수 있다.)
  - localhost:9200/application-logs-*/_search
