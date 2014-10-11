package com.backagen.peid;

import java.util.regex.Pattern;

/**
 * Created by samwong on 15/07/2014.
 */
public class Constants {
    public static final String MONEY_REGEX = "(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?";
    public static final Pattern MONEY_REGEX_PATTERN = Pattern.compile(MONEY_REGEX);
}
