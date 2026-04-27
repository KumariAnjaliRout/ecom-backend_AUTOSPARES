package com.automobile.ecom.service;


import com.automobile.ecom.document.ProductDocument;
import com.automobile.ecom.dto.*;
import com.automobile.ecom.entity.*;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.exception.UnauthorizedException;
import com.automobile.ecom.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ProductSearchRepository productsearchRepository;



    public OrderItemsDTO addToBag(OrderItemsDTO dto, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));


       //  NEW CHECK 1: ACTIVE PRODUCT
        if (!product.getIsActive()) {
            throw new BadRequestException("Product is no longer available");
        }

        // NEW CHECK 2: CATEGORY VALIDATION
        if (product.getSubCategory() == null ||
                product.getSubCategory().getCategory() == null) {
            throw new BadRequestException("Invalid product (category removed)");
        }

       // EXISTING STOCK CHECK
        if (product.getStockQuantity() <= 0) {
            throw new BadRequestException("Product is out of stock");
        }


        //  FIX 1: OUT OF STOCK CHECK
        if (product.getStockQuantity() <= 0) {
            throw new BadRequestException("Product is out of stock");
        }

        //  CHECK IF PRODUCT ALREADY EXISTS IN CART
        OrderItems existing = orderItemsRepository
                .findByUser_IdAndProduct_IdAndOrderIsNull(userId, dto.getProductId());

        if (!productRepository.existsById(dto.getProductId())) {
            throw new ResourceNotFoundException("Product does not exist anymore");
        }

        if (existing != null) {

            int newQty = existing.getQuantity() + dto.getQuantity();

            //  FIX 2: CHECK AGAINST STOCK
            if (newQty > product.getStockQuantity()) {
                throw new BadRequestException(
                        "Only " + product.getStockQuantity() + " items available in stock"
                );
            }

            existing.setQuantity(newQty);
            return convert(orderItemsRepository.save(existing));
        }

        //  FIX 3: VALIDATE NEW ITEM
        if (dto.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException(
                    "Only " + product.getStockQuantity() + " items available in stock"
            );
        }

        OrderItems item = new OrderItems();
        item.setUser(user);
        item.setProduct(product);
        item.setOrder(null);

        item.setPname(product.getName());
        item.setActualPrice(product.getActualPrice().doubleValue());
        item.setDiscount(product.getDiscount().doubleValue());
        item.setPrice(product.getPrice().doubleValue());
        item.setQuantity(dto.getQuantity());

        return convert(orderItemsRepository.save(item));
    }
    // ================= REMOVE =================
    public void removeFromBag(Long id, UUID userId) {

        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized");
        }

        orderItemsRepository.delete(item);
    }

    public void clearCart(UUID userId) {

        List<OrderItems> items =
                orderItemsRepository.findByUser_IdAndOrderIsNull(userId);

        orderItemsRepository.deleteAll(items);
    }

    public OrderItemsDTO updateCartItemQuantity(Long id, Integer qty, UUID userId) {

        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized");
        }
        Product product = item.getProduct(); // already mapped
        int newQty = item.getQuantity() + qty;

        // REMOVE ITEM IF <= 0
        if (newQty <= 0) {
            orderItemsRepository.delete(item);
            return null;
        }

        //STOCK VALIDATION
        if (newQty > product.getStockQuantity()) {
            throw new ResourceNotFoundException(
                    "Only " + product.getStockQuantity() + " items available in stock"
            );
        }
        item.setQuantity(newQty);
        return convert(orderItemsRepository.save(item));
    }
