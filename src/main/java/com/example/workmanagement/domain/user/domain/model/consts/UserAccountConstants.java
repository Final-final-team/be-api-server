package com.example.workmanagement.domain.user.domain.model.consts;

import java.util.regex.Pattern;

public interface UserAccountConstants {

    Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    int MIN_NICKNAME_LENGTH = 2;
    int MAX_NICKNAME_LENGTH = 30;
    String DEFAULT_NICKNAME = "사용자";
    int MAX_EMAIL_LENGTH = 320;
    int MAX_SUB_LENGTH = 255;
}
