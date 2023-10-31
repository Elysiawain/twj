package com.tangwuji.reggie.utils;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Component;

/**
 * 描述 工具 检验地址是否合法
 */
@Component
public class EmailUtil {
    public static boolean isValidEmailAddress(String email) throws AddressException {
        boolean result = true;
        InternetAddress internetAddress = new InternetAddress(email);
        try {
            internetAddress.validate();
        } catch (AddressException e) {
            e.printStackTrace();
            result = false;
        }
        return  result;
    }
}
