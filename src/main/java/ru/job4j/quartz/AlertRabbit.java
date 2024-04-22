package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            job.getJobDataMap().put("connection", connection);
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(setIntervalFromPropFile())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            try {
                Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
                long millis = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(millis);
                PreparedStatement pS = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?)");
                pS.setTimestamp(1, timestamp);
                pS.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static int setIntervalFromPropFile() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("src/main/resources/rabbit.properties"));
        return Integer.parseInt(properties.getProperty("rabbit.interval"));
    }

    private static Connection getConnection() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader("src/main/resources/rabbit.properties"));
        Class.forName(properties.getProperty("driver_class"));
        String url = properties.getProperty("url");
        String login = properties.getProperty("login");
        String password = properties.getProperty("password");
        return DriverManager.getConnection(url, login, password);
    }
}