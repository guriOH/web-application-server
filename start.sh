mvn clean package


pid=`ps -ef | grep java | grep 7070 | cut -d" " -f 5`

echo $pid

if [[ ! -z $pid ]]
then
  kill -9 $pid
fi
java -cp target/classes:target/dependency/* webserver.WebServer 7070 &