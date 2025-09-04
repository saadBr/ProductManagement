/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * {@code Product} class represents properties and behavior of
 * product objects in the Product Management System.
 * Each product has an id, name and price
 * <br>
 * Each product can have a discount, calculated based on a
 * {@link DISCOUNT_RATE discount_rate}
 * @version 4.0
 * @author saade
 **/
public sealed abstract class Product implements Reatable<Product> permits Food,Drink {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private final Rating rating;

    Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public BigDecimal getPrice() {
        return price;
    }


    public BigDecimal getDiscount(){
        return price.multiply(DISCOUNT_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Rating getRating() {return rating;}

    public abstract Product applyRating(Rating newRating);
    /*
    * Assumes that the best before date is today
    * @return the current date
     */
    public LocalDate getBestBefore() {
        return LocalDate.now();
    }

    @Override
    public String toString() {
        return id + " " + name + " " + price + " " + getDiscount() + " " + rating.getStars()+ " " + getBestBefore();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;
        return id == product.id ;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
