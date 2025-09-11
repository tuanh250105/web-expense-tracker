FROM tomcat:10.1-jdk21

# Xóa ứng dụng mặc định
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy file WAR đã build sẵn từ Maven (IntelliJ build ra)
COPY target/BudgetBuddy.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
