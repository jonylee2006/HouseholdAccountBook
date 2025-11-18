package com.lazyledger.classification.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class ClassificationDictionary {

    private final Map<String, String> merchantCategoryMap = new LinkedHashMap<>();

    public ClassificationDictionary() {
        merchantCategoryMap.put("饿了么", "餐饮");
        merchantCategoryMap.put("美团", "餐饮");
        merchantCategoryMap.put("盒马", "食材");
        merchantCategoryMap.put("永辉", "食材");
        merchantCategoryMap.put("沃尔玛", "日用百货");
        merchantCategoryMap.put("星巴克", "咖啡");
        merchantCategoryMap.put("瑞幸", "咖啡");
        merchantCategoryMap.put("滴滴", "交通");
        merchantCategoryMap.put("高德打车", "交通");
        merchantCategoryMap.put("同程", "出行");
        merchantCategoryMap.put("京东", "网购");
        merchantCategoryMap.put("天猫", "网购");
        merchantCategoryMap.put("顺丰", "快递");
        merchantCategoryMap.put("移动", "通讯");
        merchantCategoryMap.put("联通", "通讯");
    }

    public Optional<String> match(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            return Optional.empty();
        }
        String lower = merchantName.toLowerCase(Locale.ROOT);
        return merchantCategoryMap.entrySet().stream()
                .filter(entry -> lower.contains(entry.getKey().toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
