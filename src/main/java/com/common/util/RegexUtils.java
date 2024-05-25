package com.common.util;

/**
 * ClassName: RegexUtils
 * Package: com.common.util
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2024/5/25 - 10:27
 * @Version: v1.0
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验保存的菜单的权限标识和组件路径符合规范
 */
public class RegexUtils {

    public static final String REGEX_MENU_PERMISSION = "^([^:]+:)+([^:]+)$";
    public static final String REGEX_MENU_COMPONENT = "^(/[^/]+)+$";

    /**
     * 权限标识
     * @param permission
     * @return
     */
    public static boolean verifyMenuPermission(String permission) {
        // 编译正则表达式
        Pattern pattern = Pattern.compile(REGEX_MENU_PERMISSION);
        // 创建匹配器
        Matcher matcher = pattern.matcher(permission);
        // 尝试匹配整个字符串
        return matcher.matches();
    }

    /**
     * 组件路径
     * @param component
     * @return
     */
    public static boolean verifyMenuComponent(String component) {
        // 编译正则表达式
        Pattern pattern = Pattern.compile(REGEX_MENU_COMPONENT);
        // 创建匹配器
        Matcher matcher = pattern.matcher(component);
        // 尝试匹配整个字符串
        return matcher.matches();
    }
}
