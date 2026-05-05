To run this Spring Boot project in IntelliJ IDEA, follow these step-by-step instructions:

Prerequisites
Before you begin, ensure you have the following installed on your system:

Java Development Kit (JDK) 17: This project is configured to use Java 17.
MySQL Server: The project uses MySQL for data storage.
IntelliJ IDEA: (Community or Ultimate edition).

Set Up the Database
The project is configured to connect to a MySQL database named university_reminder.

Open your MySQL terminal or a tool like MySQL Workbench.
Create the database:

SQL
CREATE DATABASE university_reminder;
Check the src/main/resources/application.properties file and ensure the credentials match your local MySQL setup:
spring.datasource.username=root
spring.datasource.password=1234 (Change this to your actual password if it's different).
 
3. Open the Project in IntelliJ
Launch IntelliJ IDEA.
Click File > Open.
Navigate to the project folder: c:\Users\Janidu\Documents\GitHub\Web based Bill and Event Reminder System and click OK.
If prompted, select "Trust Project".
4. Configure the Java SDK
Go to File > Project Structure (or press Ctrl + Alt + Shift + S).
Under Project, ensure the SDK is set to 17.
If you don't have JDK 17 installed, click Add SDK > Download JDK and select version 17.
Click Apply and OK.

5. Load Maven Dependencies
IntelliJ should automatically detect that this is a Maven project (since it has a pom.xml file).

A notification might appear in the bottom-right corner saying "Maven projects need to be imported". Click Enable Auto-Import or Load Maven Changes.
Alternatively, open the Maven tool window on the right side of the IDE and click the Reload All Maven Projects (circular arrow) icon.
Wait for IntelliJ to download all dependencies (Lombok, Spring Boot, MySQL Driver, etc.).

6. Run the Application
In the project explorer, navigate to: src/main/java/com/university/reminderapp/ReminderAppApplication.java
Right-click on the ReminderAppApplication class and select Run 'ReminderAppApplication'.
Alternatively, click the green Play icon in the gutter next to the public static void main method.

7. Access the System
Once the console shows that the application has started (usually says Started ReminderAppApplication in X seconds):

Open your web browser.
Go to: http://localhost:8081 (Note: The project is configured to run on port 8081, not the default 8080).