@Transactional
public OrderResponseDTO placeOrder(OrderRequestDTO req, UUID userId) {

    // ================= USER =================
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // ================= DEFAULT ADDRESS =================
    Address address = addressRepository
            .findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No default address found"));

    // ================= CART ITEMS =================
    List<OrderItems> items = orderItemsRepository.findAllById(req.getOrderItemIds());
    validateCartItems(items);

    if (items.isEmpty()) {
        throw new ResourceNotFoundException("Cart is empty");
    }

    // Ensure items belong to user
    items.forEach(i -> {
        if (!i.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid cart item");
        }
    });

    // ================= STOCK VALIDATION (NO UPDATE HERE) =================
    for (OrderItems item : items) {
        Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < item.getQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock for product: " + product.getName()
            );
        }

        // Attach latest product (important)
        item.setProduct(product);
    }
    // ================= LOW STOCK ALERT =================
    for (OrderItems item : items) {

        Product product = item.getProduct();

        if (product.getStockQuantity() == 1) {
            try {
                notificationService.sendToAllAdmins(
                        SendNotificationRequest.builder()
                                .senderId(userId)
                                .title("Low Stock Alert")
                                .category(Category.PRODUCT)
                                .notificationType(NotificationType.INSUFFICIENT_STOCK)
                                .message("Product '" + product.getName() + "' is running low (only 1 left)")
                                .link("/product")
                                .notificationChannel(NotificationChannel.BOTH)
                                .build()
                );
            }
            catch (Exception e) {
                log.error("Failed to send notification for Insufficient stock for product: {}",product.getName(), e);
            }
        }
    }

    // ================= PRICE CALCULATION =================
    double total = items.stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();

    double delivery = total >= 500 ? 0 : 100;

    // ================= ORDER CREATION =================
    Order order = new Order();
    order.setUser(user);
    order.setAddress(address);
    order.setTotalAmount(total);
    order.setDeliveryCost(delivery);
    order.setOrderStatus(OrderStatus.PLACED);
    order.setPaymentMode(PaymentMode.COD);

    Order savedOrder = orderRepository.save(order);

    // ================= ATTACH ITEMS =================
    items.forEach(i -> i.setOrder(savedOrder));
    orderItemsRepository.saveAll(items);

    // ================= STOCK REDUCTION (AFTER ORDER SAVE) =================
    for (OrderItems item : items) {

        Product product = item.getProduct();

        int newStock = product.getStockQuantity() - item.getQuantity();

        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(newStock);
        Product savedProduct = productRepository.save(product);

        // 🔥 INDEX INTO ELASTICSEARCH
        try {
            ProductDocument doc = buildDocument(savedProduct);
            productsearchRepository.save(doc);
        }
        catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
    }

    // ================= PAYMENT CREATION =================
    Payment payment = new Payment();
    payment.setOrder(savedOrder);
    payment.setUid(userId);
    payment.setAmount(total + delivery);
    payment.setPaymentStatus(PaymentStatus.CREATE);

    paymentRepository.save(payment);

    // ================= NOTIFICATIONS =================
   try{
    notificationService.sendToAllAdmins(
            SendNotificationRequest.builder()
                    .senderId(userId)
                    .title("New Order Placed")
                    .category(Category.ORDER)
                    .notificationType(NotificationType.ORDER_PLACED)
                    .message("User placed order ID: " + savedOrder.getId())
                    .link("/orders")
                    .notificationChannel(NotificationChannel.BOTH)
                    .build()
    );
    } catch (Exception e) {
        log.error("Failed to send notification for orderId: {}",savedOrder.getId(), e);
    }
    try{
    notificationService.sendNotification(
            SendNotificationRequest.builder()
                    .senderId(userId)
                    .receiverId(userId)
                    .title("Order Confirmed")
                    .category(Category.ORDER)
                    .message("Your order has been placed successfully")
                    .link("/orders")
                    .notificationType(NotificationType.ORDER_PLACED)
                    .notificationChannel(NotificationChannel.BOTH)
                    .build()
    );
    } catch (Exception e) {
        log.error("Failed to send notification for orderId: {}",savedOrder.getId(), e);
    }

    log.info("COD Order placed successfully for orderId: {}", savedOrder.getId());

    return convert(savedOrder, items);
}

    private void validateCartItems(List<OrderItems> items) {

        for (OrderItems item : items) {

            Product product = productRepository.findById(item.getProduct().getId())
                    .orElse(null);

            if (product == null || !product.getIsActive()) {
                throw new BadRequestException(
                        "Product removed: " + item.getPname()
                );
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for: " + product.getName()
                );
            }
        }
    }
    //for online
    public OrderResponseDTO placeOnlineOrder(OrderRequestDTO req, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository
                .findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
        List<OrderItems> items = orderItemsRepository.findAllById(req.getOrderItemIds());

        if (items.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty");
        }

        items.forEach(i -> {
            if (!i.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Invalid cart item");
            }
        });

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        double delivery = total >= 500 ? 0 : 100;

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setTotalAmount(total);
        order.setDeliveryCost(delivery);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentMode(PaymentMode.ONLINE);

        for (OrderItems item : items) {
            Product product = item.getProduct();

            int newStock = product.getStockQuantity() - item.getQuantity();

            if (newStock < 0) {
                throw new ResourceNotFoundException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(newStock);

            Product savedProduct = productRepository.save(product);
            try {
                ProductDocument doc = buildDocument(savedProduct);
                productsearchRepository.save(doc);
            } catch (Exception e) {
                System.out.println("ES sync failed: " + e.getMessage());
            }

        }

        Order saved = orderRepository.save(order);

        auditService.logAction(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "ORDER_CREATED",
                "ORDER",
                String.valueOf(order.getId()),
                "Online order created (payment pending)"
        );

        items.forEach(i -> i.setOrder(saved));
        orderItemsRepository.saveAll(items);

        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setUid(userId);
        payment.setAmount(total + delivery);
        payment.setPaymentStatus(PaymentStatus.CREATE);

        paymentRepository.save(payment);

        log.info("ONLINE Order placed successfully for orderId: {}", saved.getId());

        return convert(saved, items);
    }

    // ================= GET =================
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        return convert(order,
                orderItemsRepository.findByOrder_Id(id));
    }

    public List<OrderResponseDTO> getOrdersByUser(UUID userId) {
        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(o -> convert(o,
                        orderItemsRepository.findByOrder_Id(o.getId())))
                .toList();
    }

    public CartResponseDTO getCartItems(UUID userId) {

        List<OrderItems> items =
                orderItemsRepository.findByUser_IdAndOrderIsNull(userId);

        List<OrderItemsDTO> dtos = items.stream().map(this::convert).toList();

        double sub = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        double delivery = sub < 500 ? 100 : 0;

        return new CartResponseDTO(dtos, sub, delivery, sub + delivery);
    }

    // ================= ADMIN =================
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(o -> convert(o,
                        orderItemsRepository.findByOrder_Id(o.getId())))
                .toList();
    }

    public OrderResponseDTO dispatchOrder(Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        if (o.getOrderStatus() != OrderStatus.PLACED) {
            throw new BadRequestException("Order not ready for dispatch");
        }
        o.setOrderStatus(OrderStatus.DISPATCHED);
        Order saved = orderRepository.save(o);
        auditService.logAction(
                null,
                "SYSTEM",
                "ADMIN",
                "ORDER_DISPATCHED",
                "ORDER",
                String.valueOf(id),
                "Order dispatched"
        );
        try{
        notificationService.sendNotification(
                SendNotificationRequest.builder()
                        .receiverId(o.getUser().getId())
                        .title("Order Dispatched")
                        .category(Category.ORDER)
                        .notificationType(NotificationType.ORDER_DISPATCHED)
                        .message("Your order #" + id + " is dispatched")
                        .link("/orders")
                        .notificationChannel(NotificationChannel.BOTH)
                        .build()
        );
    } catch (Exception e) {
        log.error("Failed to send notification for orderId: {}",id, e);
    }
        return convert(saved,
                orderItemsRepository.findByOrder_Id(id));
    }

    public OrderResponseDTO deliverOrder(Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setOrderStatus(OrderStatus.DELIVERED);
        Order saved = orderRepository.save(o);

        auditService.logAction(
                null,
                "SYSTEM",
                "ADMIN",
                "ORDER_DELIVERED",
                "ORDER",
                String.valueOf(id),
                "Order delivered"
        );
try{
        notificationService.sendNotification(
                SendNotificationRequest.builder()
                        .receiverId(o.getUser().getId())
                        .title("Order Delivered")
                        .category(Category.ORDER)
                        .notificationType(NotificationType.ORDER_DELIVERED)
                        .message("Your order #" + id + " delivered")
                        .link("/orders")
                        .notificationChannel(NotificationChannel.BOTH)
                        .build()
        );
    } catch (Exception e) {
        log.error("Failed to send notification for orderId: {}",id, e);
    }
//        Order saved = orderRepository.save(o);

        // ✅ If COD → mark payment as SUCCESS on delivery
        if (saved.getPaymentMode() == PaymentMode.COD) {
            paymentRepository.findByOrderId(id).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
                auditService.logAction(
                        saved.getUser().getId(),
                        saved.getUser().getUsername(),
                        saved.getUser().getRole().name(),
                        "PAYMENT_SUCCESS",
                        "PAYMENT",
                        String.valueOf(id),
                        "COD payment completed"
                );
                log.info("COD Payment marked SUCCESS for orderId: {}", id);
            });
        }

        return convert(saved, orderItemsRepository.findByOrder_Id(id));
    }
