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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Shop {
    public static void main(String[] args){
        ProductManager pm = ProductManager.getInstance();
        AtomicInteger clientCount = new AtomicInteger(0);
        Callable<String> client = ()->{
            String  clientId = "Client" + clientCount.getAndIncrement();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(20)+101;
            String languageTag =
                    ProductManager.getSupportedLanguages()
                            .stream()
                            .skip(ThreadLocalRandom.current().nextInt(4))
                            .findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId).append(" ").append(threadName).append("\n-\tstart of log\t-\n");
            log.append(pm.getDiscounts(languageTag)
                    .entrySet()
                    .stream()
                    .map(entry->entry.getKey()+"\t"+entry.getValue())
                    .collect(Collectors.joining("\n")));
            Product product = pm.reviewProduct(productId,Rating.FOUR_STAR,"Yet another review");
            log.append((product!=null) ? "\nProduct"+productId+ "reviewed\n"
                    :"\nProduct"+productId+ "not reviewed\n");
            pm.printProductReport(productId,languageTag,clientId);
            log.append(clientId+" generated report for "+productId+" product");
            log.append("\n-\tend of log\t-\n");
            return log.toString();
        };
        List<Callable<String>> clients = Stream.generate(()->client)
                .limit(5)
                .collect(Collectors.toList());
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executor.invokeAll(clients);
            executor.shutdown();
            results.forEach(result->{
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retrieving client log", e);
                }
            });
        } catch (InterruptedException e) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients", e);
        }
    }
}
