@echo off
REM --- SET CLASSPATH ---
REM The command below includes the current directory (.) and the MySQL JAR file in the classpath.
set CLASSPATH=.;jarFile.jar

REM --- RUN JAVA APPLICATION ---
echo Starting Sanguine Blood Donation App...
java -cp ".;jarFile.jar" SanguineApp

REM IMPORTANT: If the path to your 'java.exe' is incorrect, replace "C:\Program Files\Java\jdk-your-version\bin\java.exe"
REM with the correct full path to your java executable (e.g., C:\Program Files\Java\jdk-21\bin\java.exe).
REM If java is in your system PATH, you can simplify the line above to just: java -cp "%CLASSPATH%" SanguineApp

pause