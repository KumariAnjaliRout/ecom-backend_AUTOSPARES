package com.automobile.ecom.controller;

import com.automobile.ecom.dto.GlobalSearchResponse;
import com.automobile.ecom.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    // ✅ GLOBAL SEARCH ENDPOINT
    @GetMapping
    public ResponseEntity<GlobalSearchResponse> search(
            @RequestParam(required = false) String keyword
    ) throws IOException {

        GlobalSearchResponse response = globalSearchService.search(keyword);

        return ResponseEntity.ok(response);
    }
}