<template>
  <Header />
  <div class="main box_center cf mb50">
    <div class="channelWrap channelBookInfo cf">
      <div class="bookCover cf">
        <a class="book_cover">
          <img
            id="bookCover"
            class="cover"
            :src="getBookImageUrl(book.picUrl)"
            :alt="book.bookName"
        /></a>
        <div class="book_info">
          <div class="tit">
            <h1>{{ book.bookName }}</h1>
            <!--<i class="vip_b">VIP</i>--><a class="author">{{
              book.authorName
            }}</a>
          </div>
          <ul class="list">
            <li>
              <span class="item"
                >类别：<em>{{ book.categoryName }}</em></span
              >
              <span class="item"
                >状态：<em>{{
                  book.bookStatus == 0 ? "连载中" : "已完结"
                }}</em></span
              >
              <span class="item"
                >总点击：<em id="cTotal">{{ book.visitCount }}</em></span
              >
              <span class="item"
                >总字数：<em>{{ book.wordCount }}</em></span
              >
            </li>
          </ul>
          <div class="intro_txt">
            <p style="white-space:break-spaces" v-html="book.bookDesc"></p>
            <a class="icon_hide" href="javascript:void(0)" onclick=""
              ><i></i>收起</a
            >
            <a class="icon_show" href="javascript:void(0)" onclick=""
              ><i></i>展开</a
            >
          </div>
          <div class="btns" id="optBtn">
            <a
              href="javascript:void(0)"
              @click="bookContent(book.id, book.firstChapterId)"
              class="btn_ora"
              >点击阅读</a
            >
            <span id="cFavs" v-if="uid">
              <a
                v-if="!inBookshelf"
                href="javascript:void(0);"
                class="btn_ora_white btn_addsj"
                @click="addToBookshelf"
                >加入书架</a
              >
              <a
                v-else
                href="javascript:void(0);"
                class="btn_gray btn_addsj"
                @click="removeFromBookshelf"
                >移出书架</a
              >
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="channelBookContent cf">
      <!--left start-->
      <div class="wrap_left fl">
        <div class="wrap_bg">
          <!--章节目录 start-->
          <div class="pad20_nobt">
            <div class="bookChapter">
              <div class="book_tit">
                <div class="fl">
                  <h3>最新章节</h3>
                  <span id="bookIndexCount"
                    >({{ chapterAbout.chapterTotal }}章)</span
                  >
                </div>
                <a
                  class="fr"
                  @click="chapterList(book.id)"
                  href="javascript:void(0)"
                  >全部目录</a
                >
              </div>
              <ul class="list cf">
                <li>
                  <span class="fl font16">
                    <a
                      @click="
                        bookContent(
                          chapterAbout.chapterInfo.bookId,
                          chapterAbout.chapterInfo.id
                        )
                      "
                      href="javascript:void(0)"
                      v-if="chapterAbout.chapterInfo"
                      >{{ chapterAbout.chapterInfo.chapterName }}】</a
                    ></span
                  >
                  <span class="black9 fr" v-if="chapterAbout.chapterInfo"
                    >更新时间：{{
                      chapterAbout.chapterInfo.chapterUpdateTime
                    }}</span
                  >
                </li>
                <li class="zj_yl" id="lastBookContent">
                  <!--go-->
                  　　<span
                    v-html="`${chapterAbout.contentSummary}` + '...'"
                  ></span>
                </li>
                <!--此处是该章节预览，截取最前面的42个字-->
              </ul>
            </div>
          </div>
          <!--章节目录 end-->

          <!--作品评论区 start-->
          <div class="pad20">
            <div class="bookComment">
              <div class="book_tit">
                <div class="fl">
                  <h3>作品评论区</h3>
                  <span id="bookCommentTotal"
                    >({{ newestComments.commentTotal }}条)</span
                  >
                </div>
                <a
                  class="fr"
                  @click="goToAnchor('txtComment')"
                  href="javascript:void(0)"
                  >发表评论</a
                >
              </div>
              <div
                v-if="newestComments.commentTotal == 0"
                class="no_comment"
                id="noCommentPanel"
              >
                <img :src="no_comment" alt="" />
                <span class="block">暂无评论</span>
              </div>
              <div
                v-if="newestComments.commentTotal > 0"
                class="commentBar"
                id="commentPanel"
              >
                <div
                  class="comment_list cf"
                  v-for="(item, index) in newestComments.comments"
                  :key="index"
                >
                  <div class="user_heads fl" vals="389">
                    <img
                      :src="
                        item.commentUserPhoto
                          ? getBookImageUrl(item.commentUserPhoto)
                          : man
                      "
                      class="user_head"
                      alt=""
                    /><span class="user_level1" style="display: none"
                      >见习</span
                    >
                  </div>
                  <ul class="pl_bar fr">
                    <li class="name">{{ item.commentUser }}</li>
                    <li class="dec" v-html="item.commentContent"></li>
                    <li class="other cf">
                      <span class="time fl">{{ item.commentTime }}</span
                      ><span class="fr">
                        <a
                          v-if="uid"
                          href="javascript:void(0);"
                          @click="toggleLike(item.id)"
                          class="zan like-btn"
                          :class="{ 'liked': likeStatus[item.id] }"
                        >
                          <span class="like-icon">{{ likeStatus[item.id] ? '❤' : '♡' }}</span>
                          <span>赞({{ item.likeCount || 0 }})</span>
                        </a>
                        <span v-if="!uid" class="zan-disabled">
                          <span class="like-icon">♡</span>
                          <span>赞({{ item.likeCount || 0 }})</span>
                        </span>
                        |
                        <a
                          href="javascript:void(0);"
                          @click="toggleReplyInput(item.id)"
                          class="zan"
                          >回复({{ item.replyCount || 0 }})</a
                        >
                        <span v-if="item.commentUserId == uid">
                          |
                          <a
                            href="javascript:void(0);"
                            @click="
                              updateUserComment(item.id, item.commentContent)
                            "
                            class="zan"
                            >修改</a
                          >
                          |
                          <a
                            href="javascript:void(0);"
                            @click="deleteUserComment(item.id)"
                            class="zan"
                            >删除</a
                          >
                        </span>
                      </span>
                    </li>
                    <!-- 回复列表 -->
                    <li v-if="commentReplies[item.id] && commentReplies[item.id].length > 0" class="reply_list">
                      <div v-for="reply in commentReplies[item.id]" :key="reply.id" class="reply_item">
                        <span class="reply_user">{{ reply.username }}：</span>
                        <span class="reply_content">{{ reply.replyContent }}</span>
                        <span class="reply_time">{{ reply.createTime }}</span>
                        <a
                          v-if="reply.userId == uid"
                          href="javascript:void(0);"
                          @click="deleteReply(item.id, reply.id)"
                          class="reply_delete"
                          >删除</a
                        >
                      </div>
                    </li>
                    <!-- 回复输入框 -->
                    <li v-if="showReplyInput[item.id]" class="reply_input_box">
                      <textarea
                        v-model="replyContent"
                        class="reply_input"
                        placeholder="写下你的回复..."
                        rows="2"
                      ></textarea>
                      <div class="reply_btn_group">
                        <el-button size="small" @click="cancelReply(item.id)">取消</el-button>
                        <el-button size="small" type="primary" @click="submitReply(item.id)">发表</el-button>
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
              <el-dialog
                v-model="dialogUpdateCommentFormVisible"
                title="评论修改"
              >
                <el-form>
                  <el-form-item label="评论内容">
                    <el-input type="textarea" v-model="updateComment" />
                  </el-form-item>
                </el-form>
                <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="dialogUpdateCommentFormVisible = false"
                      >取消</el-button
                    >
                    <el-button type="primary" @click="goUpdateComment()"
                      >确定</el-button
                    >
                  </span>
                </template>
              </el-dialog>

              <!--无评论时此处隐藏-->
              <div class="more_bar" id="moreCommentPanel" style="display: none">
                <a href="/book/comment-1431636283466297344.html"
                  >查看全部评论&gt;</a
                >
              </div>

              <div class="reply_bar" id="reply_bar">
                <div class="tit">
                  <span class="fl font16">发表评论</span>

                  <span class="fr black9" style="display: none"
                    >请先 <a class="orange" href="/user/login.html">登录</a
                    ><em class="ml10 mr10">|</em
                    ><a class="orange" href="/user/register.html">注册</a></span
                  >
                </div>

                <textarea
                  v-model="commentContent"
                  name="txtComment"
                  rows="2"
                  cols="20"
                  id="txtComment"
                  class="replay_text"
                  placeholder="我来说两句..."
                ></textarea>
                <div class="reply_btn">
                  <span class="fl black9"
                    ><em class="ml5" id="emCommentNum">0/1000</em> 字</span
                  >
                  <span class="fr"
                    ><a
                      class="btn_ora"
                      href="javascript:void(0);"
                      @click="userComment"
                      >发表</a
                    ></span
                  >
                </div>
              </div>
            </div>
          </div>
          <!--作品评论区 end-->
        </div>
      </div>
      <!--left end-->

      <!--right start-->
      <div class="wrap_right fr">
        <!--作者专栏s-->
        <div class="wrap_inner author_info mb20">
          <div class="author_head cf">
            <a href="javascript:void(0);" class="head"
              ><img :src="author_head" alt="作者头像" id="authorLogoImg"
            /></a>
            <div class="msg">
              <span class="icon_qyzz">签约作家</span>
              <h4>
                <a href="javascript:searchByK('冷漠的天蝎')">{{
                  book.authorName
                }}</a>
              </h4>
            </div>
          </div>
          <div class="author_intro cf">
            <h4>作者有话说</h4>
            <div class="intro_txt" id="authorNote">
              亲亲们，你们的支持是我最大的动力！求点击、求推荐、求书评哦！
            </div>
          </div>
        </div>

        <div id="RelateBookOther" class="wrap_inner wrap_right_cont mb20">
          <div class="title cf">
            <h3 class="on">同类推荐</h3>
          </div>
          <div class="tj_bar">
            <ul id="recBookList">
              <li v-for="(item, index) in books" :key="index">
                <div class="book_intro">
                  <div class="cover">
                    <a href="javascript:void(0)" @click="bookDetail(item.id)"
                      ><img
                        :id="'bookCover' + `${index}`"
                        :src="getBookImageUrl(item.picUrl)"
                        :alt="item.bookName"
                    /></a>
                  </div>
                  <div class="dec">
                    <a href="javascript:void(0)" @click="bookDetail(item.id)">{{
                      item.bookName
                    }}</a>
                    <a
                      class="txt"
                      href="javascript:void(0)"
                      @click="bookDetail(item.id)"
                      v-html="item.bookDesc"
                    ></a>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <!--right end-->
    </div>
  </div>

  <Footer />
