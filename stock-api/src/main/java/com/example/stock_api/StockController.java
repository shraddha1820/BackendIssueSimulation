package com.example.stockapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// This is what is hit from the browser

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock/{symbol}")
    public String getStock(@PathVariable String symbol) {
        long start = System.currentTimeMillis();
        
        String price = stockService.getStockPrice(symbol);
        
        long end = System.currentTimeMillis();
        return price + " (Took " + (end - start) + "ms)";
    }
}