/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

/**
 * {@code Shop} class represents an application that manages Products
 * @version 4.0
 * @auhtor saade
 */
package labs.pm.app;

import labs.pm.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;

public class Shop {
    public static void main(String[] args){
        ProductManager pm = new ProductManager("en-GB");
        pm.parseProduct("D,101,Tea,1.99,0,2025-09-08");
        pm.printProductReport(101);
        pm.parseReview("101,4,nice hot tea");
        pm.parseReview("101,4,Nice hot cup of tea");
        pm.parseReview("101,2,Rather weak tea");
        pm.parseReview("101,4,Fine tea");
        pm.parseReview("101,4,Good tea");
        pm.parseReview("101,5,Perfect tea");
        pm.parseReview("101,3,Just add some lemon");
//        Product p2 = pm.createProduct(102,"Coffee",BigDecimal.valueOf(1.99), Rating.NOT_RATED);
//        p2 = pm.reviewProduct(102, Rating.FIVE_STAR, "Strong and rich flavor, just perfect!");
//        p2 = pm.reviewProduct(102, Rating.THREE_STAR, "A bit too bitter for my taste.");
//        p2 = pm.reviewProduct(102, Rating.FOUR_STAR, "Smooth and aromatic, nice morning boost.");
//        p2 = pm.reviewProduct(102, Rating.TWO_STAR, "Tastes slightly burnt.");
//
//        Product p3 = pm.createProduct(103,"Cake",BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(3));
//        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "Delicious, moist, and perfectly sweet!");
//        p3 = pm.reviewProduct(103, Rating.FOUR_STAR, "Good cake but could use more cream.");
//        p3 = pm.reviewProduct(103, Rating.THREE_STAR, "Not bad, but a bit dry.");
//        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "Perfect for dessert lovers!");
//
//        Product p4 = pm.createProduct(104,"Cookie",BigDecimal.valueOf(2.99),Rating.NOT_RATED, LocalDate.now());
//        p4 = pm.reviewProduct(104, Rating.FIVE_STAR, "Crispy on the outside, chewy inside. Amazing!");
//        p4 = pm.reviewProduct(104, Rating.FOUR_STAR, "Tasty, but a bit too sweet.");
//        p4 = pm.reviewProduct(104, Rating.THREE_STAR, "Average cookie, nothing special.");
//        p4 = pm.reviewProduct(104, Rating.FIVE_STAR, "Perfect snack with milk!");
//
//        Product p5 = pm.createProduct(105,"Chocolate",BigDecimal.valueOf(2.99), Rating.NOT_RATED);
//        p5 = pm.reviewProduct(105, Rating.FIVE_STAR, "Rich and smooth, melts in the mouth!");
//        p5 = pm.reviewProduct(105, Rating.FOUR_STAR, "Good quality, but slightly overpriced.");
//        p5 = pm.reviewProduct(105, Rating.FIVE_STAR, "Heavenly taste, highly recommended!");
//        p5 = pm.reviewProduct(105, Rating.THREE_STAR, "Too sweet for me.");
        /*
        pm.printProductReport(101);
        pm.changeLocale("ar-MA");
        pm.printProductReport(102);
        pm.changeLocale("ru-RU");
        pm.printProductReport(103);
        pm.changeLocale("fr-FR");
        pm.printProductReport(104);
        pm.changeLocale("en-GB");
        pm.printProductReport(105);

         */
//        Comparator<Product> ratingSorter = (prd1, prd2)->prd2.getRating().ordinal()-prd1.getRating().ordinal();
//        Comparator<Product> priceSorter = (prd1,prd2)->prd2.getPrice().compareTo(prd1.getPrice());
//        Predicate<Product> priceFilter = (prd -> prd.getPrice().floatValue()<2);
//        pm.printProducts(priceFilter,ratingSorter.thenComparing(priceSorter));
//        pm.getDiscounts().forEach(
//                (rating,discount)->{
//                    System.out.println(rating+"\t"+discount);
//                }
//        );
        pm.printProductReport(101);
        pm.printProductReport(11);
    }
}
