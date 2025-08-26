package FreshBid.back.service.impl;

import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import FreshBid.back.entity.User;
import FreshBid.back.entity.User.Role;
import FreshBid.back.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final String PREFIX = "[FreshBid] ";

    @Override
    public void sendEmail(String to, String subject, String content) {
        if (!isValidEmail(to)) {
            log.error("Email Sent Failed To : {} - Invalid email address format", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(PREFIX + subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email Sent Successfully To : {}", to);
        } catch (Exception e) {
            log.error("Email Sent Failed To : {}. Error: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendOrderEmail(User user, Order order) {
        if (user == null) {
            log.error("주문 이메일 발송 실패 - User가 null입니다");
            return;
        }

        if (order == null) {
            log.error("주문 이메일 발송 실패 - Order가 null입니다 - User ID: {}", user.getId());
            return;
        }

        log.debug("주문 이메일 발송 시작 - User ID: {}, Order ID: {}, Status: {}",
            user.getId(), order.getId(), order.getStatus());

        String templateName = getEmailTemplate(order.getStatus());
        log.debug("이메일 템플릿 선택 - Template: {}, UserRole: {}", templateName, user.getRole().name());

        String subject = getSubjectFromTemplate(templateName, user.getRole());
        String content = getContentFromTemplate(templateName, user.getRole(), user, order);
        sendEmail(user.getEmail(), subject, content);
    }

    @Override
    public void sendAuctionFailureEmail(User seller, Auction auction) {
        if (seller == null) {
            log.error("유찰 이메일 발송 실패 - Seller가 null입니다");
            return;
        }

        if (auction == null) {
            log.error("유찰 이메일 발송 실패 - Auction이 null입니다 - Seller ID: {}", seller.getId());
            return;
        }

        log.debug("유찰 이메일 발송 시작 - Seller ID: {}, Auction ID: {}", 
            seller.getId(), auction.getId());

        String templateName = "order-failure";
        String subject = getSubjectFromTemplate(templateName, seller.getRole());
        String content = getAuctionFailureContent(seller, auction);
        
        sendEmail(seller.getEmail(), subject, content);
    }

    private String getEmailTemplate(OrderStatus status) {
        return switch (status) {
            case WAITING -> "order-created";
            case CANCELLED -> "order-cancelled";
            case REFUNDED -> "order-refunded";
            case SHIPPED -> "order-shipped";
            case COMPLETED -> "order-completed";
            default -> "order-status-changed";
        };
    }

    private String getSubjectFromTemplate(String templateName, Role userRole) {
        String baseSubject = switch (templateName) {
            case "order-created" -> "주문이 생성되었습니다";
            case "order-cancelled" -> "주문이 취소되었습니다";
            case "order-refunded" -> "환불이 완료되었습니다";
            case "order-shipped" -> "상품이 발송되었습니다";
            case "order-completed" -> "주문이 완료되었습니다";
            case "order-failure" -> "경매가 유찰되었습니다";
            default -> "주문 상태가 변경되었습니다";
        };

        String rolePrefix = Role.ROLE_CUSTOMER.equals(userRole) ? "구매자" : "판매자";
        return rolePrefix + "님, " + baseSubject;
    }

    private String getContentFromTemplate(String templateName, Role userRole, User user,
        Order order) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("userRole", userRole);
            context.setVariable("isCustomer", Role.ROLE_CUSTOMER.equals(userRole));

            return templateEngine.process("email/bid/" + templateName, context);
        } catch (Exception e) {
            log.error("템플릿 처리 실패 - Template: {}", templateName, e);
            throw new RuntimeException("템플릿 처리 실패: " + templateName, e);
        }
    }

    private String getAuctionFailureContent(User seller, Auction auction) {
        try {
            Context context = new Context();
            context.setVariable("user", seller);
            context.setVariable("auction", auction);
            context.setVariable("userRole", seller.getRole());
            context.setVariable("isCustomer", false);

            return templateEngine.process("email/bid/order-failure", context);
        } catch (Exception e) {
            log.error("유찰 템플릿 처리 실패 - Auction ID: {}", auction.getId(), e);
            throw new RuntimeException("유찰 템플릿 처리 실패: " + auction.getId(), e);
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // 기본적인 이메일 형식 검증
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}
