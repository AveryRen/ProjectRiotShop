package com.example.riotshop.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {
    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }
}
