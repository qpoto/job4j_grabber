package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) throws Exception {
        try (InputStream input = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(input);
            Class.forName(config.getProperty("driver_class"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("login"),
                    config.getProperty("password"));
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement pS = connection.prepareStatement(
                             "INSERT INTO post(name, text, link, created) VALUES (?, ?, ?, ?) "
                                     + "ON CONFLICT (link) do nothing")) {
            pS.setString(1, post.getTitle());
            pS.setString(2, post.getDescription());
            pS.setString(3, post.getLink());
            pS.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            pS.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement pS =
                     connection.prepareStatement("Select * from post")) {
            pS.execute();
            ResultSet rS = pS.getResultSet();
            while (rS.next()) {
                Post postFromDB = getPost(rS);
                posts.add(postFromDB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    private static Post getPost(ResultSet rS) throws SQLException {
        int id = rS.getInt("id");
        String name = rS.getString("name");
        String text = rS.getString("text");
        String link = rS.getString("link");
        Timestamp timestamp = rS.getTimestamp("created");
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return new Post(id, name, link, text, dateTime);
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement pS =
                     connection.prepareStatement("Select * from post where id = (?)", id)) {
            pS.execute();
            ResultSet rS = pS.getResultSet();
            while (rS.next()) {
                post = getPost(rS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}