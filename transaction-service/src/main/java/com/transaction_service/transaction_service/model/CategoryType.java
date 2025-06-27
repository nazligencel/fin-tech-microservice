package com.transaction_service.transaction_service.model;

public enum CategoryType {
    SALARY,          // Maaş (Gelir)
    FREELANCE,       // Serbest Çalışma (Gelir)
    INVESTMENT,      // Yatırım (Gelir)
    GIFT,            // Hediye (Gelir)
    OTHER_INCOME,    // Diğer Gelir

    FOOD,            // Yemek (Gider)
    TRANSPORTATION,  // Ulaşım (Gider)
    HOUSING,         // Konut/Kira (Gider)
    BILLS,           // Faturalar (Gider)
    HEALTH,          // Sağlık (Gider)
    EDUCATION,       // Eğitim (Gider)
    ENTERTAINMENT,   // Eğlence (Gider)
    SHOPPING,        // Alışveriş (Gider)
    TRAVEL,          // Seyahat (Gider)
    OTHER_EXPENSE    // Diğer Gide
}
