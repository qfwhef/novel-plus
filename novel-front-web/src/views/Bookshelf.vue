<template>
  <Header />
  <div class="main box_center cf mb50">
    <div class="channelWrap cf">
      <div class="title cf">
        <h2>我的书架</h2>
      </div>
      
      <div v-if="loading" class="loading-container">
        <p>加载中...</p>
      </div>

      <div v-else-if="bookList.length === 0" class="empty-bookshelf">
        <img :src="emptyImage" alt="空书架" />
        <p>书架空空如也，快去添加喜欢的小说吧~</p>
        <router-link to="/home" class="btn_ora">去首页看看</router-link>
      </div>

      <div v-else class="bookshelf-list">
        <div class="channelWrap channelPic cf">
          <div class="leftBox">
            <div class="picRecommend cf">
              <div
                class="itemsList"
                v-for="(item, index) in bookList"
                :key="item.id || index"
              >
                <a class="items_img" href="javascript:void(0)" @click="bookDetail(item.bookId)">
                  <img
                    :src="getBookImageUrl(item.picUrl)"
                    :alt="item.bookName"
                  />
                </a>
                <div class="items_txt">
                  <h4>
                    <a href="javascript:void(0)" @click="bookDetail(item.bookId)">{{ item.bookName }}</a>
                  </h4>
                  <p class="author">
                    <a href="javascript:void(0)">作者：{{ item.authorName }}</a>
                  </p>
                  <p class="intro">
                    <a
                      href="javascript:void(0)" 
                      @click="bookDetail(item.bookId)"
                      v-html="item.bookDesc"
                    ></a>
                  </p>
                  <div class="bookshelf-actions">
                    <a 
                      href="javascript:void(0)" 
                      @click="continueReading(item)"
                      class="btn_ora_small"
                    >
                      {{ item.preContentId ? '继续阅读' : '开始阅读' }}
                    </a>
                    <a 
                      href="javascript:void(0)" 
                      @click="removeBook(item.bookId)"
                      class="btn_remove"
                    >
                      移除
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getBookshelfList, removeFromBookshelf } from "@/api/bookshelf";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import emptyImage from "@/assets/images/no_comment.png";
import { getImageUrl } from "@/utils/imageHelper";

export default {
  name: "Bookshelf",
  components: {
    Header,
    Footer,
  },
  setup() {
    const router = useRouter();
    const state = reactive({
      bookList: [],
      loading: true,
    });

    onMounted(() => {
      loadBookshelf();
    });

    const loadBookshelf = async () => {
      try {
        state.loading = true;
        const { data } = await getBookshelfList();
        // 后端返回的是分页数据，需要取list字段
        state.bookList = data?.list || [];
      } catch (error) {
        console.error("加载书架失败:", error);
        ElMessage.error("加载书架失败");
      } finally {
        state.loading = false;
      }
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    const continueReading = (item) => {
      if (item.preContentId) {
        // 如果有上次阅读记录，跳转到上次阅读的章节
        router.push({ path: `/book/${item.bookId}/${item.preContentId}` });
      } else {
        // 否则跳转到第一章
        router.push({ path: `/book/${item.bookId}/${item.firstChapterId}` });
      }
    };

    const removeBook = async (bookId) => {
      try {
        await ElMessageBox.confirm(
          '确定要从书架中移除这本书吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        );
        
        await removeFromBookshelf(bookId);
        ElMessage.success("移除成功");
        loadBookshelf();
      } catch (error) {
        if (error !== 'cancel') {
          console.error("移除失败:", error);
          ElMessage.error("移除失败");
        }
      }
    };

    const getBookImageUrl = (imageUrl) => {
      return getImageUrl(imageUrl, '/assets/images/default.gif');
    };

    return {
      ...toRefs(state),
      emptyImage,
      bookDetail,
      continueReading,
      removeBook,
      getBookImageUrl,
    };
  },
};
</script>

<style scoped>
.loading-container {
  text-align: center;
  padding: 100px 0;
  font-size: 16px;
  color: #999;
}

.empty-bookshelf {
  text-align: center;
  padding: 80px 0;
}

.empty-bookshelf img {
  width: 200px;
  margin-bottom: 20px;
}

.empty-bookshelf p {
  font-size: 16px;
  color: #999;
  margin-bottom: 30px;
}

.bookshelf-list {
  margin-top: 20px;
}

.bookshelf-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.btn_ora_small {
  display: inline-block;
  padding: 5px 15px;
  background: #f70;
  color: #fff;
  border-radius: 3px;
  font-size: 14px;
  text-decoration: none;
}

.btn_ora_small:hover {
  background: #f80;
}

.btn_remove {
  display: inline-block;
  padding: 5px 15px;
  background: #999;
  color: #fff;
  border-radius: 3px;
  font-size: 14px;
  text-decoration: none;
}

.btn_remove:hover {
  background: #666;
}

.title {
  padding: 20px 0;
  border-bottom: 2px solid #f70;
}

.title h2 {
  font-size: 24px;
  color: #333;
}
</style>
