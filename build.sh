git pull
docker stop mmcg && docker rm mmcg
mvn clean package -Dmaven.test.skip=true && docker rmi mmcg && docker image build --no-cache=true -t mmcg:latest .
docker run -d --net=host --name mmcg mmcg:latest
echo '容器已经重启，当前分支：'
git branch | awk  '$1 == "*"{print $2}'
