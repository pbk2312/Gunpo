package com.example.gunpo.constants;

public class RedisConstants {
    public static final String REDIS_KEY_PREFIX = "smoking_area:";
    public static final String GYEONGGI_MERCHANT_KEY_PREFIX = "GYEONGGI_MERCHANT:";
    public static final String EMAIL_CERTIFICATION_PREFIX = "emailCertification:";
    public static final String VIEW_COUNT_PREFIX = "viewed:board:";

    public static final String LIKE_PREFIX = "board:like:";
    public static final long LIKE_EXPIRE_TIME = 60 * 60 * 24; // 1일 (초 단위)
    public static final long VIEW_EXPIRE_TIME = 24 * 60 * 60L;
    public static final long EMAIL_CERTIFICATION_EXPIRE_TIME = 60 * 60;

    private RedisConstants() {
    }

}
