Creating war file for the Search 
Apps needed - Maven, Java

1. Check out the code from the git repository and run 

mvn clean install on the root directory

2. The build process will take around 10 min as it installs node.js and its modules and then builds angular webpack dist using the pom inside the client directory.

3. The server pom.xml will now create a ROOT.war with client angular code and jersey server code.


Building Docker image and running Docker for Search UI

To build docker image run the following command:

docker build -t uspto-docker-searchui .

To run in detached mode execute the following command:

docker run -p 5000:5000 -d --env-file config/properties.env uspto-docker-searchui

Use docker ps to see the container ID and use that for seeing logs using
docker logs <Container ID > and stop using docker stop <container ID >.