</template>

<script>
import "@/assets/styles/book.css";
import man from "@/assets/images/man.png";
import { reactive, toRefs, onMounted, onUpdated } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import {
  getBookById,
  addVisitCount,
  getLastChapterAbout,
  listRecBooks,
  listNewestComments,
} from "@/api/book";
import { comment, deleteComment, updateComment, saveCommentReply, deleteCommentReply, listCommentReplies } from "@/api/user";
import { toggleCommentLike, batchGetLikeStatus } from "@/api/like";
import { 
  checkInBookshelf, 
  addToBookshelf as addToBookshelfApi, 
  removeFromBookshelf as removeFromBookshelfApi 
} from "@/api/bookshelf";
import { getUid } from "@/utils/auth";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import author_head from "@/assets/images/author_head.png";
import no_comment from "@/assets/images/no_comment.png";
import { goToAnchor } from "@/utils";
import { getImageUrl } from "@/utils/imageHelper";
export default {
  name: "book",
  components: {
    Header,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      uid: getUid(),
      book: {},
      books: [],
      chapterAbout: {},
      commentContent: "",
      newestComments: {},
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
      dialogUpdateCommentFormVisible: false,
      commentId: "",
      updateComment: "",
      inBookshelf: false,
      replyContent: "",
      currentReplyCommentId: null,
      commentReplies: {},
      showReplyInput: {},
      likeStatus: {}, // 点赞状态
      likeStatus: {},
    });
    onMounted(() => {
      const bookId = route.params.id;
      loadBook(bookId);
      loadRecBooks(bookId);
      loadLastChapterAbout(bookId);
      loadNewestComments(bookId);
      if (state.uid) {
        checkBookshelfStatus(bookId);
      }
    });

    onUpdated(() => {
      console.log("onUpdated==========================");
      for (let i = 0; i < state.books.length; i++) {
        document
          .getElementById("bookCover" + i)
          .setAttribute("onerror", "this.src='default.gif';this.onerror=null");
      }
    });

    const loadBook = async (bookId) => {
      const { data } = await getBookById(bookId);
      state.book = data;
      document
        .getElementById("bookCover")
        .setAttribute("onerror", "this.src='default.gif';this.onerror=null");
      addBookVisit(bookId);
    };

    const loadRecBooks = async (bookId) => {
      const { data } = await listRecBooks({ bookId: bookId });
      state.books = data;
    };

    const loadLastChapterAbout = async (bookId) => {
      const { data } = await getLastChapterAbout({ bookId: bookId });
      state.chapterAbout = data;
    };

    const bookContent = (bookId, chapterId) => {
      router.push({ path: `/book/${bookId}/${chapterId}` });
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
      loadBook(bookId);
      loadRecBooks(bookId);
      loadLastChapterAbout(bookId);
      loadNewestComments(bookId);
      if (state.uid) {
        checkBookshelfStatus(bookId);
      }
    };

    const chapterList = (bookId) => {
      router.push({ path: `/chapter_list/${bookId}` });
    };

    const addBookVisit = async (bookId) => {
      addVisitCount({ bookId: bookId });
    };

    const loadNewestComments = async (bookId) => {
      const { data } = await listNewestComments({ bookId: bookId });
      state.newestComments = data;
      
      // 加载点赞状态
      if (state.uid && data.comments && data.comments.length > 0) {
        try {
          const commentIds = data.comments.map(c => c.id);
          const { data: likeStatusData } = await batchGetLikeStatus(commentIds);
          state.likeStatus = likeStatusData;
        } catch (error) {
          console.error("加载点赞状态失败:", error);
        }
      }
      
      // 自动加载每条评论的回复
      if (data.comments && data.comments.length > 0) {
        for (const comment of data.comments) {
          if (comment.replyCount > 0) {
            await loadReplies(comment.id);
          }
        }
      }
    };

    const userComment = async () => {
      if (!state.commentContent) {
        ElMessage.error("用户评论不能为空！");
        return;
      }
      if (state.commentContent.length < 10) {
        ElMessage.error("评论不能少于 10 个字符！");
        return;
      }
      if (state.commentContent.length > 512) {
        ElMessage.error("评论不能多于 512 个字符！");
        return;
      }
      await comment({
        bookId: state.book.id,
        commentContent: state.commentContent,
      });
      state.commentContent = "";
      loadNewestComments(state.book.id);
    };

    const updateUserComment = async (id, comment) => {
      state.dialogUpdateCommentFormVisible = true;
      state.updateComment = comment;
      state.commentId = id;
    };

    const deleteUserComment = async (id) => {
      ElMessageBox.confirm(
        '确定要删除这条评论吗？删除后将无法恢复。',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
        .then(async () => {
          try {
            await deleteComment(id);
            loadNewestComments(state.book.id);
            ElMessage.success("删除成功！");
          } catch (error) {
            console.error("删除评论失败:", error);
            ElMessage.error("删除失败！");
          }
        })
        .catch(() => {
          // 用户取消删除
        });
    };

    const goUpdateComment = async (id) => {
      state.dialogUpdateCommentFormVisible = false;
      await updateComment(state.commentId, state.updateComment);
      loadNewestComments(state.book.id);
    };

    const toggleReplyInput = async (commentId) => {
      state.showReplyInput[commentId] = !state.showReplyInput[commentId];
      state.currentReplyCommentId = commentId;
      
      // 加载回复列表
      if (state.showReplyInput[commentId] && !state.commentReplies[commentId]) {
        await loadReplies(commentId);
      }
    };

    const loadReplies = async (commentId) => {
      try {
        const { data } = await listCommentReplies(commentId);
        state.commentReplies[commentId] = data;
      } catch (error) {
        console.error("加载回复失败:", error);
      }
    };

    const submitReply = async (commentId) => {
      if (!state.replyContent) {
        ElMessage.error("回复内容不能为空！");
        return;
      }
      if (state.replyContent.length < 2) {
        ElMessage.error("回复不能少于 2 个字符！");
        return;
      }
      if (state.replyContent.length > 512) {
        ElMessage.error("回复不能多于 512 个字符！");
        return;
      }

      try {
        await saveCommentReply({
          commentId: commentId,
          replyContent: state.replyContent,
        });
        state.replyContent = "";
        state.showReplyInput[commentId] = false;
        
        // 重新加载评论和回复
        await loadNewestComments(state.book.id);
        await loadReplies(commentId);
        
        ElMessage.success("回复成功！");
      } catch (error) {
        console.error("回复失败:", error);
        ElMessage.error("回复失败！");
      }
    };

    const cancelReply = (commentId) => {
      state.showReplyInput[commentId] = false;
      state.replyContent = "";
    };

    const deleteReply = async (commentId, replyId) => {
      ElMessageBox.confirm(
        '确定要删除这条回复吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
        .then(async () => {
          try {
            await deleteCommentReply(replyId);
            await loadReplies(commentId);
            await loadNewestComments(state.book.id);
            ElMessage.success("删除成功！");
          } catch (error) {
            console.error("删除回复失败:", error);
            ElMessage.error("删除失败！");
          }
        })
        .catch(() => {
          // 用户取消删除
        });
    };

    const toggleLike = async (commentId) => {
      if (!state.uid) {
        ElMessage.warning('请先登录');
        return;
      }
      
      try {
        const currentLiked = state.likeStatus[commentId];
        const newLiked = !currentLiked;
        
        // 调用统一的切换接口
        await toggleCommentLike(commentId, newLiked);
        
        // 更新本地状态
        state.likeStatus[commentId] = newLiked;
        
        // 更新点赞数量
        const comment = state.newestComments.comments.find(c => c.id === commentId);
        if (comment) {
          comment.likeCount = newLiked 
            ? (comment.likeCount || 0) + 1 
            : Math.max(0, (comment.likeCount || 0) - 1);
        }
        
        ElMessage.success(newLiked ? '点赞成功' : '已取消点赞');
      } catch (error) {
        console.error('点赞操作失败:', error);
        ElMessage.error('操作失败');
      }
    };

    const checkBookshelfStatus = async (bookId) => {
      try {
        const { data } = await checkInBookshelf(bookId);
        state.inBookshelf = data;
      } catch (error) {
        console.error("检查书架状态失败:", error);
      }
    };

    const addToBookshelf = async () => {
      try {
        await addToBookshelfApi({ bookId: state.book.id });
        state.inBookshelf = true;
        ElMessage.success("已加入书架");
      } catch (error) {
        console.error("加入书架失败:", error);
        ElMessage.error("加入书架失败");
      }
    };

    const removeFromBookshelf = async () => {
      try {
        await removeFromBookshelfApi(state.book.id);
        state.inBookshelf = false;
        ElMessage.success("已移出书架");
      } catch (error) {
        console.error("移出书架失败:", error);
        ElMessage.error("移出书架失败");
      }
    };
    
    // 智能处理图片URL
    const getBookImageUrl = (imageUrl) => {
      return getImageUrl(imageUrl, '/assets/images/default.gif');
    };

    return {
      ...toRefs(state),
      author_head,
      no_comment,
      bookContent,
      bookDetail,
      chapterList,
      goToAnchor,
      userComment,
      deleteUserComment,
      man,
      updateUserComment,
      goUpdateComment,
      addToBookshelf,
      removeFromBookshelf,
      getBookImageUrl,
      toggleReplyInput,
      submitReply,
      cancelReply,
      deleteReply,
      toggleLike,
      toggleLike,
    };
  },
  mounted() {
    $(".icon_show").click(function () {
      $(this).hide();
      $(".icon_hide").show();
      $(".intro_txt").innerHeight("auto");
    });
    $(".icon_hide").click(function () {
      $(this).hide();
      $(".icon_show").show();
      $(".intro_txt").innerHeight("");
    });

    $("#AuthorOtherNovel li").unbind("mouseover");

    $("#txtComment").on("input propertychange", function () {
      var count = $(this).val().length;
      $("#emCommentNum").html(count + "/1000");
      if (count > 1000) {
        $("#txtComment").val($("#txtComment").val().substring(0, 1000));
      }
    });
  },
};
</script>

