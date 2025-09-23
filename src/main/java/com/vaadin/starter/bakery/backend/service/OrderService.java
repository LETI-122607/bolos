package com.vaadin.starter.bakery.backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.vaadin.starter.bakery.backend.data.DashboardData;
import com.vaadin.starter.bakery.backend.data.DeliveryStats;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderSummary;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.OrderRepository;

/**
 * Service class for managing {@link Order} entities.
 * Provides operations for creating, updating, querying, and analyzing orders.
 * Implements business logic for handling orders and generates dashboard statistics.
 */
@Service
public class OrderService implements CrudService<Order> {

    /** Repository for order persistence operations. */
    private final OrderRepository orderRepository;

    /**
     * Constructs an OrderService with the required {@link OrderRepository}.
     *
     * @param orderRepository the order repository
     */
    @Autowired
    public OrderService(OrderRepository orderRepository) {
        super();
        this.orderRepository = orderRepository;
    }

    /** 
     * Set of order states considered not available for delivery statistics. 
     */
    private static final Set<OrderState> notAvailableStates = Collections.unmodifiableSet(
            EnumSet.complementOf(EnumSet.of(OrderState.DELIVERED, OrderState.READY, OrderState.CANCELLED)));

    /**
     * Saves an order, creating a new one if the id is null, or updating an existing order.
     * Uses a BiConsumer to fill order details.
     *
     * @param currentUser the current user
     * @param id the order id, or null for a new order
     * @param orderFiller a BiConsumer to fill order fields
     * @return the saved order
     */
    @Transactional(rollbackOn = Exception.class)
    public Order saveOrder(User currentUser, Long id, BiConsumer<User, Order> orderFiller) {
        Order order;
        if (id == null) {
            order = new Order(currentUser);
        } else {
            order = load(id);
        }
        orderFiller.accept(currentUser, order);
        return orderRepository.save(order);
    }

    /**
     * Saves the given order entity.
     *
     * @param order the order to save
     * @return the saved order
     */
    @Transactional(rollbackOn = Exception.class)
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Adds a comment to the given order and saves the update.
     *
     * @param currentUser the user adding the comment
     * @param order the order being commented on
     * @param comment the comment to add
     * @return the updated order
     */
    @Transactional(rollbackOn = Exception.class)
    public Order addComment(User currentUser, Order order, String comment) {
        order.addHistoryItem(currentUser, comment);
        return orderRepository.save(order);
    }

    /**
     * Finds orders matching the optional customer name filter and/or due date filter, paged.
     *
     * @param optionalFilter optional customer full name filter
     * @param optionalFilterDate optional due date filter
     * @param pageable the paging information
     * @return a page of matching orders
     */
    public Page<Order> findAnyMatchingAfterDueDate(Optional<String> optionalFilter,
            Optional<LocalDate> optionalFilterDate, Pageable pageable) {
        if (optionalFilter.isPresent() && !optionalFilter.get().isEmpty()) {
            if (optionalFilterDate.isPresent()) {
                return orderRepository.findByCustomerFullNameContainingIgnoreCaseAndDueDateAfter(
                        optionalFilter.get(), optionalFilterDate.get(), pageable);
            } else {
                return orderRepository.findByCustomerFullNameContainingIgnoreCase(optionalFilter.get(), pageable);
            }
        } else {
            if (optionalFilterDate.isPresent()) {
                return orderRepository.findByDueDateAfter(optionalFilterDate.get(), pageable);
            } else {
                return orderRepository.findAll(pageable);
            }
        }
    }

    /**
     * Finds orders starting from today (current date).
     *
     * @return list of order summaries starting today or later
     */
    @Transactional
    public List<OrderSummary> findAnyMatchingStartingToday() {
        return orderRepository.findByDueDateGreaterThanEqual(LocalDate.now());
    }

    /**
     * Counts orders matching the optional customer name and/or due date filters.
     *
     * @param optionalFilter optional customer full name filter
     * @param optionalFilterDate optional due date filter
     * @return the count of matching orders
     */
    public long countAnyMatchingAfterDueDate(Optional<String> optionalFilter, Optional<LocalDate> optionalFilterDate) {
        if (optionalFilter.isPresent() && optionalFilterDate.isPresent()) {
            return orderRepository.countByCustomerFullNameContainingIgnoreCaseAndDueDateAfter(optionalFilter.get(),
                    optionalFilterDate.get());
        } else if (optionalFilter.isPresent()) {
            return orderRepository.countByCustomerFullNameContainingIgnoreCase(optionalFilter.get());
        } else if (optionalFilterDate.isPresent()) {
            return orderRepository.countByDueDateAfter(optionalFilterDate.get());
        } else {
            return orderRepository.count();
        }
    }