//    public OrderResponseDTO cancelOrder(Long id, UUID userId) {
//
//        Order o = orderRepository.findById(id).orElseThrow();
//
//        if (!o.getUser().getId().equals(userId)) {
//            throw new UnauthorizedException("Unauthorized");
//        }
//
//        if (o.getOrderStatus() == OrderStatus.DISPATCHED ||
//                o.getOrderStatus() == OrderStatus.DELIVERED) {
//            throw new BadRequestException("Order cannot be cancelled now");
//        }
//
//        if (o.getOrderStatus() == OrderStatus.CANCELLED) {
//            throw new BadRequestException("Order already cancelled");
//        }
//
//        o.setOrderStatus(OrderStatus.CANCELLED);
//        o.setCancelledAt(LocalDateTime.now());
//        Order saved = orderRepository.save(o);
//        auditService.logAction(
//                saved.getUser().getId(),
//                saved.getUser().getUsername(),
//                saved.getUser().getRole().name(),
//                "PAYMENT_SUCCESS",
//                "PAYMENT",
//                saved.getId().toString(),
//                "COD payment completed"
//        );
//
//        return convert(saved,
//                orderItemsRepository.findByOrder_Id(id));
//    }
public OrderResponseDTO cancelOrder(Long id, UUID userId) {

    Order o = orderRepository.findById(id).orElseThrow();

    if (!o.getUser().getId().equals(userId)) {
        throw new UnauthorizedException("Unauthorized");
    }

    if (o.getOrderStatus() == OrderStatus.DISPATCHED ||
            o.getOrderStatus() == OrderStatus.DELIVERED) {
        throw new BadRequestException("Order cannot be cancelled now");
    }

    if (o.getOrderStatus() == OrderStatus.CANCELLED) {
        throw new BadRequestException("Order already cancelled");
    }

    // ✅ FETCH ITEMS
    List<OrderItems> items = orderItemsRepository.findByOrder_Id(id);

    // ✅ RESTORE STOCK
    for (OrderItems item : items) {
        Product product = item.getProduct();

        product.setStockQuantity(
                product.getStockQuantity() + item.getQuantity()
        );
        Product savedProduct = productRepository.save(product);

        try {
            ProductDocument doc = buildDocument(savedProduct);
            productsearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        try{
        notificationService.sendNotification(
                SendNotificationRequest.builder()
                        .receiverId(o.getUser().getId())
                        .title("Order Cancelled")
                        .category(Category.ORDER)
                        .notificationType(NotificationType.ORDER_CANCELLED)
                        .message("Your order #" + id + " Cancelled")
                        .link("/orders")
                        .notificationChannel(NotificationChannel.BOTH)
                        .build()
        );
    } catch (Exception e) {
        log.error("Failed to send notification for orderId: {}",id, e);
    }

    }

    // ✅ CANCEL ORDER
    o.setOrderStatus(OrderStatus.CANCELLED);
    o.setCancelledAt(LocalDateTime.now());

    Order saved = orderRepository.save(o);

    auditService.logAction(
            saved.getUser().getId(),
            saved.getUser().getUsername(),
            saved.getUser().getRole().name(),
            "ORDER_CANCELLED",
            "ORDER",
            String.valueOf(id),
            "Order cancelled and stock restored"
    );

    return convert(saved, items);
}

    public List<OrderResponseDTO> getRecentOrders(int days) {

        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);

        return orderRepository.findByCreatedAtAfter(fromDate)
                .stream()
                .map(o -> convert(o,
                        orderItemsRepository.findByOrder_Id(o.getId())))
                .toList();
    }

    public Page<OrderResponseDTO> getAllOrdersPaginated(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return orderRepository.findAll(pageable)
                .map(order -> convert(order,
                        orderItemsRepository.findByOrder_Id(order.getId())));
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public double getTodaySales() {

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Double total = orderRepository.getRevenueBetween(start, end);

        return total != null ? total : 0;
    }

    public double getMonthlySales() {

        LocalDateTime start = LocalDateTime.now()
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime end = start.plusMonths(1);

        Double total = orderRepository.getRevenueBetween(start, end);

        return total != null ? total : 0;
    }

    public double getYearlySales() {

        LocalDateTime start = LocalDateTime.now()
                .withDayOfYear(1)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime end = start.plusYears(1);

        Double total = orderRepository.getRevenueBetween(start, end);

        return total != null ? total : 0;
    }

    public List<SalesTrendDTO> getSalesTrend(int days) {

        LocalDateTime start = LocalDateTime.now().minusDays(days);

        return orderRepository.getSalesTrend(start)
                .stream()
                .map(obj -> new SalesTrendDTO(
                        (LocalDateTime) obj[0],
                        (Double) obj[1]
                ))
                .toList();
    }

    public List<TopProductDTO> getTopProducts(int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        return orderItemsRepository.getTopSellingProducts(pageable)
                .stream()
                .map(obj -> new TopProductDTO(
                        (Long) obj[0],
                        (String) obj[1],
                        (Long) obj[2]
                ))
                .toList();
    }

    public Map<String, Double> getRevenueByPaymentMode() {

        return orderRepository.getRevenueByPaymentMode()
                .stream()
                .collect(Collectors.toMap(
                        obj -> obj[0].toString(),
                        obj -> (Double) obj[1]
                ));
    }

    public List<TopCustomerDTO> getTopCustomers(int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        return orderRepository.getTopCustomers(pageable)
                .stream()
                .map(obj -> new TopCustomerDTO(
                        (UUID) obj[0],
                        (String) obj[1],
                        ((Number) obj[2]).doubleValue(),
                        ((Number) obj[3]).longValue()
                ))
                .toList();
    }

//    public List<Map<String, Object>> getTopCustomers(int limit) {
//
//        Pageable pageable = PageRequest.of(0, limit);
//
//        return orderRepository.getTopCustomers(pageable)
//                .stream()
//                .map(obj -> Map.of(
//                        "userId", obj[0],
//                        "username", obj[1],
//                        "totalSpent", obj[2]
//                ))
//                .toList();
//    }
    public Map<String, Long> getPaymentStatusStats() {

        return orderRepository.getPaymentStatusCounts()
                .stream()
                .collect(Collectors.toMap(
                        obj -> obj[0].toString(),
                        obj -> (Long) obj[1]
                ));
    }

    // ================= CONVERTERS =================
    private OrderItemsDTO convert(OrderItems i) {
        return new OrderItemsDTO(
                i.getId(),
                i.getProduct().getId(),
                i.getPname(),
                i.getActualPrice(),
                i.getDiscount(),
                i.getPrice(),
                i.getQuantity(),
                i.getActualPrice() * i.getQuantity(),
                i.getPrice() * i.getQuantity()
        );
    }

    private OrderResponseDTO convert(Order o, List<OrderItems> items) {

        List<OrderItemsDTO> list = items.stream().map(this::convert).toList();
        UserResponse userResponse = UserResponse.from(o.getUser());
        Address a = o.getAddress();
        AddressResponse addressResponse = new AddressResponse(
                a.getId(),
                a.getFullName(),
                a.getPhoneNumber(),
                a.getStreet(),
                a.getCity(),
                a.getState(),
                a.getPinCode(),
                a.getCountry(),
                a.isDefault(),
                a.getIsDeleted()
        );

        return new OrderResponseDTO(
                o.getId(),
                userResponse,
                addressResponse,
                list,
                o.getTotalAmount(),
                o.getDeliveryCost(),
                o.getTotalAmount() + o.getDeliveryCost(),
                o.getOrderStatus().name(),
                o.getPaymentMode().name(),
                o.getCreatedAt(),
                o.getCancelledAt()
        );
    }
    public ProductDocument buildDocument(Product product) {

        // 🔥 Build ProductDocument (exactly your structure)
        return ProductDocument.builder()
                .id(product.getId().toString())

                // 🔹 Basic Info
                .name(product.getName())
                .description(product.getDescription())
                .photoUrl(product.getPhotoUrl())
                .partNumber(product.getPartNumber())
                .company(product.getCompany())

                // 🔹 Pricing
                .actualPrice(product.getActualPrice())
                .discount(product.getDiscount())
                .price(product.getPrice())

                // 🔹 Inventory
                .stockQuantity(product.getStockQuantity())
                .rating(product.getRating())
                .isActive(product.getIsActive())

                // 🔹 Category Hierarchy
                .categoryId(product.getSubCategory().getCategory().getId().toString())
                .categoryName(product.getSubCategory().getCategory().getName())

                .subCategoryId(product.getSubCategory().getId().toString())
                .subCategoryName(product.getSubCategory().getName())

                // 🔹 Metadata
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())

                .build();
    }

}