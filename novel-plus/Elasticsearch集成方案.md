# Elasticsearch 集成方案

## 一、技术选型

**推荐方案：Spring Data Elasticsearch + Docker部署**

- Spring Boot 3.3.0 → Elasticsearch 8.x
- 使用官方 Spring Data Elasticsearch
- Docker Compose 统一管理

## 二、快速集成步骤

### 1. 添加依赖（pom.xml）

```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 2. Docker Compose 配置

在 `docker-compose.yml` 中添加：

```yaml
elasticsearch:
  image: elasticsearch:8.11.0
  container_name: elasticsearch
  ports:
    - "9200:9200"
    - "9300:9300"
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
    - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
  volumes:
    - /root/usr/local/elasticsearch/data:/usr/share/elasticsearch/data
    - /root/usr/local/elasticsearch/plugins:/usr/share/elasticsearch/plugins
  networks:
    - novel

kibana:
  image: kibana:8.11.0
  container_name: kibana
  ports:
    - "5601:5601"
  environment:
    - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
  depends_on:
    - elasticsearch
  networks:
    - novel
```

### 3. 配置文件（application.yml）

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 3s
    socket-timeout: 5s
```

### 4. 核心代码实现

#### 4.1 实体类（BookDocument.java）

```java
@Document(indexName = "books")
@Data
public class BookDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String bookName;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String authorName;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String bookDesc;
    
    @Field(type = FieldType.Keyword)
    private String categoryName;
    
    @Field(type = FieldType.Integer)
    private Integer bookStatus;
    
    @Field(type = FieldType.Long)
    private Long visitCount;
    
    @Field(type = FieldType.Long)
    private Long wordCount;
    
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createTime;
    
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updateTime;
}
```

#### 4.2 Repository（BookSearchRepository.java）

```java
public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, Long> {
    
    // 按书名搜索
    List<BookDocument> findByBookNameContaining(String bookName);
    
    // 按作者搜索
    List<BookDocument> findByAuthorNameContaining(String authorName);
    
    // 组合搜索
    List<BookDocument> findByBookNameContainingOrAuthorNameContaining(
        String bookName, String authorName);
}
```

#### 4.3 Service（BookSearchService.java）

```java
@Service
@RequiredArgsConstructor
public class BookSearchService {
    
    private final BookSearchRepository bookSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;
    
    /**
     * 全文搜索（推荐）
     */
    public List<BookDocument> search(String keyword, int page, int size) {
        // 构建多字段查询
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("bookName^3", "authorName^2", "bookDesc")  // 权重：书名>作者>简介
                .type(TextQueryType.BestFields)
                .fuzziness("AUTO")))  // 模糊匹配
            .withPageable(PageRequest.of(page, size))
            .withSort(Sort.by(Sort.Direction.DESC, "visitCount"))  // 按热度排序
            .build();
        
        SearchHits<BookDocument> searchHits = elasticsearchTemplate.search(query, BookDocument.class);
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * 高级搜索（带过滤条件）
     */
    public List<BookDocument> advancedSearch(String keyword, String category, Integer status) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            boolQuery.must(m -> m.multiMatch(mm -> mm
                .query(keyword)
                .fields("bookName^3", "authorName^2", "bookDesc")));
        }
        
        // 分类过滤
        if (StringUtils.hasText(category)) {
            boolQuery.filter(f -> f.term(t -> t
                .field("categoryName")
                .value(category)));
        }
        
        // 状态过滤
        if (status != null) {
            boolQuery.filter(f -> f.term(t -> t
                .field("bookStatus")
                .value(status)));
        }
        
        NativeQuery query = NativeQuery.builder()
            .withQuery(boolQuery.build()._toQuery())
            .build();
        
        SearchHits<BookDocument> searchHits = elasticsearchTemplate.search(query, BookDocument.class);
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * 同步单本书到ES
     */
    public void syncBook(BookDocument book) {
        bookSearchRepository.save(book);
    }
    
    /**
     * 批量同步
     */
    public void syncAllBooks(List<BookDocument> books) {
        bookSearchRepository.saveAll(books);
    }
    
    /**
     * 删除索引
     */
    public void deleteBook(Long bookId) {
        bookSearchRepository.deleteById(bookId);
    }
}
```

