package com.echonymous.constants;

import org.springframework.stereotype.Component;

@Component
public class Constants {
    private Constants() {}; // can't instantiate this class

    public static final String SECRET_KEY_CONST = "secretkey";
}
