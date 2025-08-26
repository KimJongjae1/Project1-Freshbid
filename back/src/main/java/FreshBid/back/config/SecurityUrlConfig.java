package FreshBid.back.config;

import org.springframework.stereotype.Component;

@Component
public class SecurityUrlConfig {

    public static final String[] PUBLIC_URLS = {
        "/index.html",          // Index.html
        "/robots.txt",          // robots.txt
        "/.well-known/**",      // SSL 인증
        "/swagger-ui/**",       // Swagger UI
        "/v3/api-docs/**",      // Swagger UI
        "/call/**",             // WebSocket
        "/auth/**",             // AuthController (사용자 인증 API)
        "/seller-info/**",      // SellerInfoController (판매자 정보 API)
        "/test-image/**"        // Test용 API
    };

    // GET ONLY
    public static final String[] PUBLIC_READ_URLS = {
        "/auction/live/**",     // LiveController (Live CRUD API)
        "/auction/review/**",   // SellerReviewController (판매자 후기 API)
        "/auction/qna/**",      // SellerQnaController (판매자 문의 API)
        "/categories/**",       // CategoryController (카테고리 관리 API)
        "/auction/**",          // TODO : GET /auction 전체 허용하므로, 추후 엔드포인트 분리 필요
        "/price/**",
    };

    public static final String[] AUTHENTICATED_URLS = {
        "/util/**",             // UtilController (기타 도구 API)
        "/auction/review/**",   // SellerReviewController (판매자 후기 API)
        "/orders/**",           // OrderController (주문 관리 API)
        "/auction/qna/**",      // SellerQnaController (판매자 문의 API)
        "/my-page/**",          // UserController (마이페이지 관련 API)
        "/cart/**",             // CartController (장바구니 관련 API)
        "/bookmark/**",          // BookmarkController (찜 관련 API)
    };

    // GET ONLY
    public static final String[] AUTHENTICATED_READ_URLS = {

    };

    public static final String[] SELLER_URLS = {
        "/auction/live/**",     // LiveController (Live CRUD API)
        "/auction/product/**",  // ProductController (상품 관리 API)
        "/auction/**",          // AuctionController (Auction CRUD API)
    };

    // GET ONLY
    public static final String[] SELLER_READ_URLS = {

    };

    public static final String[] ADMIN_URLS = {
        "/categories/**",       // 카테고리 관리 API
    };
}
