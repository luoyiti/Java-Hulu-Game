mvn clean -DskipTests
mvn package -DskipTests
java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -cp target/game-1.0-SNAPSHOT.jar com.gameengine.app.Game