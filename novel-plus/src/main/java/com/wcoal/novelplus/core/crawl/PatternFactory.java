package com.wcoal.novelplus.core.crawl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 正则Pattern缓存工厂
 * 避免重复编译正则表达式,提升性能
 *
 * @author wcoal
 * @since 2025-11-23
 */
public class PatternFactory {

    /**
     * Pattern缓存
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取Pattern对象,如果缓存中不存在则编译并缓存
     *
     * @param regex 正则表达式
     * @return Pattern对象
     */
    public static Pattern getPattern(String regex) {
        return PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        PATTERN_CACHE.clear();
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存数量
     */
    public static int getCacheSize() {
        return PATTERN_CACHE.size();
    }
}
