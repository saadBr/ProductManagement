/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * @author saade
 **/
public class ProductManager {
//    private Product product;
//    private Review[] reviews = new Review[5];
    private Map<Product, List<Review>> products = new HashMap<>();
    private static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "ru-RU", new ResourceFormatter(Locale.of("ru","RU")),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "ar-MA", new ResourceFormatter(Locale.of("ar","MA")));
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
    public Product reviewProduct(int id, Rating rating, String comment) {
        return reviewProduct(findProduct(id), rating, comment);
    }
    public Product reviewProduct(Product product, Rating rating, String comment) {
//        review = new Review(rating, comment);
        List<Review> reviews = products.get(product);
        products.remove(product,reviews);
        reviews.add(new Review(rating, comment));
        int sum = 0;
        boolean reviewed = false;
        for (Review review: reviews) {
            sum += review.rating().ordinal();
        }
        product = product.applyRating(Reatable.convert(Math.round((float) sum / reviews.size())));
        products.put(product, reviews);
        return product;
    }
    public void printProductReport(Product product) {
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        StringBuilder txt = new StringBuilder();
        //format product
        txt.append(formatter.formatProduct(product));
        txt.append("\n");
        for (Review review : reviews) {
            //format review
            txt.append(formatter.formatReview(review));
            txt.append("\n");
        }
        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no reviews"));
            txt.append("\n");
        }
        System.out.println(txt);
    }
    public void printProductReport(int id){
        printProductReport(findProduct(id));
    }
    public Product findProduct(int id) {
        Product product = null;
        for(Product p: products.keySet()) {
            if(p.getId() == id) {
                product = p;
                break;
            }
        }
        return product;
    }
}
