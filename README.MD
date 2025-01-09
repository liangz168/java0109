编译打包：
mvn clean package

运行server端：
java -jar target/test-0.0.1.jar
运行consumer端:
java -jar target/test-0.0.1.jar --my.queue=localhost --server.port=8083
运行producer端
java -jar target/test-0.0.1.jar --my.queue=localhost:8081 --server.port=8082 --my.dataPath=data.json
