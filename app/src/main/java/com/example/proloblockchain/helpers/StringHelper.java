package com.example.proloblockchain.helpers;

public class StringHelper {

    public static boolean regexEmailValidationPattern(String email) {

        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

        if (email.matches(regex)) {
            return true;
        }
        return false;
    }

}
