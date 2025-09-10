package com.weddingfit.controller.admin;

import com.weddingfit.entity.deposit.DepositSavingType;
import com.weddingfit.service.finlife.FinlifeSyncService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/finlife")
@RequiredArgsConstructor
public class FinlifeAdminController {

  private final FinlifeSyncService syncService;

  @Value("${external.finlife.endpoints.deposit:/depositProductsSearch.json}") private String depositPath;
  @Value("${external.finlife.endpoints.saving:/savingProductsSearch.json}") private String savingPath;
  @Value("${external.finlife.endpoints.personalLoan:/creditLoanProductsSearch.json}") private String personalPath;
  @Value("${external.finlife.endpoints.jeonseLoan:/rentHouseLoanProductsSearch.json}") private String jeonsePath;

  @PostMapping("/sync/deposit")
  public ResponseEntity<?> syncDeposit() {
    int cnt = syncService.syncDepositSaving(depositPath, DepositSavingType.DEPOSIT);
    return ResponseEntity.ok(Map.of("category","DEPOSIT","synced",cnt));
  }

  @PostMapping("/sync/saving")
  public ResponseEntity<?> syncSaving() {
    int cnt = syncService.syncDepositSaving(savingPath, DepositSavingType.SAVING);
    return ResponseEntity.ok(Map.of("category","SAVING","synced",cnt));
  }

  @PostMapping("/sync/personal")
  public ResponseEntity<?> syncPersonal() {
    int cnt = syncService.syncPersonalCredit(personalPath);
    return ResponseEntity.ok(Map.of("category","PERSONAL_CREDIT","synced",cnt));
  }

  @PostMapping("/sync/jeonse")
  public ResponseEntity<?> syncJeonse() {
    int cnt = syncService.syncJeonse(jeonsePath);
    return ResponseEntity.ok(Map.of("category","JEONSE_LOAN","synced",cnt));
  }

  @PostMapping("/sync/all")
  public ResponseEntity<?> syncAll() {
    int total = 0;
    total += syncService.syncDepositSaving(depositPath, DepositSavingType.DEPOSIT);
    total += syncService.syncDepositSaving(savingPath, DepositSavingType.SAVING);
    total += syncService.syncPersonalCredit(personalPath);
    total += syncService.syncJeonse(jeonsePath);
    return ResponseEntity.ok(Map.of("syncedTotal", total));
  }
}