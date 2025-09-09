package com.weddingfit.service.policies;

import com.weddingfit.dto.response.policies.PolicyListResponse;
import com.weddingfit.dto.response.policies.PolicyResponse;
import com.weddingfit.entity.policies.GovernmentPolicy;
import com.weddingfit.repository.policies.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {
    
    private final PolicyRepository policyRepository;
    
    public PolicyListResponse getAllPolicies() {
        List<GovernmentPolicy> policies = policyRepository.findAll();
        
        List<PolicyResponse> policyResponses = policies.stream()
                .map(PolicyResponse::from)
                .collect(Collectors.toList());
        
        return PolicyListResponse.from(policyResponses);
    }
}