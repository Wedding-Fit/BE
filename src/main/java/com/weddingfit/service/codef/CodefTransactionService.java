package com.weddingfit.service.codef;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weddingfit.entity.account.Account;
import com.weddingfit.entity.transaction.Transaction;
import com.weddingfit.repository.account.AccountRepository;
import com.weddingfit.repository.transaction.TransactionRepository;
import com.weddingfit.repository.couple.CoupleRepository;
import com.weddingfit.service.couple.CoupleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefTransactionService {
    
    private final CodefApiService codefApiService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final CoupleRepository coupleRepository;
    private final CoupleService coupleService;
    
    // 은행명 -> 은행코드 매핑 (CodefConnectionService와 동일)
    private static final Map<String, String> BANK_CODE_MAP = new HashMap<>();
    
    static {
        BANK_CODE_MAP.put("국민은행", "0004");
        BANK_CODE_MAP.put("신한은행", "0088");
        BANK_CODE_MAP.put("우리은행", "0020");
        BANK_CODE_MAP.put("하나은행", "0081");
        BANK_CODE_MAP.put("농협은행", "0011");
        BANK_CODE_MAP.put("기업은행", "0003");
        BANK_CODE_MAP.put("SC제일은행", "0023");
        BANK_CODE_MAP.put("씨티은행", "0027");
        BANK_CODE_MAP.put("대구은행", "0031");
        BANK_CODE_MAP.put("부산은행", "0032");
        BANK_CODE_MAP.put("광주은행", "0034");
        BANK_CODE_MAP.put("제주은행", "0035");
        BANK_CODE_MAP.put("전북은행", "0037");
        BANK_CODE_MAP.put("경남은행", "0039");
        BANK_CODE_MAP.put("새마을금고", "0045");
        BANK_CODE_MAP.put("신협", "0048");
        BANK_CODE_MAP.put("우체국", "0071");
        BANK_CODE_MAP.put("KEB하나은행", "0081");
        BANK_CODE_MAP.put("카카오뱅크", "0090");
        BANK_CODE_MAP.put("케이뱅크", "0089");
        BANK_CODE_MAP.put("토스뱅크", "0092");
    }
    
    /**
     * 연결된 계좌의 거래내역을 동기화 (기존 메서드 유지)
     */
    @Async
    @Transactional
    public void syncAccountTransactions(Long accountId) {
        syncAccountTransactionsAndBalance(accountId);
    }
    
    /**
     * 연결된 계좌의 거래내역을 동기화하고 잔액 업데이트
     */
    @Async
    @Transactional
    public void syncAccountTransactionsAndBalance(Long accountId) {
        log.info("거래내역 동기화 시작: accountId={}", accountId);
        
        try {
            // 1. 계좌 조회
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));
            
            if (!account.isConnected()) {
                log.warn("연결되지 않은 계좌입니다: accountId={}", accountId);
                return;
            }
            
            // 2. 최근 30일 거래내역 조회
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            // 은행코드 매핑 (CodefConnectionService와 동일한 매핑 사용)
            String bankCode = getBankCode(account.getBankName());
            if (bankCode == null) {
                log.error("지원하지 않는 은행입니다: {}", account.getBankName());
                return;
            }
            
            CodefApiService.CodefTransactionResponse response = codefApiService
                    .getTransactionHistory(
                    account.getConnectedId(),
                    account.getAccountNumber(), // 실제 계좌번호 전달
                    bankCode, // 은행코드 전달
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            );
            
            if (response == null || response.getResult() == null) {
                log.warn("거래내역 조회 응답이 없습니다: accountId={}", accountId);
                return;
            }
            
            if (!"CF-00000".equals(response.getResult().getCode())) {
                log.error("거래내역 조회 실패: accountId={}, error={}", 
                         accountId, response.getResult().getMessage());
                return;
            }
            
            // 3. 계좌 잔액 업데이트
            updateAccountBalance(account, response);
            
            // 4. 거래내역 파싱 및 저장
            List<Transaction> transactions = parseAndCreateTransactions(account, response);
            
            // 5. 중복 체크 후 저장 (성능 최적화)
            List<Transaction> existingTransactions = transactionRepository.findByAccount(account);
            List<Transaction> newTransactions = new ArrayList<>();
            
            for (Transaction transaction : transactions) {
                boolean exists = existingTransactions.stream()
                        .anyMatch(t -> isSameTransaction(t, transaction));
                
                if (!exists) {
                    newTransactions.add(transaction);
                }
            }
            
            if (!newTransactions.isEmpty()) {
                transactionRepository.saveAll(newTransactions);
            }
            
            int savedCount = newTransactions.size();
            
            log.info("거래내역 동기화 완료: accountId={}, 총 거래건수={}, 저장건수={}", 
                     accountId, transactions.size(), savedCount);
            
        } catch (Exception e) {
            log.error("거래내역 동기화 중 오류 발생: accountId={}", accountId, e);
        }
    }
    
    /**
     * CODEF 응답에서 계좌 잔액을 추출하여 Account 엔티티 업데이트
     */
    private void updateAccountBalance(Account account, CodefApiService.CodefTransactionResponse response) {
        try {
            CodefApiService.CodefTransactionResponse.TransactionData data = response.getData();
            if (data != null && data.getResAccountBalance() != null) {
                String balanceStr = data.getResAccountBalance().replaceAll("[^0-9.-]", "");
                if (!balanceStr.isEmpty()) {
                    BigDecimal balance = new BigDecimal(balanceStr);
                    account.setBalance(balance);
                    accountRepository.save(account);
                    
                    log.info("계좌 잔액 업데이트 완료: accountId={}, balance={}", 
                             account.getAccountId(), balance);
                    
                    // 커플 총 자산 업데이트
                    updateCoupleTotalAmount(account);
                }
            } else {
                log.warn("CODEF 응답에 잔액 정보가 없습니다: accountId={}", account.getAccountId());
            }
        } catch (Exception e) {
            log.error("계좌 잔액 업데이트 중 오류: accountId={}", account.getAccountId(), e);
        }
    }
    
    /**
     * CODEF 응답을 Transaction 엔티티 리스트로 변환
     */
    private List<Transaction> parseAndCreateTransactions(Account account, CodefApiService.CodefTransactionResponse response) {
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            CodefApiService.CodefTransactionResponse.TransactionData data = response.getData();
            if (data != null && data.getResTrHistoryList() != null) {
                for (Object trObj : data.getResTrHistoryList()) {
                    // Object를 JsonNode로 변환
                    JsonNode trNode = objectMapper.valueToTree(trObj);
                    Transaction transaction = createTransactionFromJson(account, trNode);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("거래내역 파싱 중 오류: accountId={}", account.getAccountId(), e);
        }
        
        return transactions;
    }
    
    /**
     * JSON 노드에서 Transaction 엔티티 생성
     */
    private Transaction createTransactionFromJson(Account account, JsonNode trNode) {
        try {
            // CODEF 실제 응답 필드명 사용
            String dateStr = trNode.get("resAccountTrDate").asText(); // 거래일자
            String timeStr = trNode.get("resAccountTrTime") != null ? trNode.get("resAccountTrTime").asText() : ""; // 거래시간
            String baseDescription = trNode.get("resAccountDesc3").asText(); // 거래처명
            String balanceAfterStr = trNode.get("resAfterTranBalance").asText().replaceAll("[^0-9.-]", ""); // 거래후잔액
            
            // 거래시간을 description에 포함 (선택적)
            String description = timeStr.isEmpty() ? baseDescription : baseDescription + " (" + timeStr + ")";
            
            // 출금/입금 금액 확인
            String outAmountStr = trNode.get("resAccountOut") != null ? trNode.get("resAccountOut").asText() : "0";
            String inAmountStr = trNode.get("resAccountIn") != null ? trNode.get("resAccountIn").asText() : "0";
            
            // 실제 거래금액 (출금 또는 입금)
            String transactionAmountStr;
            boolean isExpense;
            
            if (!"0".equals(outAmountStr)) {
                transactionAmountStr = outAmountStr; // 출금
                isExpense = true;
            } else {
                transactionAmountStr = inAmountStr; // 입금
                isExpense = false;
            }
            
            // 날짜 파싱
            Date transactionDate = Date.valueOf(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            // 금액 파싱
            BigDecimal amount = new BigDecimal(transactionAmountStr.replaceAll("[^0-9.-]", ""));
            BigDecimal balanceAfter = new BigDecimal(balanceAfterStr);
            
            // Transaction 생성
            Transaction transaction = new Transaction(account, transactionDate, description, balanceAfter);
            transaction.setAmount(amount);
            transaction.setTransactionTime(timeStr); // 거래시간 설정
            
            // 입출금 구분
            if (isExpense) {
                transaction.setTransactionType(Transaction.TransactionType.EXPENSE);
            } else {
                transaction.setTransactionType(Transaction.TransactionType.INCOME);
            }
            
            // 카테고리 자동 분류 (간단한 키워드 기반)
            transaction.setCategory(categorizeTransaction(description));
            
            return transaction;
            
        } catch (Exception e) {
            log.error("Transaction 생성 중 오류: {}", trNode, e);
            return null;
        }
    }
    
    /**
     * 거래 내역 설명을 바탕으로 카테고리 자동 분류
     */
    private Transaction.TransactionCategory categorizeTransaction(String description) {
        if (description == null) return Transaction.TransactionCategory.ETC;
        
        String desc = description.toLowerCase();
        
        // 식비 - 음식점, 배달, 카페, 프랜차이즈
        if (desc.contains("롯데리아") || desc.contains("맥도날드") || desc.contains("버거킹") || desc.contains("kfc") ||
            desc.contains("파리크라상") || desc.contains("크라상") || desc.contains("파리바게뜨") ||
            desc.contains("뚜레쥬르") || desc.contains("던킨") || desc.contains("스타벅스") || desc.contains("카페") ||
            desc.contains("쿠팡이츠") || desc.contains("배달의민족") || desc.contains("요기요") || desc.contains("배달") ||
            desc.contains("푸드") || desc.contains("food") || desc.contains("치킨") || desc.contains("피자") ||
            desc.contains("식당") || desc.contains("레스토랑") || desc.contains("음식") || desc.contains("restaurant") ||
            desc.contains("베이커리") || desc.contains("빵집") || desc.contains("떡집") || desc.contains("분식") ||
            desc.contains("햄버거") || desc.contains("샌드위치") || desc.contains("김밥") || desc.contains("도시락") ||
            desc.contains("커피") || desc.contains("coffee") || desc.contains("tea") || desc.contains("음료") ||
            desc.contains("술집") || desc.contains("bar") || desc.contains("pub") || desc.contains("호프") ||
            desc.contains("풀무원") || desc.contains("cj") || desc.contains("오뚜기")) {
            return Transaction.TransactionCategory.FOOD;
        }
        
        // 교통비 - 지하철, 버스, 택시, 주유소
        else if (desc.contains("지하철") || desc.contains("subway") || desc.contains("metro") ||
                 desc.contains("버스") || desc.contains("bus") || desc.contains("시내버스") || desc.contains("광역버스") ||
                 desc.contains("택시") || desc.contains("taxi") || desc.contains("카카오택시") || desc.contains("타다") ||
                 desc.contains("주유") || desc.contains("gs칼텍스") || desc.contains("sk에너지") || desc.contains("s-oil") ||
                 desc.contains("현대오일뱅크") || desc.contains("lpg") || desc.contains("충전소") ||
                 desc.contains("교통카드") || desc.contains("티머니") || desc.contains("하나로카드") ||
                 desc.contains("고속도로") || desc.contains("톨게이트") || desc.contains("하이패스") ||
                 desc.contains("주차") || desc.contains("parking") || desc.contains("렌터카") || desc.contains("카셰어링")) {
            return Transaction.TransactionCategory.TRANSPORT;
        }
        
        // 의료비 - 병원, 약국, 의료기관
        else if (desc.contains("병원") || desc.contains("hospital") || desc.contains("의원") || desc.contains("clinic") ||
                 desc.contains("약국") || desc.contains("pharmacy") || desc.contains("한의원") || desc.contains("치과") ||
                 desc.contains("안과") || desc.contains("피부과") || desc.contains("내과") || desc.contains("외과") ||
                 desc.contains("정형외과") || desc.contains("산부인과") || desc.contains("소아과") ||
                 desc.contains("의료") || desc.contains("medical") || desc.contains("헬스케어") ||
                 desc.contains("건강검진") || desc.contains("mri") || desc.contains("ct") || desc.contains("엑스레이")) {
            return Transaction.TransactionCategory.MEDICAL;
        }
        
        // 문화생활 - 영화, 공연, 도서, 엔터테인먼트
        else if (desc.contains("영화관") || desc.contains("cgv") || desc.contains("롯데시네마") || desc.contains("메가박스") ||
                 desc.contains("영화") || desc.contains("cinema") || desc.contains("movie") ||
                 desc.contains("공연") || desc.contains("콘서트") || desc.contains("뮤지컬") || desc.contains("연극") ||
                 desc.contains("전시") || desc.contains("박물관") || desc.contains("미술관") || desc.contains("갤러리") ||
                 desc.contains("도서관") || desc.contains("서점") || desc.contains("교보문고") || desc.contains("영풍문고") ||
                 desc.contains("알라딘") || desc.contains("예스24") || desc.contains("도서") || desc.contains("book") ||
                 desc.contains("노래방") || desc.contains("ktv") || desc.contains("karaoke") ||
                 desc.contains("pc방") || desc.contains("게임") || desc.contains("오락실") ||
                 desc.contains("스포츠") || desc.contains("헬스장") || desc.contains("피트니스") || desc.contains("gym") ||
                 desc.contains("수영장") || desc.contains("골프") || desc.contains("볼링") || desc.contains("당구") ||
                 desc.contains("문화") || desc.contains("culture") || desc.contains("엔터테인먼트")) {
            return Transaction.TransactionCategory.CULTURE;
        }
        
        // 쇼핑 - 마트, 백화점, 온라인쇼핑, 의류, 잡화
        else if (desc.contains("마트") || desc.contains("mart") || desc.contains("이마트") || desc.contains("홈플러스") ||
                 desc.contains("롯데마트") || desc.contains("하나로마트") || desc.contains("코스트코") ||
                 desc.contains("편의점") || desc.contains("gs25") || desc.contains("cu") || desc.contains("세븐일레븐") ||
                 desc.contains("미니스톱") || desc.contains("이마트24") ||
                 desc.contains("백화점") || desc.contains("롯데백화점") || desc.contains("신세계") || desc.contains("현대백화점") ||
                 desc.contains("쿠팡") || desc.contains("11번가") || desc.contains("gmarket") || desc.contains("옥션") ||
                 desc.contains("위메프") || desc.contains("티몬") || desc.contains("네이버쇼핑") ||
                 desc.contains("올리브영") || desc.contains("아모레") || desc.contains("화장품") || desc.contains("cosmetic") ||
                 desc.contains("의류") || desc.contains("패션") || desc.contains("유니클로") || desc.contains("자라") ||
                 desc.contains("h&m") || desc.contains("무신사") || desc.contains("29cm") ||
                 desc.contains("다이소") || desc.contains("아이소이") || desc.contains("잡화") ||
                 desc.contains("스토어") || desc.contains("store") || desc.contains("shop") || desc.contains("쇼핑") ||
                 desc.contains("아울렛") || desc.contains("outlet") || desc.contains("프리미엄아울렛")) {
            return Transaction.TransactionCategory.SHOPPING;
        }
        
        // 교육비 - 학원, 교육기관, 온라인강의
        else if (desc.contains("학원") || desc.contains("academy") || desc.contains("교습소") ||
                 desc.contains("과외") || desc.contains("튜터") || desc.contains("tutor") ||
                 desc.contains("어학원") || desc.contains("토익") || desc.contains("토플") || desc.contains("영어") ||
                 desc.contains("학교") || desc.contains("대학") || desc.contains("university") || desc.contains("college") ||
                 desc.contains("교육") || desc.contains("education") || desc.contains("강의") || desc.contains("lecture") ||
                 desc.contains("인프런") || desc.contains("유데미") || desc.contains("패스트캠퍼스") ||
                 desc.contains("coursera") || desc.contains("udemy") ||
                 desc.contains("자격증") || desc.contains("시험") || desc.contains("exam") || desc.contains("certificate") ||
                 desc.contains("학습") || desc.contains("study") || desc.contains("연수") || desc.contains("세미나")) {
            return Transaction.TransactionCategory.EDU;
        }
        
        // 통신비 - 휴대폰, 인터넷, 케이블TV
        else if (desc.contains("skt") || desc.contains("kt") || desc.contains("lg") || desc.contains("uplus") ||
                 desc.contains("통신") || desc.contains("telecom") || desc.contains("mobile") ||
                 desc.contains("휴대폰") || desc.contains("핸드폰") || desc.contains("스마트폰") || desc.contains("phone") ||
                 desc.contains("인터넷") || desc.contains("internet") || desc.contains("wifi") || desc.contains("broadband") ||
                 desc.contains("케이블") || desc.contains("cable") || desc.contains("iptv") ||
                 desc.contains("넷플릭스") || desc.contains("netflix") || desc.contains("디즈니") || desc.contains("disney") ||
                 desc.contains("유튜브") || desc.contains("youtube") || desc.contains("프리미엄") ||
                 desc.contains("스포티파이") || desc.contains("spotify") || desc.contains("멜론") || desc.contains("flo") ||
                 desc.contains("데이터") || desc.contains("data") || desc.contains("요금제")) {
            return Transaction.TransactionCategory.TELECOM;
        }
        
        else {
            return Transaction.TransactionCategory.ETC;
        }
    }
    
    /**
     * 같은 거래내역인지 확인 (중복 체크용)
     */
    private boolean isSameTransaction(Transaction existing, Transaction newTransaction) {
        return existing.getAccount().getAccountId().equals(newTransaction.getAccount().getAccountId()) &&
               existing.getTransactionDate().equals(newTransaction.getTransactionDate()) &&
               existing.getAmount().compareTo(newTransaction.getAmount()) == 0 &&
               existing.getDescription().equals(newTransaction.getDescription());
    }
    
    /**
     * 모든 연결된 계좌의 거래내역 동기화 (스케줄러에서 사용)
     */
    @Transactional
    public void syncAllConnectedAccounts() {
        log.info("모든 연결된 계좌 거래내역 동기화 시작");
        
        List<Account> connectedAccounts = accountRepository.findByConnectedIdIsNotNull();
        log.info("동기화 대상 계좌 수: {}", connectedAccounts.size());
        
        for (Account account : connectedAccounts) {
            syncAccountTransactionsAndBalance(account.getAccountId());
        }
    }
    
    /**
     * 은행명으로 은행코드 조회
     */
    private String getBankCode(String bankName) {
        return BANK_CODE_MAP.get(bankName);
    }
    
    /**
     * 계좌 업데이트 후 해당 사용자가 속한 커플의 총 자산 업데이트
     */
    private void updateCoupleTotalAmount(Account account) {
        try {
            coupleRepository.findByUserId(account.getUser().getId())
                    .ifPresent(couple -> {
                        try {
                            coupleService.updateCoupleTotalAmount(couple.getId());
                            log.info("커플 총 자산 업데이트 완료: coupleId={}", couple.getId());
                        } catch (Exception e) {
                            log.error("커플 총 자산 업데이트 실패: coupleId={}, error={}", 
                                     couple.getId(), e.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("커플 조회 및 총 자산 업데이트 중 오류: userId={}, error={}", 
                     account.getUser().getId(), e.getMessage());
        }
    }
}