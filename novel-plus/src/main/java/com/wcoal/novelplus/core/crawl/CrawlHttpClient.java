package com.wcoal.novelplus.core.crawl;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Random;

/**
 * 爬虫HTTP客户端
 * 封装HTTP请求,提供智能延迟和重试机制
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Slf4j
@Component
public class CrawlHttpClient {

    @Value("${crawl.interval.min:100}")
    private Integer intervalMin;

    @Value("${crawl.interval.max:500}")
    private Integer intervalMax;

    @Value("${crawl.http.timeout:10000}")
    private Integer timeout;

    @Value("${crawl.http.retry-count:3}")
    private Integer retryCount;

    private static final int INVALID_HTML_LENGTH = 100;

    private final Random random = new Random();

    private static final ThreadLocal<Integer> RETRY_COUNT_HOLDER = new ThreadLocal<>();

    /**
     * 发送GET请求获取网页内容
     *
     * @param url     目标URL
     * @param charset 字符编码
     * @return 网页内容,失败返回null
     * @throws InterruptedException 线程中断异常
     */
    public String get(String url, String charset) throws InterruptedException {
        // 随机延迟,避免被反爬虫
        if (Objects.nonNull(intervalMin) && Objects.nonNull(intervalMax) && intervalMax > intervalMin) {
            Thread.sleep(random.nextInt(intervalMax - intervalMin + 1) + intervalMin);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // 设置User-Agent伪装成浏览器
            request.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            String body = httpClient.execute(request, (ClassicHttpResponse response) -> {
                int status = response.getCode();
                if (status >= 200 && status < 300) {
                    return EntityUtils.toString(response.getEntity(), Charset.forName(charset));
                }
                log.warn("HTTP请求失败, URL: {}, Status: {}", url, status);
                return null;
            });

            // 验证响应内容
            if (Objects.isNull(body) || body.length() < INVALID_HTML_LENGTH) {
                return processErrorHttpResult(url, charset);
            }

            // 成功获取HTML内容,清除重试计数
            RETRY_COUNT_HOLDER.remove();
            return body;

        } catch (Exception e) {
            log.error("HTTP请求异常, URL: {}, Error: {}", url, e.getMessage());
            return processErrorHttpResult(url, charset);
        }
    }

    /**
     * 处理HTTP请求失败,实现重试机制
     *
     * @param url     目标URL
     * @param charset 字符编码
     * @return 网页内容,失败返回null
     * @throws InterruptedException 线程中断异常
     */
    private String processErrorHttpResult(String url, String charset) throws InterruptedException {
        Integer count = RETRY_COUNT_HOLDER.get();
        if (count == null) {
            count = 0;
        }

        if (count < retryCount) {
            RETRY_COUNT_HOLDER.set(++count);
            log.info("HTTP请求重试第{}次, URL: {}", count, url);
            return get(url, charset);
        }

        // 超过重试次数,清除计数并返回null
        RETRY_COUNT_HOLDER.remove();
        log.error("HTTP请求失败,已重试{}次, URL: {}", retryCount, url);
        return null;
    }
}
