package com.voiceide.controller;

import com.voiceide.dto.CodeExecutionRequest;
import com.voiceide.dto.CodeExecutionResult;
import com.voiceide.service.CodeExecutionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResult> executeCode(@Valid @RequestBody CodeExecutionRequest request) {
        return ResponseEntity.ok(codeExecutionService.execute(request));
    }
}
