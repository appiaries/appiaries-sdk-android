//
// Created by Appiaries Corporation on 15/04/30.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//
package com.appiaries.baas.sdk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


class ABValidator {

    public enum ValidationRule {
        REQUIRED,
        EMAIL,
        OBJECT_ID,
        LOGIN_ID,
        PASSWORD,
    }

    public static void validate(String key, Object value, ValidationRule rule, Map<String, Object> args) throws ABException {
        SortedSet<ValidationRule> rules = new TreeSet<ValidationRule>();
        rules.add(rule);
        Map<ValidationRule, Map<String, Object>> ruleArgs = new HashMap<>();
        ruleArgs.put(rule, args);
        validate(key, value, rules, ruleArgs);
    }
    public static void validate(String key, Object value, SortedSet<ValidationRule> rules, Map<ValidationRule, Map<String, Object>> ruleArgs) throws ABException {
        for (ValidationRule rule : rules) {
            switch (rule) {
                case REQUIRED:
                    if (value == null) {
                        String msg = messageForRule(ValidationRule.REQUIRED, ruleArgs, String.format("Insufficient parameter. [param: %s]", key));
                        throw new ABException(10001, msg);
                    }
                    break;
                case EMAIL:
                    //TODO: not yet implemented
                    break;
                default:
                    break;
            }
        }
    }


    private static String messageForRule(ValidationRule rule, Map<ValidationRule, Map<String, Object>> ruleArgs) {
        return messageForRule(rule, ruleArgs, null);
    }
    private static String messageForRule(ValidationRule rule, Map<ValidationRule, Map<String, Object>> ruleArgs, String defaultMessage) {
        if (ruleArgs != null) {
            Map<String, Object> args = ruleArgs.get(rule);
            if (args != null) {
                String msg = (String)args.get("msg");
                if (msg != null) {
                    return msg;
                }
            }
        }
        return defaultMessage;
    }
}
