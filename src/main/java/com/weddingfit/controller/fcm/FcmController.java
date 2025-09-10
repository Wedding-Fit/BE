package com.weddingfit.controller.fcm;

import com.weddingfit.dto.request.fcm.MessagePushServiceRequest;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/push")
    public BaseResponse<Void> push(@RequestBody MessagePushServiceRequest req) {
        fcmService.pushMessage(req);
        return BaseResponse.success(null, "성공했습니다");
    }

}
