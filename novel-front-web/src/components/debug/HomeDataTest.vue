<template>
  <div class="home-data-test">
    <h3>首页数据调试</h3>
    
    <div class="test-section">
      <h4>轮播图数据 (sliderContent)</h4>
      <div v-for="(item, index) in sliderContent" :key="index" class="data-item">
        <p><strong>书名:</strong> {{ item.bookName }}</p>
        <p><strong>原始picUrl:</strong> {{ item.picUrl }}</p>
        <p><strong>处理后URL:</strong> {{ getImageUrl(item.picUrl) }}</p>
        <img :src="getImageUrl(item.picUrl)" alt="" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
        <hr />
      </div>
    </div>
    
    <div class="test-section">
      <h4>本周强推数据 (weekcommend)</h4>
      <div v-for="(item, index) in weekcommend" :key="index" class="data-item">
        <p><strong>书名:</strong> {{ item.bookName }}</p>
        <p><strong>原始picUrl:</strong> {{ item.picUrl }}</p>
        <p><strong>处理后URL:</strong> {{ getImageUrl(item.picUrl) }}</p>
        <img :src="getImageUrl(item.picUrl)" alt="" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
        <hr />
      </div>
    </div>
    
    <div class="test-section">
      <h4>热门推荐数据 (hotRecommend)</h4>
      <div v-for="(item, index) in hotRecommend" :key="index" class="data-item">
        <p><strong>书名:</strong> {{ item.bookName }}</p>
        <p><strong>原始picUrl:</strong> {{ item.picUrl }}</p>
        <p><strong>处理后URL:</strong> {{ getImageUrl(item.picUrl) }}</p>
        <img :src="getImageUrl(item.picUrl)" alt="" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
        <hr />
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { listHomeBooks } from "@/api/home";
import { getImageUrl } from "@/utils/imageHelper";

export default {
  name: "HomeDataTest",
  setup() {
    const state = reactive({
      sliderContent: [],
      weekcommend: [],
      hotRecommend: [],
    });

    onMounted(async () => {
      const { data } = await listHomeBooks();
      console.log('首页API返回数据:', data);

      data.forEach((book) => {
        console.log('书籍数据:', book);
        if (book.type == 0) {
          state.sliderContent.push(book);
        }
        if (book.type == 2) {
          state.weekcommend.push(book);
        }
        if (book.type == 3) {
          state.hotRecommend.push(book);
        }
      });
    });

    return {
      ...toRefs(state),
      getImageUrl,
    };
  },
};
</script>

<style scoped>
.home-data-test {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 40px;
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 5px;
}

.data-item {
  margin-bottom: 20px;
}

.data-item p {
  margin: 5px 0;
  font-family: monospace;
  background: #f5f5f5;
  padding: 5px;
  border-radius: 3px;
}

.data-item img {
  margin-top: 10px;
  object-fit: cover;
}

hr {
  margin: 15px 0;
  border: none;
  border-top: 1px solid #eee;
}
</style>