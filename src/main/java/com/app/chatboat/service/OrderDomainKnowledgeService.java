package com.app.chatboat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 주문 도메인 지식 서비스
 * order-com-app의 비즈니스 로직을 기반으로 한 도메인 지식 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainKnowledgeService {
    
    /**
     * 주문 상태 플로우 정보
     */
    public static final Map<String, String> ORDER_STATUS_FLOW = Map.of(
        "CREATE", "주문생성 - 주문이 생성된 초기 상태",
        "WAIT", "결제대기 - 결제 처리를 기다리는 상태", 
        "DONE", "결제완료 - 결제가 성공적으로 완료된 상태",
        "FAIL", "결제실패 - 결제 처리에 실패한 상태",
        "CANCEL", "결제취소 - 주문이 취소된 상태"
    );
    
    /**
     * 에러 코드와 메시지 매핑
     */
    public static final Map<String, String> ERROR_CODES = Map.ofEntries(
        Map.entry("1000", "상품이 존재하지 않습니다"),
        Map.entry("1010", "상품이 매진되었습니다"), 
        Map.entry("1020", "수정할 상품이 존재하지 않습니다"),
        Map.entry("1030", "상품은 1개 이상 존재해야 합니다"),
        Map.entry("1040", "해당 상품은 이미 품절되었습니다"),
        Map.entry("1050", "상품 재고가 부족합니다"),
        Map.entry("1060", "중복된 주문번호입니다"),
        Map.entry("1070", "주문을 찾을 수 없습니다"),
        Map.entry("1080", "잘못된 주문 상태입니다"),
        Map.entry("1090", "결제를 찾을 수 없습니다"),
        Map.entry("1100", "결제 금액이 일치하지 않습니다"),
        Map.entry("1110", "이미 처리된 결제입니다"),
        Map.entry("2000", "회원을 찾을 수 없습니다")
    );
    
    /**
     * 주문 관련 비즈니스 규칙
     */
    public static final List<String> BUSINESS_RULES = List.of(
        "주문 생성 시 재고 확인 필수",
        "품절 상품(SOLDOUT_YN='Y')은 주문 불가",
        "재고 수량이 요청 수량보다 적으면 주문 불가",
        "주문 금액과 결제 금액이 일치해야 함",
        "주문 취소는 '생성' 또는 '결제대기' 상태에서만 가능",
        "결제 완료 후에는 취소 불가 (환불 프로세스 필요)",
        "주문번호는 중복되지 않도록 UUID + 타임스탬프로 생성",
        "재고 차감은 주문 생성 시점에 수행",
        "주문 취소 시 재고 복구 처리 필요"
    );
    
    /**
     * 주문 상태별 가능한 액션
     */
    public static final Map<String, List<String>> STATUS_ACTIONS = Map.of(
        "CREATE", List.of("결제 처리", "주문 취소"),
        "WAIT", List.of("결제 완료", "결제 실패", "주문 취소"),
        "DONE", List.of("주문 조회", "배송 처리"),
        "FAIL", List.of("재결제", "주문 취소"),
        "CANCEL", List.of("주문 조회")
    );
    
    /**
     * 사용자 질문에 따른 도메인 컨텍스트 생성
     */
    public String buildDomainContext(String userMessage) {
        log.info("주문 도메인 컨텍스트 생성 - 질문: {}", userMessage);
        
        var contextBuilder = new StringBuilder();
        
        // 주문 상태 관련 질문인지 확인
        if (containsOrderStatusKeywords(userMessage)) {
            contextBuilder.append(buildOrderStatusContext());
        }
        
        // 재고 관련 질문인지 확인
        if (containsStockKeywords(userMessage)) {
            contextBuilder.append(buildStockManagementContext());
        }
        
        // 결제 관련 질문인지 확인
        if (containsPaymentKeywords(userMessage)) {
            contextBuilder.append(buildPaymentContext());
        }
        
        // 장바구니 관련 질문인지 확인
        if (containsCartKeywords(userMessage)) {
            contextBuilder.append(buildCartContext());
        }
        
        // 에러 관련 질문인지 확인
        if (containsErrorKeywords(userMessage)) {
            contextBuilder.append(buildErrorContext());
        }
        
        return contextBuilder.toString();
    }
    
    /**
     * 주문 상태 관련 컨텍스트
     */
    private String buildOrderStatusContext() {
        return """
            
            [주문 상태 관리]
            주문 상태 플로우:
            """.concat(
                ORDER_STATUS_FLOW.entrySet().stream()
                    .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("")
            ).concat("""
            
            상태별 가능한 액션:
            """).concat(
                STATUS_ACTIONS.entrySet().stream()
                    .map(entry -> String.format("- %s: %s", entry.getKey(), String.join(", ", entry.getValue())))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("")
            );
    }
    
    /**
     * 재고 관리 관련 컨텍스트
     */
    private String buildStockManagementContext() {
        return """
            
            [재고 관리 규칙]
            - 품절 상품(SOLDOUT_YN='Y')은 주문 불가
            - 재고 수량이 요청 수량보다 적으면 주문 불가
            - 주문 생성 시 재고 자동 차감
            - 주문 취소 시 재고 자동 복구
            - 재고가 0이 되면 자동으로 품절 처리
            """;
    }
    
    /**
     * 결제 관련 컨텍스트
     */
    private String buildPaymentContext() {
        return """
            
            [결제 처리 규칙]
            - 주문 금액과 결제 금액이 일치해야 함
            - 결제 상태: 대기(PENDING) → 성공(SUCCESS) / 실패(FAIL)
            - PG사 응답 코드 및 메시지 처리
            - 결제 완료 후 주문 상태를 '결제완료'로 변경
            - 결제 실패 시 주문 상태를 '결제실패'로 변경
            """;
    }
    
    /**
     * 장바구니 관련 컨텍스트
     */
    private String buildCartContext() {
        return """
            
            [장바구니 관리]
            - 아이템 추가/수정/삭제/선택 가능
            - 수량 변경 시 총 가격 자동 계산
            - 선택된 아이템만 주문 가능
            - 기존 아이템 추가 시 수량 증가
            """;
    }
    
    /**
     * 에러 처리 관련 컨텍스트
     */
    private String buildErrorContext() {
        return """
            
            [에러 코드 및 해결 방법]
            """.concat(
                ERROR_CODES.entrySet().stream()
                    .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("")
            );
    }
    
    /**
     * 주문 상태 관련 키워드 확인
     */
    private boolean containsOrderStatusKeywords(String message) {
        var keywords = List.of("주문", "상태", "생성", "결제", "완료", "실패", "취소", "order", "status");
        return keywords.stream().anyMatch(message::contains);
    }
    
    /**
     * 재고 관련 키워드 확인
     */
    private boolean containsStockKeywords(String message) {
        var keywords = List.of("재고", "품절", "수량", "stock", "soldout", "quantity");
        return keywords.stream().anyMatch(message::contains);
    }
    
    /**
     * 결제 관련 키워드 확인
     */
    private boolean containsPaymentKeywords(String message) {
        var keywords = List.of("결제", "금액", "payment", "amount", "pg");
        return keywords.stream().anyMatch(message::contains);
    }
    
    /**
     * 장바구니 관련 키워드 확인
     */
    private boolean containsCartKeywords(String message) {
        var keywords = List.of("장바구니", "카트", "cart", "바구니");
        return keywords.stream().anyMatch(message::contains);
    }
    
    /**
     * 에러 관련 키워드 확인
     */
    private boolean containsErrorKeywords(String message) {
        var keywords = List.of("에러", "오류", "실패", "error", "fail", "exception");
        return keywords.stream().anyMatch(message::contains);
    }
}
