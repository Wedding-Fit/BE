package com.weddingfit.controller.vendors;

import com.weddingfit.dto.response.vendors.VendorDetailResponse;
import com.weddingfit.dto.response.vendors.VendorListResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtProvider;
import com.weddingfit.service.vendors.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor", description = "웨딩업체 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class VendorController {
    
    private final VendorService vendorService;
    private final JwtProvider jwtProvider;
    
    @GetMapping
    @Operation(
            summary = "웨딩업체 목록 조회",
            description = "모든 웨딩업체 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업체 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public BaseResponse<VendorListResponse> getVendors(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        jwtProvider.getUserId(token);
        
        VendorListResponse response = vendorService.getAllVendors();
        
        return BaseResponse.success(response, "성공했습니다");
    }
    
    @GetMapping("/{vendorId}")
    @Operation(
            summary = "웨딩업체 상세 조회",
            description = "특정 웨딩업체의 상세 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업체 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "업체 정보 없음")
    })
    public BaseResponse<VendorDetailResponse> getVendorDetail(
            @Parameter(description = "업체 ID", required = true, example = "1")
            @PathVariable Long vendorId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        jwtProvider.getUserId(token);
        
        VendorDetailResponse response = vendorService.getVendorDetail(vendorId);
        
        return BaseResponse.success(response, "성공했습니다");
    }
    
    private String extractTokenFromHeader(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header format. Use 'Bearer <token>'");
        }
        return authorization.substring(7);
    }
}