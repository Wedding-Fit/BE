package com.weddingfit.controller.account;

import com.weddingfit.dto.request.account.AccountRegisterRequest;
import com.weddingfit.dto.response.account.AccountRegisterResponse;
import com.weddingfit.dto.response.account.CoupleAccountResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtAuthenticationUtil;
import com.weddingfit.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Account", description = "계좌 관리 API")
public class AccountController {
    
    private final AccountService accountService;
    private final JwtAuthenticationUtil jwtAuthenticationUtil;
    
    @Autowired
    public AccountController(AccountService accountService, JwtAuthenticationUtil jwtAuthenticationUtil) {
        this.accountService = accountService;
        this.jwtAuthenticationUtil = jwtAuthenticationUtil;
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "계좌 등록",
            description = "사용자의 은행 계좌를 등록합니다. 등록 후 백그라운드에서 CODEF API와 연동됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "계좌 등록 성공",
                    content = @Content(schema = @Schema(implementation = AccountRegisterResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 계좌번호입니다")
    })
    public BaseResponse<AccountRegisterResponse> registerAccount(
            @Valid @RequestBody AccountRegisterRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = jwtAuthenticationUtil.getCurrentUserId(httpRequest);
        
        try {
            AccountRegisterResponse response = accountService.registerAccount(userId, request);
            return BaseResponse.success(response, "계좌 등록에 성공했습니다");
        } catch (RuntimeException e) {
            return BaseResponse.error(401, "계좌 등록에 실패했습니다: " + e.getMessage());
        }
    }
    
    @GetMapping("/list")
    @Operation(
            summary = "내 계좌 목록 조회",
            description = "현재 사용자가 등록한 모든 계좌 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "계좌 목록 조회 성공"
            ),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public BaseResponse<List<AccountRegisterResponse>> getAccountList(HttpServletRequest httpRequest) {
        Long userId = jwtAuthenticationUtil.getCurrentUserId(httpRequest);
        
        List<AccountRegisterResponse> accounts = accountService.getUserAccounts(userId);
        return BaseResponse.success(accounts, "계좌 목록 조회에 성공했습니다");
    }
    
    @GetMapping("/{coupleId}")
    @Operation(
            summary = "커플 계좌 조회",
            description = "특정 커플의 연결된 계좌 정보를 조회합니다. 해당 커플의 멤버만 조회 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "계좌 조회 성공",
                    content = @Content(schema = @Schema(implementation = CoupleAccountResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "계좌 정보를 조회할 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 커플입니다")
    })
    public BaseResponse<CoupleAccountResponse> getCoupleAccounts(
            @PathVariable Long coupleId,
            HttpServletRequest httpRequest) {
        
        Long userId = jwtAuthenticationUtil.getCurrentUserId(httpRequest);
        
        try {
            CoupleAccountResponse response = accountService.getCoupleAccounts(userId, coupleId);
            return BaseResponse.success(response, "연결된 계좌 조회에 성공했습니다");
        } catch (SecurityException e) {
            return BaseResponse.error(403, "계좌 정보를 조회할 권한이 없습니다");
        } catch (IllegalArgumentException e) {
            return BaseResponse.error(404, "존재하지 않는 커플입니다");
        }
    }
}