<style>
.el-button:not(.is-text) {
  border: #f80;
  border-color: #f80;
}
.el-button--primary {
  --el-button-hover-bg-color: #f80;
}

.el-button--primary {
  --el-button-bg-color: #f70;
}

.el-button {
  --el-button-hover-text-color: #fafafa;
}

.el-button {
  --el-button-hover-bg-color: #ff880061;
}

.btn_gray {
  background: #999 !important;
  border-color: #999 !important;
}

.btn_gray:hover {
  background: #666 !important;
  border-color: #666 !important;
}

.reply_list {
  margin-top: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.reply_item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  line-height: 1.6;
}

.reply_item:last-child {
  border-bottom: none;
}

.reply_user {
  color: #ed4259;
  font-weight: bold;
  margin-right: 5px;
}

.reply_content {
  color: #333;
}

.reply_time {
  color: #999;
  font-size: 12px;
  margin-left: 10px;
}

.reply_delete {
  color: #999;
  margin-left: 10px;
  cursor: pointer;
}

.reply_delete:hover {
  color: #ed4259;
}

.reply_input_box {
  margin-top: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.reply_input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  font-size: 13px;
}

.reply_btn_group {
  margin-top: 8px;
  text-align: right;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.like-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.like-btn:hover .like-icon {
  transform: scale(1.2);
}

.zan.liked {
  color: #ff8800 !important;
}

.zan.liked .like-icon {
  color: #ff8800 !important;
  animation: likeAnimation 0.5s ease;
}

@keyframes likeAnimation {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

.zan-disabled {
  color: #999;
  cursor: not-allowed;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
