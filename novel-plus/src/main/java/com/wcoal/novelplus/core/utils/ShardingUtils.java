package com.wcoal.novelplus.core.utils;

/**
 * 分片工具类
 * 用途：计算数据路由到哪个数据库和表
 */
public class ShardingUtils {

    // 数据库数量
    private static final int DATABASE_COUNT = 4;

    // 每个数据库的表数量
    private static final int TABLE_COUNT_PER_DB = 16;

    /**
     * 计算数据库索引
     * 算法：book_id % 4
     *
     * @param bookId 书籍ID
     * @return 数据库编号 (0-3)
     */
    public static int getDatabaseIndex(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId 不能为空");
        }
        return (int) (bookId % DATABASE_COUNT);
    }

    /**
     * 计算表索引
     * 算法：(book_id / 4) % 16
     *
     * @param bookId 书籍ID
     * @return 表编号 (0-15)
     */
    public static int getTableIndex(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId 不能为空");
        }
        return (int) ((bookId / DATABASE_COUNT) % TABLE_COUNT_PER_DB);
    }

    /**
     * 获取数据库名称
     *
     * @param bookId 书籍ID
     * @return 数据库名称，如：novel_db_0
     */
    public static String getDatabaseName(Long bookId) {
        int dbIndex = getDatabaseIndex(bookId);
        return "novel_db_" + dbIndex;
    }

    /**
     * 获取章节表名称
     *
     * @param bookId 书籍ID
     * @return 表名称，如：book_chapter_0
     */
    public static String getChapterTableName(Long bookId) {
        int tableIndex = getTableIndex(bookId);
        return "book_chapter_" + tableIndex;
    }

    /**
     * 获取内容表名称
     *
     * @param bookId 书籍ID
     * @return 表名称，如：book_content_0
     */
    public static String getContentTableName(Long bookId) {
        int tableIndex = getTableIndex(bookId);
        return "book_content_" + tableIndex;
    }

    /**
     * 获取完整的章节表路由
     *
     * @param bookId 书籍ID
     * @return 完整路由，如：novel_db_0.book_chapter_0
     */
    public static String getChapterTableRoute(Long bookId) {
        return getDatabaseName(bookId) + "." + getChapterTableName(bookId);
    }

    /**
     * 获取完整的内容表路由
     *
     * @param bookId 书籍ID
     * @return 完整路由，如：novel_db_0.book_content_0
     */
    public static String getContentTableRoute(Long bookId) {
        return getDatabaseName(bookId) + "." + getContentTableName(bookId);
    }
}
