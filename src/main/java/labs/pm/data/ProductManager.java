/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author saade
 **/
public class ProductManager {
//    private Product product;
//    private Review[] reviews = new Review[5];
    private Map<Product, List<Review>> products = new HashMap<>();
    private ResourceBundle config = ResourceBundle.getBundle("config");
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "ru-RU", new ResourceFormatter(Locale.of("ru","RU")),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "ar-MA", new ResourceFormatter(Locale.of("ar","MA")));
    private static Logger logger = Logger.getLogger(ProductManager.class.getName());
    private Path reportsFolder =
            Path.of(config.getString("reports.folder"));
    private Path dataFolder =
            Path.of(config.getString("data.folder"));
    private Path tempFolder =
            Path.of(config.getString("temp.folder"));
    private static class ResourceFormatter{
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale){
            this.locale = locale;
            resources = ResourceBundle.getBundle("resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }
        private String formatProduct(Product product){
            String type = switch (product){
                case Food food -> resources.getString("food");
                case Drink drink -> resources.getString("drink");
            };
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()),
                    type);
        }
        private String formatReview(Review review){
            return MessageFormat.format(resources.getString("review"),
                    review.rating().getStars(),
                    review.comment());
        }

        private String getText(String key){
            return resources.getString(key);
        }
    }
    private ResourceFormatter formatter;

    public void changeLocale(String languageTag){
        formatter = formatters.getOrDefault(languageTag, formatters.get("ar-MA"));
    }

    public static Set<String> getSupportedLanguages(){
        return formatters.keySet();
    }
    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }
    public ProductManager(String languageTag) {
        changeLocale(languageTag);
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }
    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }
    public Product reviewProduct(int id, Rating rating, String comment){
        try {
            return reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }
    public Product reviewProduct(Product product, Rating rating, String comment) {
        List<Review> reviews = products.get(product);
        products.remove(product,reviews);
        reviews.add(new Review(rating, comment));
        product = product.applyRating(Reatable.convert(
                (int)Math.round(
                        reviews.stream()
                                .mapToInt(r->r.rating().ordinal())
                                .average()
                                .orElse(0)
                )
        ));
        products.put(product, reviews);
        return product;
    }
    public void printProductReport(Product product) throws IOException {
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        Path prodcutFile = reportsFolder.resolve(
                MessageFormat.format(
                        config.getString("report.file"),product.getId()
                )
        );
        try(PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(prodcutFile, StandardOpenOption.CREATE),
                        StandardCharsets.UTF_8)))
        {
            out.append(formatter.formatProduct(product)+System.lineSeparator());
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews")+System.lineSeparator());
            }
            else {
                out.append(reviews.stream()
                        .map(review -> formatter.formatReview(review)+System.lineSeparator()).collect(Collectors.joining()));
            }
        }
    }
    public void printProductReport(int id){
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Error while printing product report for " + id, e.getMessage());
        }
    }
    public Product findProduct(int id) throws ProductManagerException {
        return products.keySet()
                .stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ProductManagerException("product "+ id + " not found"));
    }
    public void printProducts(Predicate<Product> filter,Comparator<Product> sorter) {
        StringBuilder txt = new StringBuilder();
        products.keySet()
                .stream()
                .sorted(sorter)
                .filter(filter)
                .forEach(product -> {txt.append(formatter.formatProduct(product)).append("\n");});
        System.out.println(txt);
    }
    public void parseReview(String text) {
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String) values[0]),
                    Reatable.convert(Integer.parseInt((String)values[1])),(String)values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "error parsing review "+text+e.getMessage());
        }
    }
    public void parseProduct(String text) {
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String)values[1]);
            String name =  (String)values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String)values[3]));
            Rating rating = Reatable.convert(Integer.parseInt((String)values[4]));
            switch ((String)values[0]) {
                case "D":
                    createProduct(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    createProduct(id,name,price,rating,bestBefore);
            }
        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING, "error parsing product "+text+e.getMessage());
        }
    }
    public Map<String, String> getDiscounts() {
        return products.keySet()
                .stream()
                .collect(Collectors.groupingBy(
                        product -> product.getRating().getStars(),
                        Collectors.collectingAndThen(Collectors.summingDouble(
                                product -> product.getDiscount().doubleValue()),
                                discount -> formatter.moneyFormat.format(discount)
                        )));
    }
}
