package com.starrating;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        System.out.println("RAW_HASH=" + new BCryptPasswordEncoder().encode("88888888"));
    }
}
