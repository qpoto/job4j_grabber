package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 1;

    public static void main(String[] args) throws IOException {
        for (int pageNumber = 1; pageNumber <= PAGES; pageNumber++) {
            String fullLinkToVacancies = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLinkToVacancies);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                String date = row.select(".basic-date").attr("datetime");
                String name = row.select(".vacancy-card__title").first().text();
                String linkVacancy = row.select(".vacancy-card__title-link").attr("href");
                String fullLink = String.format("%s%s", SOURCE_LINK, linkVacancy);
                try {
                    System.out.printf("%s %s %s%n", date, name, fullLink);
                    System.out.println("Описание");
                    System.out.println(retrieveDescription(fullLink));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
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