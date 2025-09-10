package com.weddingfit.controller.fcm;

import com.weddingfit.dto.request.fcm.FcmTokenRegisterRequest;
import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import com.weddingfit.dto.response.fcm.FcmTokenResponse;
import com.weddingfit.entity.user.User;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.fcm.FcmService;
import com.weddingfit.service.fcm.FcmTokenService;
import com.weddingfit.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmService fcmService;
    private final FcmTokenService fcmTokenService;
    private final UserService userService;

    @PostMapping("/tokens")
    public BaseResponse<FcmTokenResponse> registerToken(@RequestBody FcmTokenRegisterRequest request) {
        User user = getCurrentUser();
        FcmTokenResponse response = fcmTokenService.registerToken(user, request);
        return BaseResponse.success(response, "FCM 토큰이 등록되었습니다");
    }

    @GetMapping("/tokens")
    public BaseResponse<List<FcmTokenResponse>> getTokens() {
        User user = getCurrentUser();
        List<FcmTokenResponse> tokens = fcmTokenService.getActiveTokensByUser(user);
        return BaseResponse.success(tokens, "FCM 토큰 조회가 완료되었습니다");
    }

    @DeleteMapping("/tokens/{tokenId}")
    public BaseResponse<Void> deactivateToken(@PathVariable Long tokenId) {
        User user = getCurrentUser();
        fcmTokenService.deactivateToken(user, tokenId);
        return BaseResponse.success(null, "FCM 토큰이 비활성화되었습니다");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }

    @PostMapping("/push")
    public BaseResponse<Void> push(@RequestBody MessagePushServiceRequest req) {
        fcmService.pushMessage(req);
        return BaseResponse.success(null, "성공했습니다");
    }
}
