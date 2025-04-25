/*
 *
 */
package com.sinergise.util;

import java.math.BigDecimal;

public class StringInput {
    public static final BigDecimal readDecimal(String input) {
        input=input.trim();
        int commaIdx=input.indexOf(',');
        boolean commaTwice=input.indexOf(',',commaIdx+1)>0;
        int dotIdx=input.indexOf('.');
        boolean dotTwice=input.indexOf('.',dotIdx+1)>0;
        
        if (commaTwice) {
            return new BigDecimal(input.replaceAll(",", ""));
        } else if (dotTwice) {
            return new BigDecimal(input.replaceAll("\\.", "").replaceAll(",", "."));
        }
        if (commaIdx>0) {
            if (dotIdx>commaIdx) {
                return new BigDecimal(input.replaceAll(",", ""));
            } else if (dotIdx>0) {
                return new BigDecimal(input.replaceAll("\\.", "").replaceAll(",", "."));
            } else { // only comma
                return new BigDecimal(input.replaceAll(",", "."));
            }
        } else { // only dot
            return new BigDecimal(input);
        }
    }
    
    public static final BigDecimal readFirstDecimal(String input) {
        input=input.trim();
        int spcIdx=input.indexOf(' ');
        while (spcIdx > 0) {
            input=input.substring(0, spcIdx);
            spcIdx=input.indexOf(' ');
            if (spcIdx<0) spcIdx=input.indexOf('-',1);
        }
        System.err.println(input);
        return readDecimal(input);
    }

    public static void main(String[] args) {
        System.out.println(readDecimal("0,00 "));
        System.out.println(readDecimal("0.00 "));
        System.out.println(readDecimal("10,000 "));
        System.out.println(readDecimal("1.000,00 "));
        System.out.println(readDecimal("1,000.00 "));
        System.out.println(readDecimal("10,000,000.00 "));
        System.out.println(readFirstDecimal("-10.000.000,00 asd"));
        System.out.println(readDecimal("-10.000.000,00 "));
    }
}
