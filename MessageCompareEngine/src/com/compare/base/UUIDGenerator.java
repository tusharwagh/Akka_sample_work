/*
 * UUIDUtil.java
 * Copyright (c) 2012 CREDIT SUISSE Technology and Operations. All Rights Reserved.
 * This software is the proprietary information of CREDIT SUISSE Technology and Operations.
 * Use is subject to license and non-disclosure terms.
 * Last modified by: F658917
 * Last modified at: 2012-07-04 09:16:59
 */

package com.compare.base;

import java.util.UUID;

public class UUIDGenerator {

    public static String next() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase(); // replace "-" 36 -> 32 char
    }
}
