FROM tomcat:jre8

ADD config/startup.sh /usr/local/tomcat/bin/startup.sh

#Put more secured server.xml
# Removed server banner
# Added Secure flag in cookie
# Changed SHUTDOWN port and Command
ADD config/server.xml /usr/local/tomcat/conf/

#Put more secured web.xml
# Replaced default 404, 403, 500 pages
# Will not show server version info up  on errors and exceptions
ADD config/web.xml /usr/local/tomcat/conf/

#Remove version string from HTTP error messages
#override ServerInfo.properties in catalina.jar
RUN mkdir -p /usr/local/tomcat/lib/org/apache/catalina/util
ADD config/ServerInfo.properties /usr/local/tomcat/lib/org/apache/catalina/util/ServerInfo.properties

#remove redundant apps and unsecure configurations - This is not working and the files are not getting deleted so we run this in start up.sh
#RUN rm -rf /usr/local/tomcat/webapps/* ; \ rm -rf /usr/local/tomcat/work/Catalina/localhost/* ; \ rm -rf /usr/local/tomcat/conf/Catalina/localhost/*

#make tomcat conf dir read only
#RUN chmod -R 400 /usr/local/tomcat/conf

ADD server/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

#Set the Work Directory and Path
WORKDIR /usr/local/tomcat

CMD ["bin/startup.sh"]