#### 4.4 Controller（BookSearchController.java）

```java
@RestController
@RequestMapping("/front/search")
@RequiredArgsConstructor
public class BookSearchController {
    
    private final BookSearchService bookSearchService;
    
    @GetMapping
    public RestResp<List<BookDocument>> search(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        List<BookDocument> books = bookSearchService.search(keyword, page, size);
        return RestResp.ok(books);
    }
    
    @GetMapping("/advanced")
    public RestResp<List<BookDocument>> advancedSearch(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Integer status) {
        
        List<BookDocument> books = bookSearchService.advancedSearch(keyword, category, status);
        return RestResp.ok(books);
    }
}
```

### 5. 数据同步策略

#### 方案A：实时同步（推荐）

在 BookService 的增删改方法中直接调用 ES 同步：

```java
@Service
@RequiredArgsConstructor
public class BookInfoServiceImpl {
    
    private final BookSearchService bookSearchService;
    
    public void saveBook(Book book) {
        // 1. 保存到MySQL
        bookMapper.insert(book);
        
        // 2. 同步到ES
        BookDocument doc = convertToDocument(book);
        bookSearchService.syncBook(doc);
    }
}
```

#### 方案B：异步同步（高并发场景）

使用 RabbitMQ + XXL-Job：

```java
// 1. 发送MQ消息
rabbitTemplate.convertAndSend("book.sync.exchange", "book.sync", bookId);

// 2. 消费者同步到ES
@RabbitListener(queues = "book.sync.queue")
public void syncToES(Long bookId) {
    Book book = bookMapper.selectById(bookId);
    BookDocument doc = convertToDocument(book);
    bookSearchService.syncBook(doc);
}

// 3. XXL-Job定时全量同步（每天凌晨）
@XxlJob("syncAllBooksToES")
public void syncAllBooks() {
    List<Book> books = bookMapper.selectList(null);
    List<BookDocument> docs = books.stream()
        .map(this::convertToDocument)
        .collect(Collectors.toList());
    bookSearchService.syncAllBooks(docs);
}
```

### 6. 安装中文分词器（IK Analyzer）

```bash
# 进入容器
docker exec -it elasticsearch bash

# 安装IK分词器
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip

# 重启容器
docker restart elasticsearch
```

## 三、性能优化建议

1. **索引优化**
   - 设置合理的分片数（单节点建议1个主分片）
   - 关闭不需要的字段索引
   - 使用 `_source` 过滤减少返回数据

2. **查询优化**
   - 使用 `from + size` 分页（浅分页）
   - 深度分页使用 `search_after`
   - 合理设置超时时间

3. **缓存策略**
   - 热门搜索词结果缓存到 Redis（5分钟）
   - 搜索建议使用 Caffeine 本地缓存

4. **监控告警**
   - 使用 Kibana 监控集群状态
   - 设置慢查询日志（>1s）

## 四、部署清单

```bash
# 1. 启动 Elasticsearch
docker-compose up -d elasticsearch kibana

# 2. 验证服务
curl http://localhost:9200

# 3. 安装IK分词器
docker exec -it elasticsearch bash
./bin/elasticsearch-plugin install [IK插件地址]
docker restart elasticsearch

# 4. 创建索引（自动创建或手动创建）
# 访问 Kibana: http://localhost:5601

# 5. 全量同步数据
# 调用 XXL-Job 任务或执行初始化脚本
```

## 五、常见问题

**Q1: 数据不一致怎么办？**
- 使用 XXL-Job 定时全量同步（每天凌晨）
- 提供手动同步接口

**Q2: 搜索速度慢？**
- 检查分词器是否正确
- 优化查询语句，减少不必要的字段
- 增加 ES 内存配置

**Q3: 如何实现搜索高亮？**
```java
NativeQuery query = NativeQuery.builder()
    .withQuery(...)
    .withHighlightQuery(new HighlightQuery(
        new Highlight(List.of(
            new HighlightField("bookName"),
            new HighlightField("authorName")
        )), BookDocument.class))
    .build();
```

## 六、预期效果

- **搜索响应时间**：< 100ms
- **支持模糊搜索**：拼音、错别字容错
- **支持高级过滤**：分类、状态、排序
- **数据一致性**：实时同步 + 定时全量
