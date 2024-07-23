/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.winfxk.winfxklia.tool.copy.net;

import java.io.UnsupportedEncodingException;

@SuppressWarnings("unused")
public class URLDecoder {
    static String dfltEncName = URLEncoder.dfltEncName;
    static String utf8 = "UTF-8";

    public static String decode(String s) throws UnsupportedEncodingException {
        return decode(s, utf8);
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException {
        boolean needToChange = false;
        int numChars = s.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;
        if (enc.isEmpty()) throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    i++;
                    needToChange = true;
                    break;
                case '%':
                    try {
                        if (bytes == null) bytes = new byte[(numChars - i) / 3];
                        int pos = 0;
                        while (((i + 2) < numChars) && (c == '%')) {
                            int v = Integer.parseInt(s.substring(i + 1, i + 3), 16);
                            if (v < 0)
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value");
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < numChars) c = s.charAt(i);
                        }
                        if ((i < numChars) && (c == '%'))
                            throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                        sb.append(new String(bytes, 0, pos, enc));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }
        return (needToChange ? sb.toString() : s);
    }
}