    /**
     * Generates delivery statistics for the dashboard.
     *
     * @return the delivery statistics for today
     */
    private DeliveryStats getDeliveryStats() {
        DeliveryStats stats = new DeliveryStats();
        LocalDate today = LocalDate.now();
        stats.setDueToday((int) orderRepository.countByDueDate(today));
        stats.setDueTomorrow((int) orderRepository.countByDueDate(today.plusDays(1)));
        stats.setDeliveredToday((int) orderRepository.countByDueDateAndStateIn(today,
                Collections.singleton(OrderState.DELIVERED)));
        stats.setNotAvailableToday((int) orderRepository.countByDueDateAndStateIn(today, notAvailableStates));
        stats.setNewOrders((int) orderRepository.countByState(OrderState.NEW));
        return stats;
    }

    /**
     * Generates dashboard data for the specified month and year.
     * Includes delivery statistics, deliveries per day/month/year, sales, and product deliveries.
     *
     * @param month the month (1-based)
     * @param year the year
     * @return the dashboard data
     */
    public DashboardData getDashboardData(int month, int year) {
        DashboardData data = new DashboardData();
        data.setDeliveryStats(getDeliveryStats());
        data.setDeliveriesThisMonth(getDeliveriesPerDay(month, year));
        data.setDeliveriesThisYear(getDeliveriesPerMonth(year));

        Number[][] salesPerMonth = new Number[3][12];
        data.setSalesPerMonth(salesPerMonth);
        List<Object[]> sales = orderRepository.sumPerMonthLastThreeYears(OrderState.DELIVERED, year);

        for (Object[] salesData : sales) {
            // year, month, deliveries
            int y = year - (int) salesData[0];
            int m = (int) salesData[1] - 1;
            if (y == 0 && m == month - 1) {
                // skip current month as it contains incomplete data
                continue;
            }
            long count = (long) salesData[2];
            salesPerMonth[y][m] = count;
        }

        LinkedHashMap<Product, Integer> productDeliveries = new LinkedHashMap<>();
        data.setProductDeliveries(productDeliveries);
        for (Object[] result : orderRepository.countPerProduct(OrderState.DELIVERED, year, month)) {
            int sum = ((Long) result[0]).intValue();
            Product p = (Product) result[1];
            productDeliveries.put(p, sum);
        }

        return data;
    }

    /**
     * Computes the number of deliveries per day for a given month and year.
     * Missing days are filled with null.
     *
     * @param month the month (1-based)
     * @param year the year
     * @return a list of deliveries per day
     */
    private List<Number> getDeliveriesPerDay(int month, int year) {
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        return flattenAndReplaceMissingWithNull(daysInMonth,
                orderRepository.countPerDay(OrderState.DELIVERED, year, month));
    }

    /**
     * Computes the number of deliveries per month for a given year.
     * Missing months are filled with null.
     *
     * @param year the year
     * @return a list of deliveries per month
     */
    private List<Number> getDeliveriesPerMonth(int year) {
        return flattenAndReplaceMissingWithNull(12, orderRepository.countPerMonth(OrderState.DELIVERED, year));
    }

    /**
     * Helper method to convert a list of object arrays to a fixed-length list of numbers,
     * filling missing entries with null.
     *
     * @param length the expected length of the output list
     * @param list the source list of object arrays ([index, value])
     * @return a list of numbers with nulls where data is missing
     */
    private List<Number> flattenAndReplaceMissingWithNull(int length, List<Object[]> list) {
        List<Number> counts = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            counts.add(null);
        }

        for (Object[] result : list) {
            counts.set((Integer) result[0] - 1, (Number) result[1]);
        }
        return counts;
    }

    /**
     * Returns the repository for CRUD operations.
     *
     * @return the order repository
     */
    @Override
    public JpaRepository<Order, Long> getRepository() {
        return orderRepository;
    }

    /**
     * Creates a new order for the given user, initializing due time and date.
     *
     * @param currentUser the user creating the order
     * @return the new order
     */
    @Override
    @Transactional
    public Order createNew(User currentUser) {
        Order order = new Order(currentUser);
        order.setDueTime(LocalTime.of(16, 0));
        order.setDueDate(LocalDate.now());
        return order;
    }

}
