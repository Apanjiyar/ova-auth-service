package com.ms.authservice.util;

public class ApplicationUtil {

    /**
     * A Valid Phone number String must be like +91-9599135426
     * i.e [country code]-[phone number]
     * Country code: 1-3 digits, Phone number: 10 digits
     * @param phoneNumber
     * @return
     */
    public static boolean validatePhoneNumber(String phoneNumber){
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        // Regex: starts with +, followed by 1-3 digits, then -, then exactly 10 digits
        return phoneNumber.matches("^\\+[0-9]{1,3}-[0-9]{10}$");
    }
}
