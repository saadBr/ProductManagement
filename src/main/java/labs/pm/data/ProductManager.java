/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private final ResourceBundle config = ResourceBundle.getBundle("config");
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final static Map<String, ResourceFormatter> formatters =
            Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "ru-RU", new ResourceFormatter(Locale.of("ru","RU")),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "ar-MA", new ResourceFormatter(Locale.of("ar","MA")));
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private final Path reportsFolder =
            Path.of(config.getString("reports.folder"));
    private final Path dataFolder =
            Path.of(config.getString("data.folder"));
    private final Path tempFolder =
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
    // private ResourceFormatter formatter;
    private static final ProductManager pm = new ProductManager();
    public static ProductManager getInstance(){
        return pm;
    }
    public ResourceFormatter changeLocale(String languageTag){
        return formatters.getOrDefault(languageTag, formatters.get("ar-MA"));
    }

    public static Set<String> getSupportedLanguages(){
        return formatters.keySet();
    }
    private ProductManager() {
        loadAllData();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<>());
        }catch (Exception e){
            logger.log(Level.INFO, "Error creating product", e.getMessage());
            return null;
        }
        finally {
            writeLock.unlock();
        }
        return product;
    }
    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        }catch (Exception e){
            logger.log(Level.INFO, "Error creating product", e.getMessage());
        }finally {
            writeLock.unlock();
        }
        return product;
    }
    public Product reviewProduct(int id, Rating rating, String comment){
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }
    }
    private Product reviewProduct(Product product, Rating rating, String comment) {
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
    private void printProductReport(Product product, String languageTag, String client) throws IOException {
        ResourceFormatter formatter = changeLocale(languageTag);
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(
                        config.getString("report.file"),product.getId(),client
                )
        );
        try(PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(productFile, StandardOpenOption.CREATE),
                        StandardCharsets.UTF_8)))
        {
            out.append(formatter.formatProduct(product)).append(System.lineSeparator());
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews")).append(System.lineSeparator());
            }
            else {
                out.append(reviews.stream()
                        .map(review -> formatter.formatReview(review)+System.lineSeparator()).collect(Collectors.joining()));
            }
        }
    }
    public void printProductReport(int id, String languageTag, String client) throws IOException {
        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Error while printing product report for " + id, e.getMessage());
        } finally {
            readLock.unlock();
        }
    }
    public Product findProduct(int id) throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter(product -> product.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new ProductManagerException("product "+ id + " not found"));
        }finally {
            readLock.unlock();
        }
    }
    public void printProducts(Predicate<Product> filter,Comparator<Product> sorter, String languageTag) throws ProductManagerException {
        try {
            readLock.lock();
            ResourceFormatter formatter = changeLocale(languageTag);
            StringBuilder txt = new StringBuilder();
            products.keySet()
                    .stream()
                    .sorted(sorter)
                    .filter(filter)
                    .forEach(product -> {txt.append(formatter.formatProduct(product)).append("\n");});
            System.out.println(txt);
        }finally {
            readLock.unlock();
        }

    }
    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Reatable.convert(Integer.parseInt((String)values[0])),
                    (String)values[1]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "error parsing review "+text+e.getMessage());
        }
        return review;
    }
    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String)values[1]);
            String name =  (String)values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String)values[3]));
            Rating rating = Reatable.convert(Integer.parseInt((String)values[4]));
            switch ((String)values[0]) {
                case "D":
                    product = new Drink(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    product = new Food(id,name,price,rating,bestBefore);
            }
        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING, "error parsing product "+text+e.getMessage());
        }
        return product;
    }
    public Map<String, String> getDiscounts(String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = changeLocale(languageTag);
            return products.keySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            product -> product.getRating().getStars(),
                            Collectors.collectingAndThen(Collectors.summingDouble(
                                            product -> product.getDiscount().doubleValue()),
                                    discount -> formatter.moneyFormat.format(discount)
                            )));
        }finally {
            readLock.unlock();
        }

    }
    private List<Review> loadReviews(Product product){
        List<Review> reviews;
        Path file = dataFolder.resolve(
                MessageFormat.format(config.getString("reviews.data.file"), product.getId())
        );
        if(Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, StandardCharsets.UTF_8)
                        .map(this::parseReview)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!reviews.isEmpty()) {
                    product = product.applyRating(Reatable.convert(
                            (int) Math.round(
                                    reviews.stream()
                                            .mapToInt(r -> r.rating().ordinal())
                                            .average()
                                            .orElse(0)
                            )
                    ));
                }
            } catch (IOException e) {
                logger.log(Level.WARNING,"Error while loading reviews for " + product.getId(), e.getMessage());
                reviews = new ArrayList<>();
            }
        }
        return reviews;
    }

    private Product loadProduct(Path file) {
        Product product = null;
        try {
            product = parseProduct(
                    Files.lines(dataFolder
                            .resolve(file),
                            Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (Exception e) {
            logger.log(Level.WARNING,"Error while loading product "+file,e.getMessage());
        }
        return product;
    }

    private void loadAllData(){
        try {
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(this::loadProduct)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            product -> {
                                List<Review> reviews = loadReviews(product);
                                if (!reviews.isEmpty()) {
                                    product = product.applyRating(Reatable.convert(
                                            (int) Math.round(
                                                    reviews.stream().mapToInt(r -> r.rating().ordinal()).average().orElse(0)
                                            )
                                    ));
                                }
                                return product;
                            },
                            this::loadReviews
                    ));
        } catch (IOException e) {
            logger.log(Level.WARNING,"Error while loading data",e.getMessage());
        }
    }
    private void dumpData(){
        try {
            if(Files.notExists(tempFolder)) {
                Files.createDirectories(tempFolder);
            }
            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.now()))
            );
            try(ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile,StandardOpenOption.CREATE))){
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException e){
            logger.log(Level.WARNING,"Error while dumping data",e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private void restoreData(){
        try{
            Path tempFile = Files.list(tempFolder)
                    .filter(path ->
                            path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try(ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile,StandardOpenOption.DELETE_ON_CLOSE)
            )){
                products = (HashMap) in.readObject();
            }
        }catch (Exception e){
            logger.log(Level.WARNING,"Error while restoring data",e.getMessage());
        }
    }
}
