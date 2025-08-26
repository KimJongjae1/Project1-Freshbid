package FreshBid.back.service;

import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.User;

public interface EmailService {

    void sendEmail(String to, String subject, String content);

    void sendOrderEmail(User user, Order order);
    
    void sendAuctionFailureEmail(User seller, Auction auction);
}
