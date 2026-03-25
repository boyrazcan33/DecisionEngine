package com.inbank.decisionengine.controller;

import com.inbank.decisionengine.model.LoanRequest;
import com.inbank.decisionengine.model.LoanResponse;
import com.inbank.decisionengine.service.DecisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
@CrossOrigin(origins = "*")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/decision")
    public ResponseEntity<LoanResponse> getDecision(@RequestBody LoanRequest request) {
        LoanResponse response = decisionService.decide(request);
        return ResponseEntity.ok(response);
    }
}