package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse parse = new HabrCareerParse(dateTimeParser);
        List<Post> posts = parse.list(SOURCE_LINK);
        for (Post post : posts) {
            System.out.println(post.toString());
        }
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGES; pageNumber++) {
            String fullLinkToVacancies = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLinkToVacancies);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                String title = row.select(".vacancy-card__title").first().text();
                String linkVacancy = row.select(".vacancy-card__title-link").attr("href");
                String fullLink = String.format("%s%s", SOURCE_LINK, linkVacancy);
                LocalDateTime date = dateTimeParser.parse(row.select(".basic-date").attr("datetime"));
                int id = Integer.parseInt(linkVacancy.split("/")[2]);
                try {
                    String description = retrieveDescription(fullLink);
                    posts.add(new Post(id, title, fullLink, description, date));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return posts;
    }

    private static String retrieveDescription(String fullLink) throws IOException {
        List<String> tags = List.of("p", "h3", "li", "p");
        StringBuilder vacancyDescription = new StringBuilder();
        Connection connection = Jsoup.connect(fullLink);
        Document document = connection.get();
        Element vacancyDescriptionElement = document.select(".basic-section--appearance-vacancy-description").first();
        for (Element element : vacancyDescriptionElement.getAllElements()) {
            if (tags.contains(element.tagName())) {
                String nextLine = System.lineSeparator();
                if (element.tagName().equals("li")) {
                    vacancyDescription.append(" - " + element.text() + nextLine);
                } else {
                    vacancyDescription.append(element.text() + nextLine);
                }
            }
        }
        return vacancyDescription.toString();
    }


}