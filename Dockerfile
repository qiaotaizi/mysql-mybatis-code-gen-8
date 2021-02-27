FROM java:14-alpine
ADD target/mysql-mybatis-code-gen-8-0.0.1-SNAPSHOT.jar app.jar
# 应用运行时暴露端口8080
EXPOSE 8081
# 指定容器运行时的默认命令
ENTRYPOINT ["java","-jar","/app.jar"]

# git pull
# docker stop mmcg && docker rm mmcg
# mvn clean package -Dmaven.test.skip=true && docker rmi mmcg && docker image build --no-cache=true -t mmcg:latest .
# docker run -d --net=host --name mmcg mmcg:latest
